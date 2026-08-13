package com.example.aegis.domain.model

/** A panic/emergency dispatch request. Coordinates and battery are null until wired to real sensors. */
data class SosRequest(
  val touristId: String,
  val zoneId: String? = null,
  val latitude: Double?,
  val longitude: Double?,
  val batteryPct: Int?,
  val timestampEpochMillis: Long,
)
