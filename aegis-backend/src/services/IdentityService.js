const crypto = require('crypto');
const touristRepository = require('../repositories/TouristRepository');

class IdentityService {
  async registerIdentity({ touristId, salt, itineraryDetails, validDays }) {
    if (!touristId || !salt) {
      throw new Error('MISSING_FIELDS: touristId and salt are required');
    }

    // Zero-Knowledge Cryptographic Hash Voucher: keccak256 / sha256(touristId + salt)
    const idHash = '0x' + crypto.createHash('sha256').update(`${touristId}:${salt}`).digest('hex');
    
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
      qrPayload: `AEGIS-ID:${idHash}`
    });

    return {
      success: true,
      touristId: record.touristId,
      idHash: record.idHash,
      validFrom: record.validFrom,
      validTo: record.validTo,
      contractVoucher: record.idHash
    };
  }

  async verifyVoucher(idHash) {
    const found = await touristRepository.findByIdHash(idHash);
    if (!found) {
      return { isValid: false, reason: 'Voucher not registered on-chain' };
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
