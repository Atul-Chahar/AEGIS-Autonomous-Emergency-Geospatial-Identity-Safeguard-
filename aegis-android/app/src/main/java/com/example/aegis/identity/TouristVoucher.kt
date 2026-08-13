package com.example.aegis.identity

data class BlockchainCommitmentRef(
  val chain: String = "Sepolia",
  val chainId: Long = 11155111,
  val contractAddress: String = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e",
  val transactionHash: String? = null,
  val isConfirmed: Boolean = false,
)

data class TouristVoucher(
  val pseudonymousId: String,
  val idHash: String,
  val validFromEpochMillis: Long,
  val validToEpochMillis: Long,
  val tripId: String = "TRIP-2026-MEGHALAYA",
  val issuer: String = "AEGIS Authority Meghalaya",
  val signature: String,
  val blockchainRef: BlockchainCommitmentRef = BlockchainCommitmentRef(),
)
