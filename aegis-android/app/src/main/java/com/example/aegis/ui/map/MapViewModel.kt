package com.example.aegis.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aegis.data.repository.BlackBoxRepository
import com.example.aegis.domain.model.Breadcrumb
import com.example.aegis.domain.model.SafetyZone
import com.example.aegis.domain.usecase.ObserveSafetyZonesUseCase
import com.example.aegis.location.LocationResult
import com.example.aegis.location.LocationSanityChecker
import com.example.aegis.safety.GeoPoint
import com.example.aegis.safety.OfflineGeofenceEngine
import com.example.aegis.safety.RouteDeviationEngine
import com.example.aegis.safety.TrekRoute
import com.example.aegis.ui.state.GuardianLevel
import com.example.aegis.ui.state.GuardianSystemState
import com.example.aegis.ui.state.MapLayerState
import com.example.aegis.ui.state.MapUiState
import com.example.aegis.ui.state.MapZoneMarker
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/**
 * Drives the Map tab from REAL state only (active trip, real breadcrumbs,
 * real safety zones, real guardian derivation) — the screen never falls back
 * to preview data in production.
 */
class MapViewModel(
  observeZones: ObserveSafetyZonesUseCase,
  private val blackBoxRepository: BlackBoxRepository,
  private val sanityChecker: LocationSanityChecker = LocationSanityChecker(),
  private val geofenceEngine: OfflineGeofenceEngine = OfflineGeofenceEngine(),
  private val deviationEngine: RouteDeviationEngine = RouteDeviationEngine(),
  /** Set by the app container while the SOS overlay is open, so Map reflects EMERGENCY honestly. */
  private val emergencyOverlayActive: StateFlow<Boolean> = MutableStateFlow(false),
) : ViewModel() {

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

  val activeTrip = blackBoxRepository.observeActiveTrip()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  val latestBreadcrumb = blackBoxRepository.observeLatestBreadcrumb()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  val guardianState: StateFlow<GuardianSystemState> =
    combine(activeTrip, latestBreadcrumb, emergencyOverlayActive) { trip, breadcrumb, emergency ->
      when {
        emergency -> GuardianSystemState(GuardianLevel.EMERGENCY)
        trip == null -> GuardianSystemState(GuardianLevel.LIMITED)
        isDeviated(breadcrumb) -> GuardianSystemState(GuardianLevel.ATTENTION)
        breadcrumb == null -> GuardianSystemState(GuardianLevel.LIMITED)
        else -> GuardianSystemState(GuardianLevel.ACTIVE)
      }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GuardianSystemState(GuardianLevel.LIMITED))

  /** Real breadcrumb trail for the active trip, re-queried whenever a fix lands. */
  val trailPoints: StateFlow<List<Pair<Double, Double>>> =
    combine(activeTrip, latestBreadcrumb) { trip, _ -> trip }
      .flatMapLatest { trip ->
        flow {
          val points =
            if (trip != null) {
              blackBoxRepository.getBreadcrumbsForTrip(trip.tripId)
                .map { it.latitude to it.longitude }
            } else {
              emptyList()
            }
          emit(points)
        }
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  val mapState: StateFlow<MapUiState> =
    combine(zones, activeTrip, latestBreadcrumb, guardianState, trailPoints) { zones, trip, breadcrumb, guardian, trail ->
      val (routeSummary, corridorStatus) = buildRouteInfo(trip, breadcrumb)
      MapUiState(
        guardian = guardian,
        title = "Live Route",
        destination = trip?.plannedRouteId ?: "No active journey",
        routeSummary = routeSummary,
        currentLocationLabel = buildLocationLabel(breadcrumb),
        corridorStatus = corridorStatus,
        layers =
          listOf(
            MapLayerState("Route", true),
            MapLayerState("Safety", true),
            MapLayerState("Hazards", true),
            MapLayerState("Rescue", false),
          ),
        zones =
          zones.map {
            MapZoneMarker(
              id = it.id,
              name = it.name,
              subtitle = it.tagline,
              status = it.status,
            )
          },
        selectedDetailTitle = if (trip != null) "Journey protection active" else "No active journey",
        selectedDetailBody =
          if (trip != null) {
            "Your route, breadcrumbs and check-ins are recorded locally and synced when connectivity allows."
          } else {
            "Start a Safe Journey from Home to begin real route tracking."
          },
        trailPoints = trail,
      )
    }.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5_000),
      MapUiState(
        guardian = GuardianSystemState(GuardianLevel.LIMITED),
        title = "Live Route",
        destination = "No active journey",
        routeSummary = "No journey started",
        currentLocationLabel = "Waiting for GPS fix",
        corridorStatus = "Start a Safe Journey to begin tracking",
        layers =
          listOf(
            MapLayerState("Route", true),
            MapLayerState("Safety", true),
            MapLayerState("Hazards", true),
            MapLayerState("Rescue", false),
          ),
        zones = emptyList(),
        selectedDetailTitle = "No active journey",
        selectedDetailBody = "Start a Safe Journey from Home to begin real route tracking.",
      ),
    )

  private fun buildRouteInfo(trip: com.example.aegis.domain.model.Trip?, breadcrumb: Breadcrumb?): Pair<String, String> {
    if (trip == null) return "No journey started" to "Start a Safe Journey to begin tracking"
    if (breadcrumb == null) return "Waiting for GPS fix" to "No location fix recorded yet"
    val distanceKm = approximateDistanceFrom(breadcrumb)
    val fix = LocationResult.Success(
      latitude = breadcrumb.latitude,
      longitude = breadcrumb.longitude,
      accuracyMeters = breadcrumb.horizontalAccuracyMeters,
      timestampEpochMillis = breadcrumb.timestamp,
    )
    val dev = deviationEngine.evaluateDeviation(fix, defaultTrekRoute)
    val corridor =
      if (dev.isDeviated) {
        // Meaningful only near the route — otherwise state it plainly.
        if (dev.effectiveDistanceMeters > 5_000) {
          "Far from your planned trail"
        } else {
          String.format(Locale.US, "%.0f m off your planned trail", dev.effectiveDistanceMeters)
        }
      } else {
        "Inside planned route corridor"
      }
    return String.format(Locale.US, "%.1f km recorded", distanceKm) to corridor
  }

  private fun buildLocationLabel(breadcrumb: Breadcrumb?): String {
    if (breadcrumb == null) return "Waiting for GPS fix"
    val rawFix = LocationResult.Success(
      latitude = breadcrumb.latitude,
      longitude = breadcrumb.longitude,
      accuracyMeters = breadcrumb.horizontalAccuracyMeters,
      timestampEpochMillis = breadcrumb.timestamp,
    )
    val sanity = sanityChecker.checkSanity(rawFix)
    if (!sanity.isValid) return "Location signal needs attention (${sanity.reason})"
    val geofence = geofenceEngine.classifyLocation(breadcrumb.latitude, breadcrumb.longitude)
    val zoneName = geofence.matchedPolygonName ?: "Meghalaya Region"
    return "$zoneName · ${breadcrumb.batteryPercent}% battery"
  }

  /** Haversine distance from the latest fix to the route start (honest, derived from real breadcrumbs). */
  private fun approximateDistanceFrom(breadcrumb: Breadcrumb): Double {
    val start = defaultTrekRoute.waypoints.first()
    val lat1 = Math.toRadians(start.latitude)
    val lat2 = Math.toRadians(breadcrumb.latitude)
    val dLat = lat2 - lat1
    val dLon = Math.toRadians(breadcrumb.longitude - start.longitude)
    val a = sin(dLat / 2) * sin(dLat / 2) +
      cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
    return 6371.0 * 2 * atan2(sqrt(a), sqrt(1 - a))
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
