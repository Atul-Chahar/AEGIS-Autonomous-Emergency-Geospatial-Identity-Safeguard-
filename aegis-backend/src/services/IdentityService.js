const crypto = require('crypto');
const touristRepository = require('../repositories/TouristRepository');
const ethereumClient = require('../blockchain/ethereumClient');

class IdentityService {
  async registerIdentity({ touristId, salt, itineraryDetails, validDays }) {
    if (!touristId || !salt) {
      throw new Error('MISSING_FIELDS: touristId and salt are required');
    }

    // Canonical Keccak256 commitment: keccak256(touristId + ":" + salt)
    const idHash = ethereumClient.computeCanonicalHash(touristId, salt);
    
    // Register on-chain with Ethers
    const onChainResult = await ethereumClient.registerVoucherOnChain(touristId, salt, itineraryDetails, validDays);

    let itineraryHash = null;
    if (itineraryDetails) {
      itineraryHash = '0x' + crypto.createHash('sha256').update(JSON.stringify(itineraryDetails)).digest('hex');
    }

    const validFrom = new Date();
    const validTo = new Date(Date.now() + (validDays || 7) * 86400000);

    const record = await touristRepository.saveIdentity({
      touristId,
      idHash,
      itineraryHash,
      validFrom: validFrom.toISOString(),
      validTo: validTo.toISOString(),
      status: 'ACTIVE',
      qrPayload: JSON.stringify({
        pseudonymousId: touristId,
        idHash,
        validFrom: validFrom.toISOString(),
        validTo: validTo.toISOString(),
        issuer: "AEGIS Authority Meghalaya",
        blockchainRef: {
          chain: "Sepolia",
          chainId: onChainResult.networkChainId,
          contract: onChainResult.contractAddress,
          txHash: onChainResult.transactionHash
        }
      })
    });

    return {
      success: true,
      touristId: record.touristId,
      idHash: record.idHash,
      validFrom: record.validFrom,
      validTo: record.validTo,
      transactionHash: onChainResult.transactionHash,
      contractAddress: onChainResult.contractAddress,
      networkChainId: onChainResult.networkChainId,
      confirmed: onChainResult.confirmed,
      contractVoucher: record.idHash
    };
  }

  async verifyVoucher(idHash) {
    const found = await touristRepository.findByIdHash(idHash);
    if (!found) {
      return { isValid: false, reason: 'Voucher hash not registered on-chain' };
    }
    const isExpired = new Date(found.validTo) < new Date();
    return {
      isValid: !isExpired && found.status === 'ACTIVE',
      touristId: found.touristId,
      status: found.status,
      validTo: found.validTo
    };
  }
}

module.exports = new IdentityService();
