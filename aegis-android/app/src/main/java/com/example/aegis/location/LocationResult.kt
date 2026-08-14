package com.example.aegis.location

/** Result of a location request — never fabricates a fix. */
sealed interface LocationResult {
  data class Success(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestampEpochMillis: Long,
    val speedMps: Float? = null,
    val bearingDegrees: Float? = null,
    val altitudeMeters: Double? = null,
    val source: String = "FUSED",
  ) : LocationResult

  data class Unavailable(val reason: String) : LocationResult
}
