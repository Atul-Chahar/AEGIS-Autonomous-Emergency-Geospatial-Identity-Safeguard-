package com.example.aegis.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aegis.data.repository.BlackBoxRepository
import com.example.aegis.domain.model.Breadcrumb
import com.example.aegis.domain.model.SafetyZone
import com.example.aegis.domain.model.TouristIdentity
import com.example.aegis.domain.model.Trip
import com.example.aegis.domain.usecase.GetTouristIdentityUseCase
import com.example.aegis.domain.usecase.ObserveSafetyZonesUseCase
import com.example.aegis.location.LocationResult
import com.example.aegis.location.LocationSanityChecker
import com.example.aegis.mesh.NearbyTransport
import com.example.aegis.safety.GeoPoint
import com.example.aegis.safety.OfflineGeofenceEngine
import com.example.aegis.safety.RouteDeviationEngine
import com.example.aegis.safety.SafetyCheckInManager
import com.example.aegis.safety.TrekRoute
import com.example.aegis.service.TripTrackingService
import com.example.aegis.ui.state.GuardianLevel
import com.example.aegis.ui.state.GuardianSystemState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

class HomeViewModel(
  observeZones: ObserveSafetyZonesUseCase,
  observeIdentity: GetTouristIdentityUseCase,
  private val blackBoxRepository: BlackBoxRepository,
  private val sanityChecker: LocationSanityChecker = LocationSanityChecker(),
  private val geofenceEngine: OfflineGeofenceEngine = OfflineGeofenceEngine(),
  private val deviationEngine: RouteDeviationEngine = RouteDeviationEngine(),
  val checkInManager: SafetyCheckInManager? = null,
  val nearbyTransport: NearbyTransport? = null,
  /** Set by the app container while the SOS overlay is open, so Home reflects EMERGENCY honestly. */
  private val emergencyOverlayActive: StateFlow<Boolean> = MutableStateFlow(false),
) : ViewModel() {

  // Reference route for deviation evaluation (same one surfaced in routeDeviationText).
  private val defaultTrekRoute =
    TrekRoute(
      routeId = "cherrapunji-ridge",
      name = "Cherrapunji Ridge Trail",
      waypoints = listOf(
        GeoPoint(25.2600, 91.6800),
        GeoPoint(25.2800, 91.7000),
        GeoPoint(25.3000, 91.7500),
      ),
      corridorWidthMeters = 50.0,
    )

  val zones: StateFlow<List<SafetyZone>> =
    observeZones().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  val activePeerCount: StateFlow<Int> =
    nearbyTransport?.activePeers?.map { it.size }
      ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
      ?: MutableStateFlow(0)

  val isMeshActive: StateFlow<Boolean> =
    if (nearbyTransport != null) {
      combine(nearbyTransport.isAdvertising, nearbyTransport.isDiscovering, nearbyTransport.activePeers) { adv, disc, peers ->
        adv || disc || peers.isNotEmpty()
      }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    } else {
      MutableStateFlow(false)
    }

  val featuredZone: StateFlow<SafetyZone?> =
    combine(zones, activePeerCount) { list, peers ->
      val base = list.firstOrNull() ?: return@combine null
      base.copy(peers = peers)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  val identity: StateFlow<TouristIdentity?> =
    observeIdentity()
      .map<TouristIdentity, TouristIdentity?> { it }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  val activeTrip: StateFlow<Trip?> =
    blackBoxRepository.observeActiveTrip()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  val isTrackingActive: StateFlow<Boolean> =
    activeTrip.map { it != null }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

  val latestBreadcrumb: StateFlow<Breadcrumb?> =
    blackBoxRepository.observeLatestBreadcrumb()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  /**
   * Real guardian status derived from actual state (no fake levels):
   *  - SOS overlay open            → EMERGENCY
   *  - no active trip              → LIMITED (protection ready, journey not started)
   *  - trip + route deviation      → ATTENTION
   *  - trip + no location fix yet  → LIMITED (tracking with offline/mesh only)
   *  - trip + valid fix, on route  → ACTIVE
   */
  val guardianState: StateFlow<GuardianSystemState> =
    combine(isTrackingActive, latestBreadcrumb, emergencyOverlayActive) { tracking, breadcrumb, emergency ->
      when {
        emergency -> GuardianSystemState(GuardianLevel.EMERGENCY)
        !tracking -> GuardianSystemState(GuardianLevel.LIMITED)
        isDeviated(breadcrumb) -> GuardianSystemState(GuardianLevel.ATTENTION)
        breadcrumb == null -> GuardianSystemState(GuardianLevel.LIMITED)
        else -> GuardianSystemState(GuardianLevel.ACTIVE)
      }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GuardianSystemState(GuardianLevel.LIMITED))

  val locationText: StateFlow<String> =
    latestBreadcrumb.map { breadcrumb ->
      if (breadcrumb != null) {
        val rawFix = LocationResult.Success(
          latitude = breadcrumb.latitude,
          longitude = breadcrumb.longitude,
          accuracyMeters = breadcrumb.horizontalAccuracyMeters,
          timestampEpochMillis = breadcrumb.timestamp,
        )
        val sanity = sanityChecker.checkSanity(rawFix)
        if (sanity.isValid) {
          val geofence = geofenceEngine.classifyLocation(breadcrumb.latitude, breadcrumb.longitude)
          val zoneName = geofence.matchedPolygonName ?: "Meghalaya Region"
          "$zoneName - Location protection active - ${breadcrumb.batteryPercent}% battery"
        } else {
          "Location signal needs attention (${sanity.reason})"
        }
      } else {
        "Location protection ready"
      }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Location protection ready")

  val routeDeviationText: StateFlow<String> =
    latestBreadcrumb.map { breadcrumb ->
      if (breadcrumb == null) return@map "Route guidance ready"
      val fix = LocationResult.Success(
        latitude = breadcrumb.latitude,
        longitude = breadcrumb.longitude,
        accuracyMeters = breadcrumb.horizontalAccuracyMeters,
        timestampEpochMillis = breadcrumb.timestamp,
      )
      val dev = deviationEngine.evaluateDeviation(fix, defaultTrekRoute)
      if (dev.isDeviated) {
        // Raw meter counts are only meaningful close to the route; far away
        // (e.g. testing from another city) we state it plainly instead of
        // printing a meaningless "39512 m".
        if (dev.effectiveDistanceMeters > 5_000) {
          "You are far from your planned trail."
        } else {
          String.format(Locale.US, "You are %.0f m away from your planned trail.", dev.effectiveDistanceMeters)
        }
      } else {
        "You are on your planned trail."
      }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Route guidance ready")

  val touristName: String
    get() = identity.value?.displayName ?: "Tourist"

  fun startRoute(context: Context, plannedRouteId: String? = null) {
    val touristId = identity.value?.touristId ?: "TST-DEFAULT"
    TripTrackingService.start(context, touristId, plannedRouteId)
    nearbyTransport?.startAdvertising()
    nearbyTransport?.startDiscovery()
  }

  fun stopRoute(context: Context) {
    TripTrackingService.stop(context)
    nearbyTransport?.stopAll()
  }

  private fun isDeviated(breadcrumb: Breadcrumb?): Boolean {
    if (breadcrumb == null) return false
    val fix = LocationResult.Success(
      latitude = breadcrumb.latitude,
      longitude = breadcrumb.longitude,
      accuracyMeters = breadcrumb.horizontalAccuracyMeters,
      timestampEpochMillis = breadcrumb.timestamp,
    )
    return deviationEngine.evaluateDeviation(fix, defaultTrekRoute).isDeviated
  }
}
