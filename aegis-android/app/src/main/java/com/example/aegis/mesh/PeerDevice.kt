package com.example.aegis.mesh

data class PeerDevice(
  val endpointId: String,
  val name: String,
  val connectedAtEpochMillis: Long = System.currentTimeMillis(),
  val distanceEstimateMeters: Double? = null,
)
