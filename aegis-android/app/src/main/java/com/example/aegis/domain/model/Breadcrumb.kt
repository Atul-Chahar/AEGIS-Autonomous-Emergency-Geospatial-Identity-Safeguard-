package com.example.aegis.domain.model

data class Breadcrumb(
  val breadcrumbId: String,
  val tripId: String,
  val timestamp: Long,
  val latitude: Double,
  val longitude: Double,
  val horizontalAccuracyMeters: Float = 0f,
  val altitudeMeters: Double? = null,
  val speedMps: Float = 0f,
  val bearingDegrees: Float = 0f,
  val batteryPercent: Int = 100,
  val activityMode: String = "UNKNOWN",
  val locationSource: String = "FUSED",
  val isEstimated: Boolean = false,
  val syncState: String = "PENDING",
)
