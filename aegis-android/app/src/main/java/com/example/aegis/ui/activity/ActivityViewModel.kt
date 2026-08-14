package com.example.aegis.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aegis.data.repository.BlackBoxRepository
import com.example.aegis.data.repository.CheckInRepository
import com.example.aegis.location.LocationResult
import com.example.aegis.location.LocationSanityChecker
import com.example.aegis.safety.GeoPoint
import com.example.aegis.safety.OfflineGeofenceEngine
import com.example.aegis.safety.RouteDeviationEngine
import com.example.aegis.safety.TrekRoute
import com.example.aegis.ui.state.ActivityTimelineState
import com.example.aegis.ui.state.GuardianLevel
import com.example.aegis.ui.state.GuardianSystemState
import com.example.aegis.ui.state.JourneyActivityEvent
import com.example.aegis.ui.state.JourneyBlackBoxState
import com.example.aegis.ui.state.TimelineEventLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/**
 * Drives the Activity timeline from REAL data only: real check-ins (Room),
 * the real trip lifecycle, real breadcrumbs and the real guardian state.
 * The timeline is never seeded with fake events.
 */
class ActivityViewModel(
  private val blackBoxRepository: BlackBoxRepository,
  private val checkInRepository: CheckInRepository,
  private val sanityChecker: LocationSanityChecker = LocationSanityChecker(),
  private val geofenceEngine: OfflineGeofenceEngine = OfflineGeofenceEngine(),
  private val deviationEngine: RouteDeviationEngine = RouteDeviationEngine(),
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

  private val activeTrip = blackBoxRepository.observeActiveTrip()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  private val latestBreadcrumb = blackBoxRepository.observeLatestBreadcrumb()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  private val recentCheckIns = checkInRepository.observeRecentCheckIns()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

  val activityState: StateFlow<ActivityTimelineState> =
    combine(activeTrip, latestBreadcrumb, recentCheckIns, guardianState) { trip, breadcrumb, checkIns, guardian ->
      Triple(trip, breadcrumb to checkIns, guardian)
    }.flatMapLatest { (trip, data, guardian) ->
      flow {
        val stored = trip?.let { blackBoxRepository.getBreadcrumbsForTrip(it.tripId).size } ?: 0
        val pending = trip?.let { blackBoxRepository.getUnsyncedBreadcrumbs().size } ?: 0
        emit(
          ActivityTimelineState(
            guardian = guardian,
            blackBoxSummary = buildBlackBoxSummary(trip, data.first, stored, pending),
            events = buildEvents(trip, data.first, data.second),
          ),
        )
      }
    }.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5_000),
      ActivityTimelineState(
        guardian = GuardianSystemState(GuardianLevel.LIMITED),
        blackBoxSummary =
          JourneyBlackBoxState(
            recordingState = "Idle",
            lastBreadcrumbTime = "No fix yet",
            locationAccuracy = "—",
            currentActivityType = "—",
            battery = "—",
            storedBreadcrumbs = 0,
            syncedBreadcrumbs = 0,
            pendingBreadcrumbs = 0,
            recentEvents = emptyList(),
          ),
        events = emptyList(),
      ),
    )

  private fun buildBlackBoxSummary(
    trip: com.example.aegis.domain.model.Trip?,
    breadcrumb: com.example.aegis.domain.model.Breadcrumb?,
    stored: Int,
    pending: Int,
  ): JourneyBlackBoxState {
    return JourneyBlackBoxState(
      recordingState = if (trip != null) "Recording" else "Idle",
      lastBreadcrumbTime = breadcrumb?.let { relativeTime(it.timestamp) } ?: "No fix yet",
      locationAccuracy = breadcrumb?.let { "±${it.horizontalAccuracyMeters.toInt()} m" } ?: "—",
      currentActivityType = breadcrumb?.activityMode ?: "—",
      battery = breadcrumb?.let { "${it.batteryPercent}%" } ?: "—",
      storedBreadcrumbs = stored,
      syncedBreadcrumbs = (stored - pending).coerceAtLeast(0),
      pendingBreadcrumbs = pending,
      recentEvents = emptyList(),
    )
  }

  private fun buildEvents(
    trip: com.example.aegis.domain.model.Trip?,
    breadcrumb: com.example.aegis.domain.model.Breadcrumb?,
    checkIns: List<com.example.aegis.domain.model.CheckIn>,
  ): List<JourneyActivityEvent> {
    val events = mutableListOf<JourneyActivityEvent>()

    if (trip != null) {
      events += JourneyActivityEvent(
        title = "Journey started",
        subtitle = "Journey Protection began recording locally.",
        time = clockTime(trip.startedAt),
        level = TimelineEventLevel.NORMAL,
      )
    }

    checkIns.take(6).forEach { checkIn ->
      events += JourneyActivityEvent(
        title = "Safety check completed",
        subtitle = "You confirmed you are safe${checkIn.latitude?.let { " (${String.format(Locale.US, "%.4f", it)}, ${String.format(Locale.US, "%.4f", checkIn.longitude ?: 0.0)})" } ?: ""}.",
        time = clockTime(checkIn.timestampEpochMillis),
        level = TimelineEventLevel.NORMAL,
      )
    }

    if (breadcrumb != null) {
      val fix = LocationResult.Success(
        latitude = breadcrumb.latitude,
        longitude = breadcrumb.longitude,
        accuracyMeters = breadcrumb.horizontalAccuracyMeters,
        timestampEpochMillis = breadcrumb.timestamp,
      )
      val dev = deviationEngine.evaluateDeviation(fix, defaultTrekRoute)
      if (dev.isDeviated) {
        events += JourneyActivityEvent(
          title = "Route deviation detected",
          subtitle = String.format(Locale.US, "You are %.0f m away from your planned trail.", dev.effectiveDistanceMeters),
          time = clockTime(breadcrumb.timestamp),
          level = TimelineEventLevel.ATTENTION,
        )
      } else {
        val geofence = geofenceEngine.classifyLocation(breadcrumb.latitude, breadcrumb.longitude)
        val zoneName = geofence.matchedPolygonName ?: "the route corridor"
        events += JourneyActivityEvent(
          title = "Location recorded",
          subtitle = "Latest fix within $zoneName.",
          time = clockTime(breadcrumb.timestamp),
          level = TimelineEventLevel.NORMAL,
        )
      }
    }

    return events.take(12)
  }

  private fun isDeviated(breadcrumb: com.example.aegis.domain.model.Breadcrumb?): Boolean {
    if (breadcrumb == null) return false
    val fix = LocationResult.Success(
      latitude = breadcrumb.latitude,
      longitude = breadcrumb.longitude,
      accuracyMeters = breadcrumb.horizontalAccuracyMeters,
      timestampEpochMillis = breadcrumb.timestamp,
    )
    return deviationEngine.evaluateDeviation(fix, defaultTrekRoute).isDeviated
  }

  private fun relativeTime(timestamp: Long): String {
    val diffMin = (System.currentTimeMillis() - timestamp) / 60_000
    return when {
      diffMin < 1 -> "just now"
      diffMin < 60 -> "$diffMin min ago"
      else -> "${diffMin / 60} hr ${diffMin % 60} min ago"
    }
  }

  private fun clockTime(timestamp: Long): String =
    SimpleDateFormat("HH:mm", Locale.US).format(Date(timestamp))
}
