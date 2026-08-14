package com.example.aegis

import android.content.Context
import com.example.aegis.data.local.AegisDatabase
import com.example.aegis.data.remote.OkHttpAegisApi
import com.example.aegis.data.repository.CheckInRepository
import com.example.aegis.data.repository.EmergencyRepository
import com.example.aegis.data.repository.IdentityRepository
import com.example.aegis.data.repository.LocalIdentityRepository
import com.example.aegis.data.repository.RoomCheckInRepository
import com.example.aegis.data.repository.SafetyZoneRepository
import com.example.aegis.data.repository.demo.DemoSafetyZoneRepository
import com.example.aegis.location.AndroidLocationProvider
import com.example.aegis.location.LocationProvider
import com.example.aegis.sensors.ActivityRecognitionProvider
import com.example.aegis.sensors.AndroidActivityRecognitionProvider
import com.example.aegis.data.repository.BlackBoxRepository
import com.example.aegis.service.BreadcrumbSyncScheduler
import com.example.aegis.service.CheckInScheduler
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Manual dependency container. Swap demo repositories for Room/remote-backed
 * implementations here as each stage lands — the UI never sees the swap.
 */
class AppContainer(context: Context) {

  private val appContext = context.applicationContext

  val database: AegisDatabase by lazy { AegisDatabase.getInstance(appContext) }

  // Real OkHttp transport to the AEGIS backend gateway (BuildConfig base URL).
  val aegisApi: OkHttpAegisApi by lazy { OkHttpAegisApi() }

  // Real, Room & Offline Geofence-backed safety zone repository
  val safetyZoneRepository: SafetyZoneRepository by lazy {
    com.example.aegis.data.repository.RoomSafetyZoneRepository(database.zoneDao())
  }
  val emergencyRepository: EmergencyRepository by lazy {
    com.example.aegis.data.repository.RealEmergencyRepository(
      outboxDao = database.outboxDao(),
      api = aegisApi,
      blackBoxRepository = blackBoxRepository,
      appContext = appContext,
      nearbyTransport = nearbyTransport,
    )
  }
  // Real per-install identity: unique tourist ID generated on first launch
  // and registered with the gateway (keccak256 commitment) so incidents link
  // a real idHash and the dashboard no longer shows one ID for every device.
  val identityRepository: IdentityRepository =
    LocalIdentityRepository(appContext, api = aegisApi)

  // Real, Room-backed check-ins.
  val checkInRepository: CheckInRepository by lazy {
    RoomCheckInRepository(checkInDao = database.checkInDao(), identityRepository = identityRepository)
  }

  // Real Offline Geospatial Safety Engine components
  val locationSanityChecker by lazy { com.example.aegis.location.LocationSanityChecker() }
  val offlineGeofenceEngine by lazy { com.example.aegis.safety.OfflineGeofenceEngine() }
  val routeDeviationEngine by lazy { com.example.aegis.safety.RouteDeviationEngine() }
  val safetyCheckInManager by lazy { com.example.aegis.safety.SafetyCheckInManager(checkInRepository) }

  // Real Peer Relay Mesh components
  val packetDeduplicator by lazy { com.example.aegis.mesh.PacketDeduplicator() }
  val relayInbox by lazy { com.example.aegis.mesh.RelayInbox(database.relayInboxDao(), packetDeduplicator) }
  val relayOutbox by lazy { com.example.aegis.mesh.RelayOutbox(relayInboxDao = database.relayInboxDao(), api = aegisApi) }
  val nearbyTransport by lazy { com.example.aegis.mesh.NearbyTransport(appContext, relayInbox) }

  // True while the global SOS overlay is open. Owned by EmergencyViewModel;
  // mirrored here so screens (e.g. Home) can reflect EMERGENCY guardian state.
  val emergencyOverlayActive = MutableStateFlow(false)

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
    BreadcrumbSyncScheduler.schedule(appContext)
  }
}
