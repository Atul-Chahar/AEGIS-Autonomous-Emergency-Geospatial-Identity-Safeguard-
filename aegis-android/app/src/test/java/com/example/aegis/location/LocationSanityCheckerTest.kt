package com.example.aegis.location

import com.example.aegis.safety.ActivityMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocationSanityCheckerTest {

  private lateinit var checker: LocationSanityChecker

  @Before
  fun setup() {
    checker = LocationSanityChecker()
  }

  @Test
  fun `good location fix passes sanity check`() {
    val fix = LocationResult.Success(
      latitude = 25.2742,
      longitude = 91.6964,
      accuracyMeters = 8.0f,
      timestampEpochMillis = 1000L,
    )

    val result = checker.checkSanity(fix)
    assertTrue(result.isValid)
    assertEquals(SanityReason.VALID, result.reason)
    assertNotNull(result.sanitizedLocation)
  }

  @Test
  fun `low accuracy fix above 75m is rejected`() {
    val fix = LocationResult.Success(
      latitude = 25.2742,
      longitude = 91.6964,
      accuracyMeters = 120.0f,
      timestampEpochMillis = 1000L,
    )

    val result = checker.checkSanity(fix)
    assertFalse(result.isValid)
    assertEquals(SanityReason.ACCURACY_TOO_LOW, result.reason)
  }

  @Test
  fun `impossible speed is rejected`() {
    val fix1 = LocationResult.Success(
      latitude = 25.2742,
      longitude = 91.6964,
      accuracyMeters = 5.0f,
      timestampEpochMillis = 1000L,
    )
    checker.checkSanity(fix1)

    // 20km away in 1 second (20,000 m/s)
    val fix2 = LocationResult.Success(
      latitude = 25.5000,
      longitude = 92.0000,
      accuracyMeters = 5.0f,
      timestampEpochMillis = 2000L,
    )

    val result = checker.checkSanity(fix2)
    assertFalse(result.isValid)
    assertEquals(SanityReason.IMPOSSIBLE_SPEED, result.reason)
  }

  @Test
  fun `pedestrian speed mismatch is rejected`() {
    val fix1 = LocationResult.Success(
      latitude = 25.2742,
      longitude = 91.6964,
      accuracyMeters = 5.0f,
      timestampEpochMillis = 1000L,
    )
    checker.checkSanity(fix1, ActivityMode.WALKING)

    // 1 km away in 2 seconds (500 m/s) while WALKING
    val fix2 = LocationResult.Success(
      latitude = 25.2830,
      longitude = 91.6964,
      accuracyMeters = 5.0f,
      timestampEpochMillis = 3000L,
    )

    val result = checker.checkSanity(fix2, ActivityMode.WALKING)
    assertFalse(result.isValid)
    assertEquals(SanityReason.IMPOSSIBLE_SPEED, result.reason)
  }

  @Test
  fun `single teleport spike is detected and handled without emergency`() {
    val fix0 = LocationResult.Success(latitude = 25.2700, longitude = 91.6900, accuracyMeters = 5.0f, timestampEpochMillis = 1000L)
    val fix1Teleport = LocationResult.Success(latitude = 25.9500, longitude = 92.5500, accuracyMeters = 5.0f, timestampEpochMillis = 2000L)
    val fix2Return = LocationResult.Success(latitude = 25.2705, longitude = 91.6905, accuracyMeters = 5.0f, timestampEpochMillis = 3000L)

    checker.checkSanity(fix0)
    val result1 = checker.checkSanity(fix1Teleport)
    assertFalse("Teleport spike 1 must be rejected", result1.isValid)

    val result2 = checker.checkSanity(fix2Return)
    assertTrue("Return to cluster must be valid", result2.isValid)
    assertEquals(SanityReason.TELEPORT_SPIKE, result2.reason)
  }
}
