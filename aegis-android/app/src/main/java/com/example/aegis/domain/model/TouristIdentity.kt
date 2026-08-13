package com.example.aegis.domain.model

enum class IdentityStatus(val label: String) {
  ACTIVE("Active"),
  EXPIRED("Expired"),
}

/**
 * The tourist's identity voucher. The record is local/offline-first; the
 * on-chain proof (keccak256 voucher) is populated once the identity service
 * registers it — null until then, never fabricated.
 */
data class TouristIdentity(
  val touristId: String,
  val displayName: String,
  val status: IdentityStatus,
  val validFrom: String,
  val validTo: String,
  val onChainHash: String? = null,
)
