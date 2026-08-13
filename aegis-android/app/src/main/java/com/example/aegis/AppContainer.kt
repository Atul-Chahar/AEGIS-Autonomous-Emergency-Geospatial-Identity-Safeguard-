package com.example.aegis

import android.content.Context
import com.example.aegis.data.local.AegisDatabase
import com.example.aegis.data.repository.CheckInRepository
import com.example.aegis.data.repository.EmergencyRepository
import com.example.aegis.data.repository.IdentityRepository
import com.example.aegis.data.repository.RoomCheckInRepository
import com.example.aegis.data.repository.SafetyZoneRepository
import com.example.aegis.data.repository.demo.DemoEmergencyRepository
import com.example.aegis.data.repository.demo.DemoIdentityRepository
import com.example.aegis.data.repository.demo.DemoSafetyZoneRepository
import com.example.aegis.location.AndroidLocationProvider
import com.example.aegis.location.LocationProvider
import com.example.aegis.sensors.ActivityRecognitionProvider
import com.example.aegis.sensors.AndroidActivityRecognitionProvider
import com.example.aegis.data.repository.BlackBoxRepository
import com.example.aegis.service.CheckInScheduler

/**
 * Manual dependency container. Swap demo repositories for Room/remote-backed
 * implementations here as each stage lands — the UI never sees the swap.
 */
class AppContainer(context: Context) {

  private val appContext = context.applicationContext

  val database: AegisDatabase by lazy { AegisDatabase.getInstance(appContext) }

  // Real, Room & Offline Geofence-backed safety zone repository
  val safetyZoneRepository: SafetyZoneRepository by lazy {
    com.example.aegis.data.repository.RoomSafetyZoneRepository(database.zoneDao())
  }
  val emergencyRepository: EmergencyRepository by lazy {
    com.example.aegis.data.repository.RealEmergencyRepository(
      outboxDao = database.outboxDao(),
      blackBoxRepository = blackBoxRepository,
    )
  }
  val identityRepository: IdentityRepository = DemoIdentityRepository()

  // Real, Room-backed check-ins.
  val checkInRepository: CheckInRepository by lazy {
    RoomCheckInRepository(checkInDao = database.checkInDao(), identityRepository = identityRepository)
  }

  // Real Offline Geospatial Safety Engine components
  val locationSanityChecker by lazy { com.example.aegis.location.LocationSanityChecker() }
  val offlineGeofenceEngine by lazy { com.example.aegis.safety.OfflineGeofenceEngine() }
  val routeDeviationEngine by lazy { com.example.aegis.safety.RouteDeviationEngine() }
  val safetyCheckInManager by lazy { com.example.aegis.safety.SafetyCheckInManager(checkInRepository) }

  // Real sensors (permission-gated at feature start).
  val locationProvider: LocationProvider = AndroidLocationProvider(appContext)
  val activityRecognitionProvider: ActivityRecognitionProvider =
    AndroidActivityRecognitionProvider(appContext)

  // Real BlackBox repository for offline trip breadcrumb & sensor logging
  val blackBoxRepository: BlackBoxRepository by lazy {
    com.example.aegis.data.repository.RoomBlackBoxRepository(
      tripDao = database.tripDao(),
      breadcrumbDao = database.breadcrumbDao(),
      sensorEventChunkDao = database.sensorEventChunkDao(),
    )
  }

  fun scheduleBackgroundWork() {
    CheckInScheduler.schedule(appContext)
  }
}
