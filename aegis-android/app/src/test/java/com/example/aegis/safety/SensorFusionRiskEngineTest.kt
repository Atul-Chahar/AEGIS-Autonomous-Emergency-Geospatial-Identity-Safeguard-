package com.example.aegis.safety

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class SensorFusionRiskEngineTest {

  private lateinit var engine: SensorFusionRiskEngine
  private lateinit var buffer: SensorRingBuffer

  @Before
  fun setup() {
    engine = SensorFusionRiskEngine()
    buffer = SensorRingBuffer(maxAgeMillis = 180_000L)
  }

  @Test
  fun `normal walking trace produces IGNORE action`() {
    val startTime = 100_000L
    // 30s before walking
    for (i in 0 until 30) {
      val t = startTime + i * 1000L
      buffer.addSample(
        SensorSample(
          timestamp = t,
          accelerometer = Vector3(0.2f, 0.5f, 9.8f),
          linearAcceleration = Vector3(0.1f, 0.2f, 0.5f),
          gyroscope = Vector3(0.05f, 0.05f, 0.05f),
          stepCountDelta = 1,
          gpsSpeedMps = 1.2f,
          activityMode = ActivityMode.WALKING,
        )
      )
    }

    val eventSample = SensorSample(
      timestamp = startTime + 30_000L,
      accelerometer = Vector3(0.3f, 0.6f, 10.1f),
      linearAcceleration = Vector3(0.2f, 0.3f, 0.8f),
      gyroscope = Vector3(0.1f, 0.1f, 0.1f),
      stepCountDelta = 1,
      gpsSpeedMps = 1.2f,
      activityMode = ActivityMode.WALKING,
    )
    buffer.addSample(eventSample)

    // 60s after walking
    for (i in 1..60) {
      val t = startTime + 30_000L + i * 1000L
      buffer.addSample(
        SensorSample(
          timestamp = t,
          accelerometer = Vector3(0.2f, 0.5f, 9.8f),
          linearAcceleration = Vector3(0.1f, 0.2f, 0.5f),
          gyroscope = Vector3(0.05f, 0.05f, 0.05f),
          stepCountDelta = 1,
          gpsSpeedMps = 1.2f,
          activityMode = ActivityMode.WALKING,
        )
      )
    }

    val result = engine.analyzeWindow(buffer, eventSample)
    assertEquals(RecommendedAction.IGNORE, result.recommendedAction)
    assertEquals(IncidentType.NONE, result.eventType)
    assertTrue(result.confidence < 0.2f)
  }

  @Test
  fun `running trace produces IGNORE action`() {
    val startTime = 100_000L
    for (i in 0..90) {
      val t = startTime + i * 1000L
      val sample = SensorSample(
        timestamp = t,
        accelerometer = Vector3(1.2f, 2.5f, 9.8f),
        linearAcceleration = Vector3(1.0f, 2.0f, 2.5f),
        gyroscope = Vector3(0.3f, 0.4f, 0.3f),
        stepCountDelta = 2,
        gpsSpeedMps = 3.2f,
        activityMode = ActivityMode.RUNNING,
      )
      buffer.addSample(sample)
    }

    val eventSample = buffer.getAllSamples()[30]
    val result = engine.analyzeWindow(buffer, eventSample)
    assertEquals(RecommendedAction.IGNORE, result.recommendedAction)
    assertEquals(IncidentType.NONE, result.eventType)
  }

  @Test
  fun `phone dropped while user keeps moving is NOT an emergency`() {
    val startTime = 100_000L

    // 30s before: user walking with phone upright (0, 0, 9.8)
    for (i in 0 until 30) {
      buffer.addSample(
        SensorSample(
          timestamp = startTime + i * 1000L,
          accelerometer = Vector3(0.0f, 0.0f, 9.8f),
          linearAcceleration = Vector3(0.1f, 0.1f, 0.2f),
          stepCountDelta = 1,
          gpsSpeedMps = 1.2f,
          activityMode = ActivityMode.WALKING,
        )
      )
    }

    // Impact event: phone dropped on ground (sharp acceleration + orientation tilt flat: 9.8, 0, 0)
    val eventSample = SensorSample(
      timestamp = startTime + 30_000L,
      accelerometer = Vector3(22.0f, 5.0f, 2.0f),
      linearAcceleration = Vector3(20.0f, 4.0f, 1.0f),
      gyroscope = Vector3(4.5f, 3.2f, 2.1f),
      stepCountDelta = 0,
      gpsSpeedMps = 1.2f,
      activityMode = ActivityMode.WALKING,
    )
    buffer.addSample(eventSample)

    // 60s after: user keeps walking without phone (steps detected from user motion/watch/pocket, speed 1.2 m/s)
    for (i in 1..60) {
      buffer.addSample(
        SensorSample(
          timestamp = startTime + 30_000L + i * 1000L,
          accelerometer = Vector3(9.8f, 0.1f, 0.1f), // Phone flat on ground
          linearAcceleration = Vector3(0.1f, 0.1f, 0.1f),
          stepCountDelta = 1,
          gpsSpeedMps = 1.2f,
          activityMode = ActivityMode.WALKING,
        )
      )
    }

    val result = engine.analyzeWindow(buffer, eventSample)
    assertEquals(IncidentType.PHONE_DROPPED, result.eventType)
    assertEquals(RecommendedAction.IGNORE, result.recommendedAction)
    assertTrue("Phone drop must never be emergency", result.recommendedAction != RecommendedAction.EMERGENCY_CANDIDATE)
  }

  @Test
  fun `bus car vibration produces IGNORE action`() {
    val startTime = 100_000L
    val random = Random(42)

    for (i in 0..90) {
      val t = startTime + i * 1000L
      val vibX = random.nextFloat() * 4.0f
      val vibY = random.nextFloat() * 4.0f
      val vibZ = random.nextFloat() * 4.0f
      buffer.addSample(
        SensorSample(
          timestamp = t,
          accelerometer = Vector3(vibX, vibY, 9.8f + vibZ),
          linearAcceleration = Vector3(vibX, vibY, vibZ),
          gyroscope = Vector3(0.1f, 0.1f, 0.1f),
          gpsSpeedMps = 14.0f, // 50 km/h
          activityMode = ActivityMode.IN_VEHICLE,
        )
      )
    }

    val eventSample = buffer.getAllSamples()[30]
    val result = engine.analyzeWindow(buffer, eventSample)
    assertEquals(RecommendedAction.IGNORE, result.recommendedAction)
    assertNotEquals(RecommendedAction.EMERGENCY_CANDIDATE, result.recommendedAction)
  }

  @Test
  fun `speed breaker is classified as SPEED_BUMP and IGNORED`() {
    val startTime = 100_000L

    // 30s before driving smoothly at 11 m/s (~40 km/h)
    for (i in 0 until 30) {
      buffer.addSample(
        SensorSample(
          timestamp = startTime + i * 1000L,
          accelerometer = Vector3(0.0f, 0.0f, 9.8f),
          linearAcceleration = Vector3(0.2f, 0.2f, 0.2f),
          gpsSpeedMps = 11.0f,
          activityMode = ActivityMode.IN_VEHICLE,
        )
      )
    }

    // Speed bump hit: high vertical spike Z = 24.0 m/s², low rotation, vehicle slows slightly
    val eventSample = SensorSample(
      timestamp = startTime + 30_000L,
      accelerometer = Vector3(1.0f, 2.0f, 25.0f),
      linearAcceleration = Vector3(0.5f, 1.0f, 24.0f),
      gyroscope = Vector3(0.2f, 0.2f, 0.1f),
      gpsSpeedMps = 9.0f,
      activityMode = ActivityMode.IN_VEHICLE,
    )
    buffer.addSample(eventSample)

    // 60s after: vehicle continues driving at 11.0 m/s
    for (i in 1..60) {
      buffer.addSample(
        SensorSample(
          timestamp = startTime + 30_000L + i * 1000L,
          accelerometer = Vector3(0.0f, 0.0f, 9.8f),
          linearAcceleration = Vector3(0.2f, 0.2f, 0.2f),
          gpsSpeedMps = 11.0f,
          activityMode = ActivityMode.IN_VEHICLE,
        )
      )
    }

    val result = engine.analyzeWindow(buffer, eventSample)
    assertEquals(IncidentType.SPEED_BUMP, result.eventType)
    assertEquals(RecommendedAction.IGNORE, result.recommendedAction)
    assertTrue("Speed bump must not trigger emergency", result.recommendedAction != RecommendedAction.EMERGENCY_CANDIDATE)
  }

  @Test
  fun `hard braking is classified as HARD_BRAKING and LOGGED`() {
    val startTime = 100_000L

    // 30s before driving fast at 20 m/s (~72 km/h)
    for (i in 0 until 30) {
      buffer.addSample(
        SensorSample(
          timestamp = startTime + i * 1000L,
          accelerometer = Vector3(0.0f, 0.0f, 9.8f),
          linearAcceleration = Vector3(0.2f, 0.2f, 0.2f),
          gpsSpeedMps = 20.0f,
          activityMode = ActivityMode.IN_VEHICLE,
        )
      )
    }

    // Hard braking event: longitudinal deceleration (7 m/s²), low orientation change
    val eventSample = SensorSample(
      timestamp = startTime + 30_000L,
      accelerometer = Vector3(0.0f, 7.5f, 9.8f),
      linearAcceleration = Vector3(0.0f, 7.0f, 0.5f),
      gyroscope = Vector3(0.2f, 0.3f, 0.1f),
      gpsSpeedMps = 12.0f,
      activityMode = ActivityMode.IN_VEHICLE,
    )
    buffer.addSample(eventSample)

    // 60s after: vehicle slows to 4 m/s, then continues moving
    for (i in 1..60) {
      buffer.addSample(
        SensorSample(
          timestamp = startTime + 30_000L + i * 1000L,
          accelerometer = Vector3(0.0f, 0.0f, 9.8f),
          linearAcceleration = Vector3(0.2f, 0.2f, 0.2f),
          gpsSpeedMps = 4.0f,
          activityMode = ActivityMode.IN_VEHICLE,
        )
      )
    }

    val result = engine.analyzeWindow(buffer, eventSample)
    assertEquals(IncidentType.HARD_BRAKING, result.eventType)
    assertEquals(RecommendedAction.LOG, result.recommendedAction)
    assertTrue("Hard braking must not trigger emergency", result.recommendedAction != RecommendedAction.EMERGENCY_CANDIDATE)
  }

  @Test
  fun `possible vehicle crash triggers EMERGENCY_CANDIDATE`() {
    val startTime = 100_000L

    // 30s before driving fast at 22 m/s (~80 km/h)
    for (i in 0 until 30) {
      buffer.addSample(
        SensorSample(
          timestamp = startTime + i * 1000L,
          accelerometer = Vector3(0.0f, 0.0f, 9.8f),
          linearAcceleration = Vector3(0.2f, 0.2f, 0.2f),
          gpsSpeedMps = 22.0f,
          activityMode = ActivityMode.IN_VEHICLE,
        )
      )
    }

    // Violent crash impact: 38 m/s² linear accel + violent rotation (gyro 5.2 rad/s) + orientation tumble
    val eventSample = SensorSample(
      timestamp = startTime + 30_000L,
      accelerometer = Vector3(35.0f, 15.0f, 2.0f),
      linearAcceleration = Vector3(38.0f, 14.0f, 2.0f),
      gyroscope = Vector3(5.2f, 4.1f, 3.8f),
      gpsSpeedMps = 0.0f,
      activityMode = ActivityMode.IN_VEHICLE,
    )
    buffer.addSample(eventSample)

    // 60s after crash: vehicle stopped, 0 speed, 0 steps, prolonged inactivity
    for (i in 1..60) {
      buffer.addSample(
        SensorSample(
          timestamp = startTime + 30_000L + i * 1000L,
          accelerometer = Vector3(9.8f, 0.0f, 0.0f), // Vehicle on side
          linearAcceleration = Vector3(0.0f, 0.0f, 0.0f),
          gyroscope = Vector3(0.0f, 0.0f, 0.0f),
          stepCountDelta = 0,
          gpsSpeedMps = 0.0f,
          activityMode = ActivityMode.IN_VEHICLE,
        )
      )
    }

    val result = engine.analyzeWindow(buffer, eventSample)
    assertEquals(IncidentType.VEHICLE_CRASH_CANDIDATE, result.eventType)
    assertEquals(RecommendedAction.EMERGENCY_CANDIDATE, result.recommendedAction)
    assertTrue("High confidence required for crash", result.confidence >= 0.85f)
  }

  @Test
  fun `walking fall followed by continued movement triggers ASK_USER`() {
    val startTime = 100_000L

    // 30s walking
    for (i in 0 until 30) {
      buffer.addSample(
        SensorSample(
          timestamp = startTime + i * 1000L,
          accelerometer = Vector3(0.0f, 0.0f, 9.8f),
          gpsSpeedMps = 1.2f,
          stepCountDelta = 1,
          activityMode = ActivityMode.WALKING,
        )
      )
    }

    // Fall event: 22 m/s² impact, orientation shift flat (9.8, 0, 0)
    val eventSample = SensorSample(
      timestamp = startTime + 30_000L,
      accelerometer = Vector3(22.0f, 2.0f, 1.0f),
      linearAcceleration = Vector3(20.0f, 2.0f, 1.0f),
      gyroscope = Vector3(3.1f, 2.0f, 1.0f),
      gpsSpeedMps = 0.0f,
      stepCountDelta = 0,
      activityMode = ActivityMode.WALKING,
    )
    buffer.addSample(eventSample)

    // 60s after: user pauses for 3 seconds then resumes walking (steps detected)
    for (i in 1..60) {
      val steps = if (i > 3) 1 else 0
      val speed = if (i > 3) 1.1f else 0.0f
      buffer.addSample(
        SensorSample(
          timestamp = startTime + 30_000L + i * 1000L,
          accelerometer = Vector3(0.0f, 0.0f, 9.8f),
          gpsSpeedMps = speed,
          stepCountDelta = steps,
          activityMode = ActivityMode.WALKING,
        )
      )
    }

    val result = engine.analyzeWindow(buffer, eventSample)
    assertEquals(IncidentType.POTENTIAL_FALL, result.eventType)
    assertEquals(RecommendedAction.ASK_USER, result.recommendedAction)
  }

  @Test
  fun `walking fall followed by prolonged inactivity triggers HIGH_RISK_CHECK`() {
    val startTime = 100_000L

    // 30s walking
    for (i in 0 until 30) {
      buffer.addSample(
        SensorSample(
          timestamp = startTime + i * 1000L,
          accelerometer = Vector3(0.0f, 0.0f, 9.8f),
          gpsSpeedMps = 1.2f,
          stepCountDelta = 1,
          activityMode = ActivityMode.WALKING,
        )
      )
    }

    // Fall event: 24 m/s² impact, orientation tilt
    val eventSample = SensorSample(
      timestamp = startTime + 30_000L,
      accelerometer = Vector3(24.0f, 3.0f, 1.0f),
      linearAcceleration = Vector3(22.0f, 2.0f, 1.0f),
      gyroscope = Vector3(3.8f, 2.5f, 1.2f),
      gpsSpeedMps = 0.0f,
      stepCountDelta = 0,
      activityMode = ActivityMode.WALKING,
    )
    buffer.addSample(eventSample)

    // 60s after: 0 steps, 0 speed (motionless on ground)
    for (i in 1..60) {
      buffer.addSample(
        SensorSample(
          timestamp = startTime + 30_000L + i * 1000L,
          accelerometer = Vector3(9.8f, 0.0f, 0.0f),
          gpsSpeedMps = 0.0f,
          stepCountDelta = 0,
          activityMode = ActivityMode.WALKING,
        )
      )
    }

    val result = engine.analyzeWindow(buffer, eventSample)
    assertEquals(IncidentType.POTENTIAL_FALL, result.eventType)
    assertEquals(RecommendedAction.HIGH_RISK_CHECK, result.recommendedAction)
    assertTrue("High confidence required for fall with inactivity", result.confidence >= 0.80f)
  }

  @Test
  fun `normal vehicle ride over 100 iterations NEVER generates emergency candidates`() {
    val random = Random(12345)
    var emergencyCount = 0

    for (iteration in 1..100) {
      val testBuffer = SensorRingBuffer(maxAgeMillis = 180_000L)
      val startTime = iteration * 200_000L

      // 30s normal vehicle driving with vibrations & minor bumps
      for (i in 0..90) {
        val t = startTime + i * 1000L
        val isBump = i == 30 && (iteration % 5 == 0) // Speed bump every 5th ride
        val isBrake = i == 50 && (iteration % 7 == 0) // Hard brake every 7th ride

        val linAccel = when {
          isBump -> Vector3(0.5f, 1.0f, 22.0f)
          isBrake -> Vector3(0.0f, 6.5f, 0.5f)
          else -> Vector3(random.nextFloat() * 2f, random.nextFloat() * 2f, random.nextFloat() * 2f)
        }

        val speed = when {
          isBrake -> 4.0f
          else -> 12.0f
        }

        val sample = SensorSample(
          timestamp = t,
          accelerometer = Vector3(linAccel.x, linAccel.y, linAccel.z + 9.8f),
          linearAcceleration = linAccel,
          gyroscope = Vector3(0.1f, 0.1f, 0.1f),
          gpsSpeedMps = speed,
          activityMode = ActivityMode.IN_VEHICLE,
        )
        testBuffer.addSample(sample)
      }

      val sampleToAnalyze = testBuffer.getAllSamples()[30]
      val analysis = engine.analyzeWindow(testBuffer, sampleToAnalyze)

      if (analysis.recommendedAction == RecommendedAction.EMERGENCY_CANDIDATE) {
        emergencyCount++
      }
    }

    assertEquals("Normal vehicle ride must generate 0 emergency candidates across 100 iterations", 0, emergencyCount)
  }
}
