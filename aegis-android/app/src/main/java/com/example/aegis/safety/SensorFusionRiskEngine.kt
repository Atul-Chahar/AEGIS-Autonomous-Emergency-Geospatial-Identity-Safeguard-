package com.example.aegis.safety

import kotlin.math.acos
import kotlin.math.sqrt

class SensorFusionRiskEngine {

  fun analyzeWindow(buffer: SensorRingBuffer, eventSample: SensorSample): IncidentAnalysis {
    val eventTime = eventSample.timestamp
    val before = buffer.getBeforeWindow(eventTime, 30_000L)
    val after = buffer.getAfterWindow(eventTime, 60_000L)

    // 1. Resolve activity mode (with fallback if UNKNOWN)
    val resolvedMode = resolveActivityMode(eventSample, before)

    // 2. Compute event peak metrics
    val peakLinearAccel = calculatePeakLinearAccel(eventSample, before, after)
    val peakGyro = calculatePeakGyro(eventSample, before, after)
    val orientationChange = calculateOrientationChange(eventSample, before, after)

    // 3. Compute speed & movement metrics
    val beforeAvgSpeed = calculateAvgSpeed(before) ?: eventSample.gpsSpeedMps ?: 0f
    val afterAvgSpeed = calculateAvgSpeed(after) ?: 0f
    val speedDrop = (beforeAvgSpeed - afterAvgSpeed).coerceAtLeast(0f)

    val totalPostSteps = after.sumOf { it.stepCountDelta }
    val prolongedInactivity = isProlongedInactivity(after)

    val contributingFactors = mutableListOf<String>()
    contributingFactors.add("Resolved Mode: $resolvedMode")
    contributingFactors.add(String.format("Peak Linear Accel: %.2f m/s²", peakLinearAccel))
    contributingFactors.add(String.format("Peak Gyro: %.2f rad/s", peakGyro))
    contributingFactors.add(String.format("Orientation Change: %.1f°", orientationChange))
    contributingFactors.add(String.format("Speed Drop: %.1f m/s (%.1f -> %.1f)", speedDrop, beforeAvgSpeed, afterAvgSpeed))
    contributingFactors.add("Post-event Steps: $totalPostSteps")
    contributingFactors.add("Prolonged Inactivity: $prolongedInactivity")

    // 4. Low impact threshold guard
    if (peakLinearAccel < 14.0f && speedDrop < 4.0f) {
      return IncidentAnalysis(
        eventType = IncidentType.NONE,
        confidence = 0.0f,
        contributingFactors = contributingFactors,
        recommendedAction = RecommendedAction.IGNORE,
      )
    }

    // 5. Context-based State Machine evaluation
    return when (resolvedMode) {
      ActivityMode.IN_VEHICLE -> evaluateVehicleContext(
        peakLinearAccel = peakLinearAccel,
        peakGyro = peakGyro,
        orientationChange = orientationChange,
        speedDrop = speedDrop,
        afterAvgSpeed = afterAvgSpeed,
        prolongedInactivity = prolongedInactivity,
        contributingFactors = contributingFactors,
      )

      ActivityMode.WALKING, ActivityMode.RUNNING, ActivityMode.ON_BICYCLE, ActivityMode.STILL, ActivityMode.UNKNOWN -> evaluatePedestrianContext(
        mode = resolvedMode,
        peakLinearAccel = peakLinearAccel,
        peakGyro = peakGyro,
        orientationChange = orientationChange,
        speedDrop = speedDrop,
        afterAvgSpeed = afterAvgSpeed,
        totalPostSteps = totalPostSteps,
        prolongedInactivity = prolongedInactivity,
        contributingFactors = contributingFactors,
      )
    }
  }

  private fun evaluateVehicleContext(
    peakLinearAccel: Float,
    peakGyro: Float,
    orientationChange: Float,
    speedDrop: Float,
    afterAvgSpeed: Float,
    prolongedInactivity: Boolean,
    contributingFactors: List<String>,
  ): IncidentAnalysis {
    // Rule: Vehicle speed bump / road vibration
    // High vertical impact spike, but vehicle maintains speed, low tumble/rotation
    if (orientationChange < 25.0f && speedDrop < 3.5f && (afterAvgSpeed > 3.0f || !prolongedInactivity)) {
      return IncidentAnalysis(
        eventType = IncidentType.SPEED_BUMP,
        confidence = 0.15f,
        contributingFactors = contributingFactors + "Rule: Speed bump / road vibration detected — low orientation shift and vehicle continued moving.",
        recommendedAction = RecommendedAction.IGNORE,
      )
    }

    // Rule: Hard Braking
    // Deceleration without tumbling angular velocity or post-crash inactivity
    if (speedDrop >= 4.5f && peakGyro < 2.5f && orientationChange < 25.0f && !prolongedInactivity) {
      return IncidentAnalysis(
        eventType = IncidentType.HARD_BRAKING,
        confidence = 0.25f,
        contributingFactors = contributingFactors + "Rule: Hard braking detected — deceleration without structural tumble or post-impact inactivity.",
        recommendedAction = RecommendedAction.LOG,
      )
    }

    // Rule: Vehicle Crash Candidate
    // Requires combination: high linear impact + violent orientation tilt/gyro + major speed drop + post-event inactivity
    val isSevereImpact = peakLinearAccel >= 30.0f || speedDrop >= 6.5f
    val isTumbleOrRotation = orientationChange >= 30.0f || peakGyro >= 3.0f
    val isStoppedAfter = afterAvgSpeed < 1.5f && prolongedInactivity

    if (isSevereImpact && isTumbleOrRotation && isStoppedAfter) {
      return IncidentAnalysis(
        eventType = IncidentType.VEHICLE_CRASH_CANDIDATE,
        confidence = 0.90f,
        contributingFactors = contributingFactors + "Rule: Vehicle crash candidate — high impact, violent rotation, major speed drop, and post-event inactivity.",
        recommendedAction = RecommendedAction.EMERGENCY_CANDIDATE,
      )
    }

    // Fallback for moderate vehicle disturbance
    return IncidentAnalysis(
      eventType = IncidentType.NONE,
      confidence = 0.10f,
      contributingFactors = contributingFactors + "Rule: Moderate vehicle motion within normal driving boundaries.",
      recommendedAction = RecommendedAction.IGNORE,
    )
  }

  private fun evaluatePedestrianContext(
    mode: ActivityMode,
    peakLinearAccel: Float,
    peakGyro: Float,
    orientationChange: Float,
    speedDrop: Float,
    afterAvgSpeed: Float,
    totalPostSteps: Int,
    prolongedInactivity: Boolean,
    contributingFactors: List<String>,
  ): IncidentAnalysis {
    // Rule: Phone Dropped while user keeps moving
    // High impact + orientation change, but user continues walking (post-event steps detected or speed continues)
    if (peakLinearAccel >= 16.0f && orientationChange >= 25.0f && (totalPostSteps >= 3 || afterAvgSpeed > 0.6f)) {
      return IncidentAnalysis(
        eventType = IncidentType.PHONE_DROPPED,
        confidence = 0.15f,
        contributingFactors = contributingFactors + "Rule: Phone dropped while user keeps walking — post-event step activity detected.",
        recommendedAction = RecommendedAction.IGNORE,
      )
    }

    // Rule: Walking Fall followed by continued movement
    if (peakLinearAccel >= 16.0f && orientationChange >= 25.0f && totalPostSteps in 1..2) {
      return IncidentAnalysis(
        eventType = IncidentType.POTENTIAL_FALL,
        confidence = 0.40f,
        contributingFactors = contributingFactors + "Rule: Fall candidate with movement recovery — user resumed motion after initial pause.",
        recommendedAction = RecommendedAction.ASK_USER,
      )
    }

    // Rule: Walking Fall followed by prolonged inactivity
    if (peakLinearAccel >= 16.0f && orientationChange >= 25.0f && totalPostSteps == 0 && prolongedInactivity) {
      return IncidentAnalysis(
        eventType = IncidentType.POTENTIAL_FALL,
        confidence = 0.85f,
        contributingFactors = contributingFactors + "Rule: High-confidence walking fall — impact, orientation tilt, and 0 post-event steps.",
        recommendedAction = RecommendedAction.HIGH_RISK_CHECK,
      )
    }

    return IncidentAnalysis(
      eventType = IncidentType.NONE,
      confidence = 0.05f,
      contributingFactors = contributingFactors + "Rule: Normal pedestrian activity.",
      recommendedAction = RecommendedAction.IGNORE,
    )
  }

  private fun resolveActivityMode(sample: SensorSample, before: List<SensorSample>): ActivityMode {
    if (sample.activityMode != ActivityMode.UNKNOWN) return sample.activityMode

    val recentMode = before.lastOrNull { it.activityMode != ActivityMode.UNKNOWN }?.activityMode
    if (recentMode != null) return recentMode

    // Speed-based fallback
    val speed = sample.gpsSpeedMps ?: calculateAvgSpeed(before) ?: 0f
    return when {
      speed >= 4.5f -> ActivityMode.IN_VEHICLE
      speed >= 1.8f -> ActivityMode.RUNNING
      speed >= 0.4f -> ActivityMode.WALKING
      else -> ActivityMode.STILL
    }
  }

  private fun calculatePeakLinearAccel(sample: SensorSample, before: List<SensorSample>, after: List<SensorSample>): Float {
    val allSamples = before + sample + after
    return allSamples.mapNotNull { s ->
      s.linearAcceleration?.magnitude() ?: s.accelerometer?.let { acc ->
        // Subtract gravity ~9.81 m/s² estimate
        (acc.magnitude() - 9.81f).coerceAtLeast(0f)
      }
    }.maxOrNull() ?: 0f
  }

  private fun calculatePeakGyro(sample: SensorSample, before: List<SensorSample>, after: List<SensorSample>): Float {
    val allSamples = before + sample + after
    return allSamples.mapNotNull { it.gyroscope?.magnitude() }.maxOrNull() ?: 0f
  }

  private fun calculateOrientationChange(sample: SensorSample, before: List<SensorSample>, after: List<SensorSample>): Float {
    val beforeVector = calculateAvgAccelVector(before)
    val afterVector = calculateAvgAccelVector(after.take(5))

    if (beforeVector == null || afterVector == null) return 0f

    val magB = beforeVector.magnitude()
    val magA = afterVector.magnitude()
    if (magB == 0f || magA == 0f) return 0f

    val dot = beforeVector.x * afterVector.x + beforeVector.y * afterVector.y + beforeVector.z * afterVector.z
    val cosTheta = (dot / (magB * magA)).coerceIn(-1f, 1f)
    val radians = acos(cosTheta)
    return Math.toDegrees(radians.toDouble()).toFloat()
  }

  private fun calculateAvgAccelVector(samples: List<SensorSample>): Vector3? {
    val accels = samples.mapNotNull { it.accelerometer ?: it.linearAcceleration }
    if (accels.isEmpty()) return null
    val sumX = accels.sumOf { it.x.toDouble() }.toFloat()
    val sumY = accels.sumOf { it.y.toDouble() }.toFloat()
    val sumZ = accels.sumOf { it.z.toDouble() }.toFloat()
    return Vector3(sumX / accels.size, sumY / accels.size, sumZ / accels.size)
  }

  private fun calculateAvgSpeed(samples: List<SensorSample>): Float? {
    val speeds = samples.mapNotNull { it.gpsSpeedMps }
    if (speeds.isEmpty()) return null
    return speeds.average().toFloat()
  }

  private fun isProlongedInactivity(after: List<SensorSample>): Boolean {
    if (after.isEmpty()) return false
    val totalSteps = after.sumOf { it.stepCountDelta }
    val avgSpeed = calculateAvgSpeed(after) ?: 0f
    return totalSteps == 0 && avgSpeed < 0.5f
  }
}
