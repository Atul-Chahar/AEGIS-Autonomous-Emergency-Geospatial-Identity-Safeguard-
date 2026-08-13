package com.example.aegis.domain.model

/** A panic/emergency dispatch request. */
data class SosRequest(
  val touristId: String,
  val zoneId: String? = null,
  val latitude: Double? = null,
  val longitude: Double? = null,
  val batteryPct: Int? = null,
  val timestampEpochMillis: Long = System.currentTimeMillis(),
)
