const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("AegisTouristID Smart Contract", function () {
  let aegisContract;
  let admin, user1, user2;

  // Shared Canonical Identity Hash Test Vector
  // keccak256("TST-MEGHALAYA-101:AEGIS-SALT-2026")
  const testTouristId = "TST-MEGHALAYA-101";
  const testSalt = "AEGIS-SALT-2026";
  const canonicalInput = `${testTouristId}:${testSalt}`;
  const canonicalIdHash = ethers.keccak256(ethers.toUtf8Bytes(canonicalInput));
  const itineraryHash = ethers.keccak256(ethers.toUtf8Bytes("ITINERARY-SHILLONG-2026"));

  beforeEach(async function () {
    [admin, user1, user2] = await ethers.getSigners();
    const AegisFactory = await ethers.getContractFactory("AegisTouristID");
    aegisContract = await AegisFactory.deploy();
    await aegisContract.waitForDeployment();
  });

  it("canonical test vector produces expected keccak256 hash", function () {
    expect(canonicalIdHash).to.be.a("string");
    expect(canonicalIdHash.length).to.equal(66); // 0x + 64 hex chars
  });

  it("register: successfully registers a new tourist ID voucher and emits event", async function () {
    const validDays = 7;
    await expect(aegisContract.connect(user1).registerTripVoucher(canonicalIdHash, itineraryHash, validDays))
      .to.emit(aegisContract, "VoucherRegistered");

    const voucher = await aegisContract.vouchers(canonicalIdHash);
    expect(voucher.idHash).to.equal(canonicalIdHash);
    expect(voucher.status).to.equal(0); // Status.ACTIVE
  });

  it("duplicate registration: reverts when registering same idHash twice", async function () {
    await aegisContract.connect(user1).registerTripVoucher(canonicalIdHash, itineraryHash, 7);

    await expect(
      aegisContract.connect(user1).registerTripVoucher(canonicalIdHash, itineraryHash, 7)
    ).to.be.revertedWith("AEGIS: Voucher hash already registered");
  });

  it("validity: verifyVoucher returns true for active non-expired voucher", async function () {
    await aegisContract.connect(user1).registerTripVoucher(canonicalIdHash, itineraryHash, 7);

    const isValid = await aegisContract.verifyVoucher.staticCall(canonicalIdHash);
    expect(isValid).to.equal(true);
  });

  it("expiry: verifyVoucher returns false and updates status when timestamp exceeds validTo", async function () {
    await aegisContract.connect(user1).registerTripVoucher(canonicalIdHash, itineraryHash, 1); // 1 day

    // Increase block timestamp by 2 days
    await ethers.provider.send("evm_increaseTime", [2 * 86400]);
    await ethers.provider.send("evm_mine");

    const isValid = await aegisContract.verifyVoucher.staticCall(canonicalIdHash);
    expect(isValid).to.equal(false);

    // Call verifyVoucher to trigger status update to EXPIRED
    await aegisContract.verifyVoucher(canonicalIdHash);

    const voucher = await aegisContract.vouchers(canonicalIdHash);
    expect(voucher.status).to.equal(1); // Status.EXPIRED
  });

  it("revocation: admin can revoke a registered voucher", async function () {
    await aegisContract.connect(user1).registerTripVoucher(canonicalIdHash, itineraryHash, 7);

    await expect(aegisContract.connect(admin).revokeVoucher(canonicalIdHash, "Trip cancelled by user"))
      .to.emit(aegisContract, "VoucherRevoked");

    const voucher = await aegisContract.vouchers(canonicalIdHash);
    expect(voucher.status).to.equal(2); // Status.REVOKED

    const isValid = await aegisContract.verifyVoucher.staticCall(canonicalIdHash);
    expect(isValid).to.equal(false);
  });

  it("unauthorized revocation: non-admin call to revokeVoucher reverts", async function () {
    await aegisContract.connect(user1).registerTripVoucher(canonicalIdHash, itineraryHash, 7);

    await expect(
      aegisContract.connect(user1).revokeVoucher(canonicalIdHash, "Unauthorized attempt")
    ).to.be.revertedWith("AEGIS: Only admin authority can execute");
  });
});
