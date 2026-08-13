package com.example.aegis.ui.state

import com.example.aegis.R
import com.example.aegis.domain.model.SafetyZone
import com.example.aegis.domain.model.ZoneStatus

object AegisSampleState {
  private const val touristName = "Ananya"
  private const val touristId = "AEGIS-TST-7429"
  private const val validity = "12 Aug - 20 Aug"
  private const val touristHash = "0x8f14e45fceea167a5a36dedd4bea2543"
  private const val contractAddress = "Pending issuance"
  private const val network = "MVP identity service"
  private val previewZones =
    listOf(
      SafetyZone(
        id = "cherrapunji",
        name = "Cherrapunji Ridge",
        tagline = "Mist-heavy ridge trail with active caution sections.",
        description = "Preview route zone prepared for offline safety layers.",
        region = "Meghalaya",
        status = ZoneStatus.CAUTION,
        riskScore = 62,
        dates = "12 - 20 Aug",
        duration = "4 hr 30 min",
        elevation = "1,965 m",
        peers = 2,
      ),
      SafetyZone(
        id = "roots",
        name = "Living Root Bridges",
        tagline = "Forest bridge trail with stable route coverage.",
        description = "Preview safe zone prepared for route and rescue layers.",
        region = "Meghalaya",
        status = ZoneStatus.SAFE,
        riskScore = 18,
        dates = "12 - 20 Aug",
        duration = "3 hr",
        elevation = "740 m",
        peers = 4,
      ),
      SafetyZone(
        id = "dawki",
        name = "Dawki River",
        tagline = "River-side route with rescue post visibility.",
        description = "Preview safe route prepared for hazard overlays.",
        region = "Meghalaya",
        status = ZoneStatus.SAFE,
        riskScore = 24,
        dates = "12 - 20 Aug",
        duration = "1 day",
        elevation = "420 m",
        peers = 1,
      ),
      SafetyZone(
        id = "nohkalikai",
        name = "Nohkalikai Falls",
        tagline = "Cliff-side viewpoint with high-risk boundaries.",
        description = "Preview high-risk route prepared for caution messaging.",
        region = "Meghalaya",
        status = ZoneStatus.HIGH_RISK,
        riskScore = 78,
        dates = "12 - 20 Aug",
        duration = "2 hr",
        elevation = "1,120 m",
        peers = 0,
      ),
    )

  private val guardianActive = GuardianSystemState(GuardianLevel.ACTIVE)
  private val guardianLimited = GuardianSystemState(GuardianLevel.LIMITED)

  val protectionItems =
    listOf(
      JourneyProtectionItem("Location", "Active", "Location protection is ready for the current journey."),
      JourneyProtectionItem("Journey Log", "Recording", "Journey Protection is saving local route events."),
      JourneyProtectionItem("Offline Relay", "Available", "Nearby relay support may help if internet is unavailable."),
      JourneyProtectionItem("Check-in", "24 min", "The next safety check is scheduled soon."),
    )

  val homePreTrip =
    JourneyHomeState(
      mode = JourneyMode.PRE_TRIP,
      greetingName = touristName,
      guardian = guardianLimited,
      destination = "Cherrapunji Ridge",
      riskLabel = "Caution",
      expectedDuration = "4 hr 30 min",
      offlineReadiness = "Offline map ready",
      journeyDuration = "Not started",
      journeyDistance = "0 km",
      recordingStatus = "Ready",
      checkInCountdown = "30 min",
      nearestHazard = "Heavy rain watch near ridge path",
      routeDeviation = null,
      protectionItems = protectionItems,
    )

  val homeActiveTrip =
    JourneyHomeState(
      mode = JourneyMode.ACTIVE_TRIP,
      greetingName = touristName,
      guardian = GuardianSystemState(GuardianLevel.ATTENTION),
      destination = "Cherrapunji Ridge",
      riskLabel = "Caution",
      expectedDuration = "4 hr 30 min",
      offlineReadiness = "Offline protection active",
      journeyDuration = "1 hr 18 min",
      journeyDistance = "3.6 km",
      recordingStatus = "Journey Log recording",
      checkInCountdown = "24 min",
      nearestHazard = "Slippery trail section 420 m ahead",
      routeDeviation = RouteDeviationState(RouteDeviationSeverity.SIGNIFICANT, 180),
      protectionItems = protectionItems,
    )

  val tripSetup =
    TripSetupState(
      destination = "Cherrapunji Ridge",
      plannedRoute = "Mawsmai trail loop",
      expectedReturnTime = "18:30 IST",
      emergencyContact = "Authority contact on file",
      offlineMapReadiness = "Downloaded",
      blackBoxReadiness = "Ready to record",
      checkInInterval = "Every 30 min",
    )

  val map =
    MapUiState(
      title = "Live Route",
      destination = "Cherrapunji Ridge",
      routeSummary = "3.6 km recorded of 8.4 km planned",
      currentLocationLabel = "Near Mawsmai trail",
      corridorStatus = "Inside planned route corridor",
      layers =
        listOf(
          MapLayerState("Route", true),
          MapLayerState("Safety", true),
          MapLayerState("Hazards", true),
          MapLayerState("Rescue", false),
        ),
      zones =
        previewZones.map {
          MapZoneMarker(
            name = it.name,
            subtitle = it.tagline,
            status = it.status,
          )
        },
      selectedDetailTitle = "Mawsmai trail section",
      selectedDetailBody = "Caution zone nearby. Stay on the marked route and check in before the next ridge bend.",
    )

  val blackBox =
    JourneyBlackBoxState(
      recordingState = "Recording",
      lastBreadcrumbTime = "2 min ago",
      locationAccuracy = "Good",
      currentActivityType = "Walking",
      battery = "82%",
      storedBreadcrumbs = 148,
      syncedBreadcrumbs = 96,
      pendingBreadcrumbs = 52,
      recentEvents =
        listOf(
          JourneyActivityEvent("Safety check completed", "You confirmed you are safe.", "12:04", TimelineEventLevel.NORMAL),
          JourneyActivityEvent("Entered caution zone", "Slippery path section nearby.", "11:42", TimelineEventLevel.CAUTION),
          JourneyActivityEvent("Offline relay available", "Nearby relay support detected.", "11:18", TimelineEventLevel.NORMAL),
        ),
    )

  val activity =
    ActivityTimelineState(
      guardian = GuardianSystemState(GuardianLevel.ATTENTION),
      blackBoxSummary = blackBox,
      events =
        listOf(
          JourneyActivityEvent("Trip started", "Journey Protection began recording locally.", "10:46", TimelineEventLevel.NORMAL),
          JourneyActivityEvent("Location recorded", "Breadcrumb saved to this device.", "10:52", TimelineEventLevel.NORMAL),
          JourneyActivityEvent("Safety check completed", "You confirmed you are safe.", "11:05", TimelineEventLevel.NORMAL),
          JourneyActivityEvent("Entered caution zone", "Slippery path section nearby.", "11:42", TimelineEventLevel.CAUTION),
          JourneyActivityEvent("Route deviation detected", "You are 180 m away from your planned trail.", "12:08", TimelineEventLevel.ATTENTION),
          JourneyActivityEvent("Returned to route", "Route corridor status restored.", "12:19", TimelineEventLevel.NORMAL),
        ),
    )

  val incidentCheck =
    IncidentCheckState(
      type = IncidentType.POSSIBLE_FALL,
      countdownSeconds = 18,
      totalSeconds = 30,
    )

  val sosOfflineSearching =
    SosFlowState(
      title = "Emergency SOS",
      message = "Press and hold to start emergency sharing.",
      offlineMessage = "No internet. Your emergency has been safely stored. Searching for a nearby relay.",
      holdProgress = 0f,
      steps =
        listOf(
          SosProgressStep("Emergency recorded", SosStepStatus.SUCCEEDED),
          SosProgressStep("Location locked", SosStepStatus.IN_PROGRESS),
          SosProgressStep("Journey BlackBox attached", SosStepStatus.PENDING),
          SosProgressStep("Sending via internet", SosStepStatus.FAILED),
          SosProgressStep("SMS fallback", SosStepStatus.PENDING),
          SosProgressStep("Searching for offline relay", SosStepStatus.IN_PROGRESS),
          SosProgressStep("Authority received", SosStepStatus.PENDING),
        ),
    )

  val digitalId =
    DigitalIdUiState(
      touristId = touristId,
      status = "Active",
      validity = validity,
      privacyExplanation = "Only a privacy-preserving voucher is used for verification. Raw passport, Aadhaar, and phone details are not stored on-chain.",
      hash = touristHash,
      contractAddress = contractAddress,
      network = network,
    )

  val safetyCenter =
    SafetyCenterState(
      guardian = GuardianSystemState(GuardianLevel.ATTENTION),
      sections =
        listOf(
          SafetyCenterSection(
            "Journey Protection",
            listOf(
              SafetyCenterRow("Journey BlackBox", "Recording", "Local route events are being saved."),
              SafetyCenterRow("Location protection", "Active", "Location state is available to the journey UI."),
              SafetyCenterRow("Motion detection", "Ready", "Incident checks can be triggered by future sensors."),
            ),
          ),
          SafetyCenterSection(
            "Emergency",
            listOf(
              SafetyCenterRow("Emergency contacts", "On file", "Authority and trusted contact details are prepared."),
              SafetyCenterRow("Medical details", "Private", "Only shown when you choose to share them."),
            ),
          ),
          SafetyCenterSection(
            "Privacy",
            listOf(
              SafetyCenterRow("Data expiry", "Trip end", "Journey data can expire after the safety window."),
              SafetyCenterRow("Digital ID", "Pseudonymous", "Verification uses a privacy-preserving tourist ID."),
            ),
          ),
          SafetyCenterSection(
            "Offline",
            listOf(
              SafetyCenterRow("Offline maps", "Ready", "Map data is prepared for low-connectivity use."),
              SafetyCenterRow("Pending transmissions", "52 items", "Stored events can sync when connectivity returns.", GuardianLevel.LIMITED),
              SafetyCenterRow("Offline relay", "Available", "Nearby relay support may help deliver emergency messages.", GuardianLevel.LIMITED),
            ),
          ),
        ),
    )

  val photoZones =
    previewZones.map {
      PhotoZoneCardState(
        id = it.id,
        name = it.name,
        tagline = it.tagline,
        region = it.region,
        imageRes =
          when (it.id) {
            "roots" -> R.drawable.zone_roots
            "dawki" -> R.drawable.zone_river
            "nohkalikai" -> R.drawable.zone_valley
            else -> R.drawable.zone_cherrapunji
          },
        status = it.status,
        riskScore = it.riskScore,
        duration = it.duration,
        elevation = it.elevation,
      )
    }

  val highRiskDeviation = RouteDeviationState(RouteDeviationSeverity.HIGH_RISK, 260)
  val safeStatus = ZoneStatus.SAFE
}
