package com.example.aegis.domain.model

/** A locally-recorded \"I'm safe\" check-in (offline-first, Room-backed). */
data class CheckIn(
  val id: Long,
  val touristId: String,
  val latitude: Double?,
  val longitude: Double?,
  val timestampEpochMillis: Long,
)
