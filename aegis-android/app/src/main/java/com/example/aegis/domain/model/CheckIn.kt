package com.example.aegis.domain.model

/** A locally-recorded "I'm safe" / periodic check-in event (offline-first, Room-backed). */
data class CheckIn(
  val id: Long = 0,
  val touristId: String,
  val latitude: Double?,
  val longitude: Double?,
  val timestampEpochMillis: Long,
)
