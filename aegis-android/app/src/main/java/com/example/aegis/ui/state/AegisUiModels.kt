package com.example.aegis.ui.state

import androidx.annotation.DrawableRes
import com.example.aegis.data.ZoneStatus

enum class GuardianLevel(
  val title: String,
  val subtitle: String,
) {
  ACTIVE("Guardian Active", "All protection systems working"),
  LIMITED("Guardian Limited", "Offline protection active"),
  ATTENTION("Guardian Attention", "Safety condition requires attention"),
  EMERGENCY("Emergency Mode", "Rescue workflow active"),
}

data class GuardianSystemState(
  val level: GuardianLevel,
)

enum class JourneyMode {
  PRE_TRIP,
  ACTIVE_TRIP,
}

data class JourneyProtectionItem(
  val label: String,
  val value: String,
  val detail: String,
  val level: GuardianLevel = GuardianLevel.ACTIVE,
)

data class JourneyHomeState(
  val mode: JourneyMode,
  val greetingName: String,
  val guardian: GuardianSystemState,
  val destination: String,
  val riskLabel: String,
  val expectedDuration: String,
  val offlineReadiness: String,
  val journeyDuration: String,
  val journeyDistance: String,
  val recordingStatus: String,
  val checkInCountdown: String,
  val nearestHazard: String,
  val routeDeviation: RouteDeviationState?,
  val protectionItems: List<JourneyProtectionItem>,
)

data class TripSetupState(
  val destination: String,
  val plannedRoute: String,
  val expectedReturnTime: String,
  val emergencyContact: String,
  val offlineMapReadiness: String,
  val blackBoxReadiness: String,
  val checkInInterval: String,
)

data class MapLayerState(
  val label: String,
  val enabled: Boolean,
)

data class MapZoneMarker(
  val name: String,
  val subtitle: String,
  val status: ZoneStatus,
)

data class MapUiState(
  val title: String,
  val destination: String,
  val routeSummary: String,
  val currentLocationLabel: String,
  val corridorStatus: String,
  val layers: List<MapLayerState>,
  val zones: List<MapZoneMarker>,
  val selectedDetailTitle: String,
  val selectedDetailBody: String,
)

enum class TimelineEventLevel {
  NORMAL,
  CAUTION,
  ATTENTION,
  EMERGENCY,
}

data class JourneyActivityEvent(
  val title: String,
  val subtitle: String,
  val time: String,
  val level: TimelineEventLevel,
)

data class ActivityTimelineState(
  val guardian: GuardianSystemState,
  val blackBoxSummary: JourneyBlackBoxState,
  val events: List<JourneyActivityEvent>,
)

data class JourneyBlackBoxState(
  val recordingState: String,
  val lastBreadcrumbTime: String,
  val locationAccuracy: String,
  val currentActivityType: String,
  val battery: String,
  val storedBreadcrumbs: Int,
  val syncedBreadcrumbs: Int,
  val pendingBreadcrumbs: Int,
  val recentEvents: List<JourneyActivityEvent>,
)

enum class RouteDeviationSeverity {
  MINOR,
  SIGNIFICANT,
  HIGH_RISK,
}

data class RouteDeviationState(
  val severity: RouteDeviationSeverity,
  val distanceMeters: Int,
) {
  val message: String
    get() =
      when (severity) {
        RouteDeviationSeverity.MINOR -> "You are slightly off route."
        RouteDeviationSeverity.SIGNIFICANT -> "You are $distanceMeters m away from your planned trail."
        RouteDeviationSeverity.HIGH_RISK -> "You are off route near a high-risk area."
      }
}

enum class IncidentType(
  val title: String,
) {
  POSSIBLE_FALL("Possible fall detected"),
  POSSIBLE_CRASH("Possible vehicle crash detected"),
  UNUSUAL_IMPACT("Unusual impact detected"),
}

data class IncidentCheckState(
  val type: IncidentType,
  val countdownSeconds: Int,
  val totalSeconds: Int,
)

enum class SosStepStatus {
  PENDING,
  IN_PROGRESS,
  SUCCEEDED,
  FAILED,
}

val SosStepStatus.showsSuccess: Boolean
  get() = this == SosStepStatus.SUCCEEDED

data class SosProgressStep(
  val label: String,
  val status: SosStepStatus,
)

data class SosFlowState(
  val title: String,
  val message: String,
  val offlineMessage: String?,
  val holdProgress: Float,
  val steps: List<SosProgressStep>,
)

data class DigitalIdUiState(
  val touristId: String,
  val status: String,
  val validity: String,
  val privacyExplanation: String,
  val hash: String,
  val contractAddress: String,
  val network: String,
)

data class SafetyCenterRow(
  val label: String,
  val value: String,
  val detail: String,
  val level: GuardianLevel = GuardianLevel.ACTIVE,
)

data class SafetyCenterSection(
  val title: String,
  val rows: List<SafetyCenterRow>,
)

data class SafetyCenterState(
  val guardian: GuardianSystemState,
  val sections: List<SafetyCenterSection>,
)

data class PhotoZoneCardState(
  val id: String,
  val name: String,
  val tagline: String,
  val region: String,
  @param:DrawableRes val imageRes: Int,
  val status: ZoneStatus,
  val riskScore: Int,
  val duration: String,
  val elevation: String,
)
