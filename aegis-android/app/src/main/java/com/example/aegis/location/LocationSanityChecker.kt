package com.example.aegis.location

import com.example.aegis.safety.ActivityMode
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class SanityReason {
  VALID,
  ACCURACY_TOO_LOW,
  IMPOSSIBLE_SPEED,
  TELEPORT_SPIKE,
  ACTIVITY_MISMATCH,
}

data class SanityCheckResult(
  val isValid: Boolean,
  val confidenceWeight: Float, // 0.0 to 1.0
  val reason: SanityReason,
  val sanitizedLocation: LocationResult.Success?,
)

class LocationSanityChecker(
  private val maxAllowedAccuracyMeters: Float = 75.0f,
  private val maxVehicleSpeedMps: Float = 50.0f, // 180 km/h
  private val maxPedestrianSpeedMps: Float = 10.0f, // ~36 km/h
) {
  private val history = mutableListOf<LocationResult.Success>()
  private val maxHistorySize = 10

  @Synchronized
  fun checkSanity(
    rawFix: LocationResult.Success,
    currentActivityMode: ActivityMode = ActivityMode.UNKNOWN,
  ): SanityCheckResult {
    // 1. Filter low accuracy
    if (rawFix.accuracyMeters > maxAllowedAccuracyMeters) {
      return SanityCheckResult(
        isValid = false,
        confidenceWeight = 0.2f,
        reason = SanityReason.ACCURACY_TOO_LOW,
        sanitizedLocation = null,
      )
    }

    val lastValid = history.lastOrNull()
    if (lastValid == null) {
      history.add(rawFix)
      return SanityCheckResult(
        isValid = true,
        confidenceWeight = 1.0f,
        reason = SanityReason.VALID,
        sanitizedLocation = rawFix,
      )
    }

    val deltaMillis = rawFix.timestampEpochMillis - lastValid.timestampEpochMillis
    val deltaSeconds = (deltaMillis / 1000.0).coerceAtLeast(0.1)

    val distanceMeters = haversineDistanceMeters(
      lastValid.latitude, lastValid.longitude,
      rawFix.latitude, rawFix.longitude
    )

    val calculatedSpeedMps = (distanceMeters / deltaSeconds).toFloat()

    // 2. Filter impossible speed
    val maxSpeed = if (currentActivityMode == ActivityMode.WALKING || currentActivityMode == ActivityMode.RUNNING) {
      maxPedestrianSpeedMps
    } else {
      maxVehicleSpeedMps
    }

    if (calculatedSpeedMps > maxSpeed && distanceMeters > 100.0) {
      return SanityCheckResult(
        isValid = false,
        confidenceWeight = 0.1f,
        reason = SanityReason.IMPOSSIBLE_SPEED,
        sanitizedLocation = null,
      )
    }

    // 3. Teleport & Return Spike Check
    // If lastFix jumped far away, but rawFix returns close to lastValid-1
    if (history.size >= 2) {
      val prevValid = history[history.size - 2]
      val distFromPrev = haversineDistanceMeters(
        prevValid.latitude, prevValid.longitude,
        rawFix.latitude, rawFix.longitude
      )
      val prevDistFromLast = haversineDistanceMeters(
        prevValid.latitude, prevValid.longitude,
        lastLastValid().latitude, lastLastValid().longitude
      )

      if (distanceMeters > 500.0 && distFromPrev < 100.0 && prevDistFromLast > 500.0) {
        // Last fix was a teleport spike! Discard last and keep current
        history.removeAt(history.size - 1)
        history.add(rawFix)
        return SanityCheckResult(
          isValid = true,
          confidenceWeight = 0.8f,
          reason = SanityReason.TELEPORT_SPIKE,
          sanitizedLocation = rawFix,
        )
      }
    }

    history.add(rawFix)
    if (history.size > maxHistorySize) {
      history.removeAt(0)
    }

    return SanityCheckResult(
      isValid = true,
      confidenceWeight = 1.0f,
      reason = SanityReason.VALID,
      sanitizedLocation = rawFix,
    )
  }

  private fun lastLastValid(): LocationResult.Success = history[history.size - 1]

  @Synchronized
  fun clearHistory() {
    history.clear()
  }

  companion object {
    fun haversineDistanceMeters(
      lat1: Double, lon1: Double,
      lat2: Double, lon2: Double
    ): Double {
      val r = 6371000.0 // Earth radius in meters
      val dLat = Math.toRadians(lat2 - lat1)
      val dLon = Math.toRadians(lon2 - lon1)
      val a = sin(dLat / 2) * sin(dLat / 2) +
          cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
          sin(dLon / 2) * sin(dLon / 2)
      val c = 2 * atan2(sqrt(a), sqrt(1 - a))
      return r * c
    }
  }
}
