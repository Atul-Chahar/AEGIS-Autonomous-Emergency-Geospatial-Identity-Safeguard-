package com.example.aegis.identity

import org.bouncycastle.jcajce.provider.digest.Keccak
import java.nio.charset.StandardCharsets

/**
 * Canonical Identity Commitment: keccak256(touristId + ":" + salt)
 *
 * Uses Bouncy Castle's KeccakDigest (standards-compliant Keccak-256)
 * to produce identical byte-for-byte outputs matching:
 *   - Ethers.js: ethers.keccak256(ethers.toUtf8Bytes("TST-MEGHALAYA-101:AEGIS-SALT-2026"))
 *   - Solidity: keccak256(abi.encodePacked("TST-MEGHALAYA-101:AEGIS-SALT-2026"))
 */
object CanonicalIdentityHash {

  /**
   * Computes the canonical keccak256 identity commitment hash.
   *
   * @param touristId The pseudonymous tourist identifier (e.g., "TST-MEGHALAYA-101")
   * @param salt The commitment salt (e.g., "AEGIS-SALT-2026")
   * @return The "0x"-prefixed lowercase hex string of the 32-byte keccak256 digest
   */
  fun computeCanonicalHash(touristId: String, salt: String): String {
    val inputString = "$touristId:$salt"
    val bytes = inputString.toByteArray(StandardCharsets.UTF_8)
    val keccakBytes = keccak256(bytes)
    return "0x" + keccakBytes.joinToString("") { "%02x".format(it) }
  }

  private fun keccak256(input: ByteArray): ByteArray {
    val digest = Keccak.Digest256()
    digest.update(input)
    return digest.digest()
  }
}
