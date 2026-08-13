const test = require('node:test');
const assert = require('node:assert/strict');
const ethereumClient = require('../src/blockchain/ethereumClient');
const identityService = require('../src/services/IdentityService');

test('Identity Audit & Canonical Test Vector Verification', async (t) => {
  await t.test('canonical keccak256 hash matches test vector', () => {
    const touristId = "TST-MEGHALAYA-101";
    const salt = "AEGIS-SALT-2026";
    const hash = ethereumClient.computeCanonicalHash(touristId, salt);

    assert.ok(hash.startsWith('0x'));
    assert.equal(hash.length, 66);

    // Recompute directly with ethers
    const expected = require('ethers').keccak256(require('ethers').toUtf8Bytes("TST-MEGHALAYA-101:AEGIS-SALT-2026"));
    assert.equal(hash, expected);
  });

  await t.test('identity registration returns transactionHash, contractAddress, networkChainId, and confirmed status', async () => {
    const res = await identityService.registerIdentity({
      touristId: 'TST-AUDIT-999',
      salt: 'salt-audit',
      validDays: 7
    });

    assert.equal(res.success, true);
    assert.ok(res.transactionHash.startsWith('0x'));
    assert.equal(res.contractAddress, "0x742d35Cc6634C0532925a3b844Bc454e4438f44e");
    assert.equal(res.networkChainId, 11155111);
    assert.equal(res.confirmed, true);
  });
});
