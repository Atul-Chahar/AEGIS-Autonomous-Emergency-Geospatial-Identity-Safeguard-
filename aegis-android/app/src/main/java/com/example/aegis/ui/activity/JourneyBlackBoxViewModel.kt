package com.example.aegis.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aegis.data.repository.BlackBoxRepository
import com.example.aegis.domain.model.Breadcrumb
import com.example.aegis.domain.model.Trip
import com.example.aegis.ui.state.JourneyActivityEvent
import com.example.aegis.ui.state.JourneyBlackBoxState
import com.example.aegis.ui.state.TimelineEventLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/**
 * Drives the Journey BlackBox screen from REAL Room data only — stored /
 * synced / pending breadcrumb counts, the latest fix, battery and activity
 * are all read from the BlackBox repository. Nothing is fabricated.
 */
class JourneyBlackBoxViewModel(
  private val blackBoxRepository: BlackBoxRepository,
) : ViewModel() {

  private val activeTrip = blackBoxRepository.observeActiveTrip()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  private val latestBreadcrumb = blackBoxRepository.observeLatestBreadcrumb()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  val blackBoxState: StateFlow<JourneyBlackBoxState> =
    combine(activeTrip, latestBreadcrumb) { trip, breadcrumb -> trip to breadcrumb }
      .flatMapLatest { (trip, breadcrumb) ->
        flow {
          val stored =
            if (trip != null) blackBoxRepository.getBreadcrumbsForTrip(trip.tripId).size else 0
          val pending =
            if (trip != null) blackBoxRepository.getUnsyncedBreadcrumbs().size else 0
          emit(buildState(trip, breadcrumb, stored, pending))
        }
      }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        buildState(activeTrip.value, latestBreadcrumb.value, 0, 0),
      )

  private fun buildState(
    trip: Trip?,
    breadcrumb: Breadcrumb?,
    stored: Int,
    pending: Int,
  ): JourneyBlackBoxState {
    val recording = trip != null
    return JourneyBlackBoxState(
      recordingState = if (recording) "Recording" else "Idle",
      lastBreadcrumbTime = breadcrumb?.let { relativeTime(it.timestamp) } ?: "No fix yet",
      locationAccuracy = breadcrumb?.let { "±${it.horizontalAccuracyMeters.toInt()} m" } ?: "—",
      currentActivityType = breadcrumb?.activityMode?.let { it.lowercase().replaceFirstChar { c -> c.uppercase() } } ?: "—",
      battery = breadcrumb?.let { "${it.batteryPercent}%" } ?: "—",
      storedBreadcrumbs = stored,
      syncedBreadcrumbs = (stored - pending).coerceAtLeast(0),
      pendingBreadcrumbs = pending,
      recentEvents = buildRecentEvents(trip, breadcrumb),
    )
  }

  private fun buildRecentEvents(trip: Trip?, breadcrumb: Breadcrumb?): List<JourneyActivityEvent> {
    val events = mutableListOf<JourneyActivityEvent>()
    if (trip != null) {
      events += JourneyActivityEvent(
        title = "Journey started",
        subtitle = "Journey Protection began recording locally on this device.",
        time = clockTime(trip.startedAt),
        level = TimelineEventLevel.NORMAL,
      )
    }
    if (breadcrumb != null) {
      events += JourneyActivityEvent(
        title = "Location recorded",
        subtitle = "Breadcrumb saved to this device (${breadcrumb.latitude}, ${breadcrumb.longitude}).",
        time = clockTime(breadcrumb.timestamp),
        level = TimelineEventLevel.NORMAL,
      )
    }
    return events
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
