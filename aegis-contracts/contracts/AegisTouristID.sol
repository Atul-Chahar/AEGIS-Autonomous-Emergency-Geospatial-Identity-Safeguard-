// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

/**
 * @title AegisTouristID
 * @dev Privacy-Preserving Ephemeral Pseudonymous Identity Commitment Registry
 * Stores ONLY keccak256 hashes of tourist IDs (keccak256(touristId + ":" + salt)),
 * trip validity timestamps, and cryptographic vouchers.
 * No raw PII (Passport, Aadhaar, phone numbers) is ever stored on-chain.
 */
contract AegisTouristID {
    address public admin;

    enum Status { ACTIVE, EXPIRED, REVOKED }

    struct IDVoucher {
        bytes32 idHash;             // keccak256(TouristID + ":" + Salt)
        bytes32 itineraryHash;      // keccak256(Itinerary Details)
        uint256 validFrom;
        uint256 validTo;
        Status status;
        uint256 registrationTime;
    }

    // Mapping from tourist ID hash to Voucher
    mapping(bytes32 => IDVoucher) public vouchers;
    
    // Total registered count for statistics
    uint256 public totalVouchersRegistered;

    // Events for real-time audit trailing
    event VoucherRegistered(bytes32 indexed idHash, uint256 validFrom, uint256 validTo, uint256 timestamp);
    event VoucherVerified(bytes32 indexed idHash, bool isValid, uint256 timestamp);
    event VoucherExpired(bytes32 indexed idHash, uint256 timestamp);
    event VoucherRevoked(bytes32 indexed idHash, string reason, uint256 timestamp);

    modifier onlyAdmin() {
        require(msg.sender == admin, "AEGIS: Only admin authority can execute");
        _;
    }

    constructor() {
        admin = msg.sender;
    }

    /**
     * @dev Register a new ephemeral tourist ID voucher on-chain
     * @param _idHash Hashed commitment of keccak256(Tourist ID + ":" + Salt)
     * @param _itineraryHash Hashed commitment of planned trip itinerary
     * @param _validDays Duration of validity in days
     */
    function registerTripVoucher(
        bytes32 _idHash,
        bytes32 _itineraryHash,
        uint256 _validDays
    ) external returns (bool) {
        require(vouchers[_idHash].validFrom == 0, "AEGIS: Voucher hash already registered");
        require(_validDays > 0 && _validDays <= 90, "AEGIS: Validity duration must be between 1 and 90 days");

        uint256 startTime = block.timestamp;
        uint256 endTime = block.timestamp + (_validDays * 1 days);

        vouchers[_idHash] = IDVoucher({
            idHash: _idHash,
            itineraryHash: _itineraryHash,
            validFrom: startTime,
            validTo: endTime,
            status: Status.ACTIVE,
            registrationTime: startTime
        });

        totalVouchersRegistered++;

        emit VoucherRegistered(_idHash, startTime, endTime, startTime);
        return true;
    }

    /**
     * @dev Verify if a given tourist ID hash is currently valid and active
     * @param _idHash Hashed tourist ID voucher
     */
    function verifyVoucher(bytes32 _idHash) external returns (bool isValid) {
        IDVoucher storage voucher = vouchers[_idHash];
        
        if (voucher.validFrom == 0) {
            emit VoucherVerified(_idHash, false, block.timestamp);
            return false;
        }

        if (voucher.status != Status.ACTIVE) {
            emit VoucherVerified(_idHash, false, block.timestamp);
            return false;
        }

        if (block.timestamp > voucher.validTo) {
            voucher.status = Status.EXPIRED;
            emit VoucherExpired(_idHash, block.timestamp);
            emit VoucherVerified(_idHash, false, block.timestamp);
            return false;
        }

        emit VoucherVerified(_idHash, true, block.timestamp);
        return true;
    }

    /**
     * @dev Revoke a voucher manually upon trip cancellation or security flag
     */
    function revokeVoucher(bytes32 _idHash, string calldata _reason) external onlyAdmin {
        require(vouchers[_idHash].validFrom != 0, "AEGIS: Voucher does not exist");
        vouchers[_idHash].status = Status.REVOKED;
        emit VoucherRevoked(_idHash, _reason, block.timestamp);
    }
}
