package com.example.aegis.safety

import com.example.aegis.data.repository.demo.DemoIdentityRepository
import com.example.aegis.domain.model.ZoneStatus
import com.example.aegis.location.LocationResult
import com.example.aegis.data.repository.CheckInRepository
import com.example.aegis.data.repository.RoomCheckInRepository
import com.example.aegis.data.local.entity.CheckInEntity
import com.example.aegis.data.local.dao.CheckInDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OfflineGeospatialEngineTest {

  private lateinit var geofenceEngine: OfflineGeofenceEngine
  private lateinit var deviationEngine: RouteDeviationEngine

  @Before
  fun setup() {
    geofenceEngine = OfflineGeofenceEngine()
    deviationEngine = RouteDeviationEngine()
  }

  @Test
  fun `point inside Cherrapunji polygon is classified as CAUTION`() {
    val result = geofenceEngine.classifyLocation(latitude = 25.2800, longitude = 91.7000)
    assertEquals("cherrapunji", result.matchedPolygonId)
    assertEquals(ZoneStatus.CAUTION, result.status)
    assertEquals(62, result.riskScore)
  }

  @Test
  fun `point inside Nohkalikai Cliff polygon is classified as HIGH_RISK danger zone`() {
    val result = geofenceEngine.classifyLocation(latitude = 25.2750, longitude = 91.6600)
    assertEquals("nohkalikai", result.matchedPolygonId)
    assertEquals(ZoneStatus.HIGH_RISK, result.status)
    assertEquals(78, result.riskScore)
  }

  @Test
  fun `point inside Living Root Bridges is classified as SAFE zone`() {
    val result = geofenceEngine.classifyLocation(latitude = 25.2500, longitude = 91.6700)
    assertEquals("roots", result.matchedPolygonId)
    assertEquals(ZoneStatus.SAFE, result.status)
    assertEquals(18, result.riskScore)
  }

  @Test
  fun `point outside all polygons is classified as UNKNOWN`() {
    val result = geofenceEngine.classifyLocation(latitude = 28.0000, longitude = 77.0000)
    assertNull(result.matchedPolygonId)
    assertEquals(ZoneStatus.UNKNOWN, result.status)
  }

  @Test
  fun `location on route corridor returns ON_ROUTE`() {
    val route = TrekRoute(
      routeId = "route-1",
      name = "Sohra Trail",
      waypoints = listOf(
        GeoPoint(25.2600, 91.6800),
        GeoPoint(25.2800, 91.7000),
      ),
      corridorWidthMeters = 50.0,
    )

    val fixOnRoute = LocationResult.Success(
      latitude = 25.2700,
      longitude = 91.6900,
      accuracyMeters = 5.0f,
      timestampEpochMillis = 1000L,
    )

    val result = deviationEngine.evaluateDeviation(fixOnRoute, route)
    assertFalse(result.isDeviated)
    assertEquals(DeviationSeverity.ON_ROUTE, result.severity)
  }

  @Test
  fun `location far off corridor returns CRITICAL_DEVIATION`() {
    val route = TrekRoute(
      routeId = "route-1",
      name = "Sohra Trail",
      waypoints = listOf(
        GeoPoint(25.2600, 91.6800),
        GeoPoint(25.2800, 91.7000),
      ),
      corridorWidthMeters = 50.0,
    )

    // Position 2km away from route
    val fixOffRoute = LocationResult.Success(
      latitude = 25.3500,
      longitude = 91.8500,
      accuracyMeters = 5.0f,
      timestampEpochMillis = 1000L,
    )

    val result = deviationEngine.evaluateDeviation(fixOffRoute, route)
    assertTrue(result.isDeviated)
    assertEquals(DeviationSeverity.CRITICAL_DEVIATION, result.severity)
    assertTrue(result.effectiveDistanceMeters > 500.0)
  }

  @Test
  fun `SafetyCheckInManager state machine transitions and confirmation`() = runTest {
    val fakeDao = object : CheckInDao {
      val list = mutableListOf<CheckInEntity>()
      override fun observeRecent(): kotlinx.coroutines.flow.Flow<List<CheckInEntity>> = flowOf(list)
      override fun observeCount(): kotlinx.coroutines.flow.Flow<Int> = flowOf(list.size)
      override suspend fun insert(checkIn: CheckInEntity): Long {
        list.add(checkIn)
        return list.size.toLong()
      }
    }

    val checkInRepo = RoomCheckInRepository(fakeDao, DemoIdentityRepository())
    val manager = SafetyCheckInManager(checkInRepo)

    assertEquals(CheckInState.NORMAL, manager.status.value.state)
    assertFalse("Honest state flag: not claiming guardian notified", manager.status.value.isGuardianNotified)

    // Trigger check-in required & prompt user
    manager.triggerCheckInRequired("Entering Caution Geofence")
    assertEquals(CheckInState.CHECK_REQUIRED, manager.status.value.state)

    manager.promptUser()
    assertEquals(CheckInState.USER_PROMPTED, manager.status.value.state)

    // User clicks "I'm Safe"
    val checkIn = manager.confirmSafe(25.28, 91.70, "cherrapunji", "I'm safe at ridge")
    assertNotNull(checkIn)
    assertEquals(CheckInState.SAFE_CONFIRMED, manager.status.value.state)
    assertEquals(1, fakeDao.list.size)
  }

  @Test
  fun `unanswered check-in timeout escalates to NO_RESPONSE`() = runTest {
    val fakeDao = object : CheckInDao {
      val list = mutableListOf<CheckInEntity>()
      override fun observeRecent(): kotlinx.coroutines.flow.Flow<List<CheckInEntity>> = flowOf(list)
      override fun observeCount(): kotlinx.coroutines.flow.Flow<Int> = flowOf(list.size)
      override suspend fun insert(checkIn: CheckInEntity): Long {
        list.add(checkIn)
        return list.size.toLong()
      }
    }

    val checkInRepo = RoomCheckInRepository(fakeDao, DemoIdentityRepository())
    val manager = SafetyCheckInManager(checkInRepo)

    manager.promptUser()
    assertEquals(CheckInState.USER_PROMPTED, manager.status.value.state)

    // Timeout occurs with no user response
    manager.handleTimeoutNoResponse()
    assertEquals(CheckInState.NO_RESPONSE, manager.status.value.state)
    assertTrue(manager.status.value.promptMessage?.contains("Risk escalation") == true)
  }
}
