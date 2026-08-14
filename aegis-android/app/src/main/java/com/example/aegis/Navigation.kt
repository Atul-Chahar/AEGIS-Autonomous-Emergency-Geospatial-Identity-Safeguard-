package com.example.aegis

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.aegis.service.TripTrackingService
import com.example.aegis.ui.AegisViewModelFactory
import com.example.aegis.ui.EmergencyViewModel
import com.example.aegis.ui.activity.ActivityScreen
import com.example.aegis.ui.activity.ActivityViewModel
import com.example.aegis.ui.activity.JourneyBlackBoxScreen
import com.example.aegis.ui.activity.JourneyBlackBoxViewModel
import com.example.aegis.ui.components.SosOverlay
import com.example.aegis.ui.home.HomeScreen
import com.example.aegis.ui.home.HomeViewModel
import com.example.aegis.ui.id.TouristIdScreen
import com.example.aegis.ui.id.TouristIdViewModel
import com.example.aegis.ui.incident.IncidentCheckScreen
import com.example.aegis.ui.map.MapScreen
import com.example.aegis.ui.map.MapViewModel
import com.example.aegis.ui.safety.SafetyCenterScreen
import com.example.aegis.ui.trip.TripSetupScreen
import com.example.aegis.ui.zone.ZoneDetailScreen
import com.example.aegis.ui.zone.ZoneDetailViewModel
import com.example.aegis.ui.zones.ZonesScreen
import com.example.aegis.ui.zones.ZonesViewModel
import com.example.aegis.ui.zoneDetailViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Home)

  // Shared across screens: the SOS overlay opens from the raised nav trigger
  // and the zone-detail button, and both must see the same dispatch state.
  val emergencyViewModel: EmergencyViewModel =
    viewModel(factory = AegisViewModelFactory)

  NavDisplay(
    backStack = backStack,
    onBack = { popBackStack(backStack) },
    entryProvider =
      entryProvider {
        entry<Home> {
          val homeViewModel: HomeViewModel = viewModel(factory = AegisViewModelFactory)
          HomeScreen(
            viewModel = homeViewModel,
            onOpenZones = { navigateTo(backStack, Map) },
            onOpenActivity = { navigateTo(backStack, Activity) },
            onOpenSafetyCenter = { backStack.add(SafetyCenter) },
            onOpenTripSetup = { backStack.add(TripSetup) },
            onOpenTouristId = { navigateTo(backStack, TouristId) },
            onOpenZoneDetail = { zoneId -> backStack.add(ZoneDetail(zoneId)) },
            onSos = emergencyViewModel::openOverlay,
          )
        }
        entry<Zones> {
          val zonesViewModel: ZonesViewModel = viewModel(factory = AegisViewModelFactory)
          ZonesScreen(
            viewModel = zonesViewModel,
            onBack = { popBackStack(backStack) },
            onOpenHome = { navigateTo(backStack, Home) },
            onOpenActivity = { navigateTo(backStack, Activity) },
            onOpenTouristId = { navigateTo(backStack, TouristId) },
            onOpenZoneDetail = { zoneId -> backStack.add(ZoneDetail(zoneId)) },
            onSos = emergencyViewModel::openOverlay,
          )
        }
        entry<Map> {
          val mapViewModel: MapViewModel = viewModel(factory = AegisViewModelFactory)
          val mapState by mapViewModel.mapState.collectAsStateWithLifecycle()
          MapScreen(
            onOpenHome = { navigateTo(backStack, Home) },
            onOpenActivity = { navigateTo(backStack, Activity) },
            onOpenTouristId = { navigateTo(backStack, TouristId) },
            onOpenSafetyCenter = { backStack.add(SafetyCenter) },
            onOpenZoneDetail = { zoneId -> backStack.add(ZoneDetail(zoneId)) },
            onSos = emergencyViewModel::openOverlay,
            state = mapState,
          )
        }
        entry<Activity> {
          val activityViewModel: ActivityViewModel = viewModel(factory = AegisViewModelFactory)
          val activityState by activityViewModel.activityState.collectAsStateWithLifecycle()
          ActivityScreen(
            onOpenHome = { navigateTo(backStack, Home) },
            onOpenMap = { navigateTo(backStack, Map) },
            onOpenTouristId = { navigateTo(backStack, TouristId) },
            onOpenSafetyCenter = { backStack.add(SafetyCenter) },
            onOpenBlackBox = { backStack.add(JourneyBlackBox) },
            onSos = emergencyViewModel::openOverlay,
            state = activityState,
          )
        }
        entry<TripSetup> {
          val context = LocalContext.current
          val scope = rememberCoroutineScope()
          TripSetupScreen(
            onBack = { popBackStack(backStack) },
            onStartJourney = {
              // Start the REAL BlackBox trip: foreground service + peer relay.
              val container = (context.applicationContext as AegisApplication).container
              scope.launch {
                val touristId = container.identityRepository.observeIdentity().first().touristId
                TripTrackingService.start(context, touristId, plannedRouteId = null)
                container.nearbyTransport.startAdvertising()
                container.nearbyTransport.startDiscovery()
              }
              navigateTo(backStack, Home)
            },
          )
        }
        entry<SafetyCenter> {
          SafetyCenterScreen(
            onBack = { popBackStack(backStack) },
            onOpenIncidentCheck = { backStack.add(IncidentCheck) },
          )
        }
        entry<JourneyBlackBox> {
          val blackBoxViewModel: JourneyBlackBoxViewModel = viewModel(factory = AegisViewModelFactory)
          val blackBoxState by blackBoxViewModel.blackBoxState.collectAsStateWithLifecycle()
          JourneyBlackBoxScreen(
            onBack = { popBackStack(backStack) },
            state = blackBoxState,
          )
        }
        entry<IncidentCheck> {
          IncidentCheckScreen(
            onBack = { popBackStack(backStack) },
            onSafe = { popBackStack(backStack) },
            onNeedHelp = emergencyViewModel::openOverlay,
          )
        }
        entry<TouristId> {
          val touristIdViewModel: TouristIdViewModel = viewModel(factory = AegisViewModelFactory)
          TouristIdScreen(
            viewModel = touristIdViewModel,
            onBack = { popBackStack(backStack) },
            onOpenHome = { navigateTo(backStack, Home) },
            onOpenZones = { navigateTo(backStack, Map) },
            onOpenActivity = { navigateTo(backStack, Activity) },
            onSos = emergencyViewModel::openOverlay,
          )
        }
        entry<ZoneDetail> { detail ->
          val zoneDetailViewModel: ZoneDetailViewModel =
            viewModel(factory = zoneDetailViewModelFactory(detail.zoneId))
          ZoneDetailScreen(
            zoneId = detail.zoneId,
            viewModel = zoneDetailViewModel,
            emergencyViewModel = emergencyViewModel,
            onBack = { popBackStack(backStack) },
          )
        }
      },
  )

  // Global SOS overlay: opened from the raised nav trigger on any screen.
  val emergencyState by emergencyViewModel.uiState.collectAsStateWithLifecycle()
  if (emergencyState.overlayVisible) {
    SosOverlay(
      payloadPreview = emergencyState.payloadPreview,
      dispatching = emergencyState.dispatching,
      dispatchResult = emergencyState.dispatchResult,
      hasLocationFix = emergencyState.hasLocationFix,
      blackBoxAttached = emergencyState.blackBoxAttached,
      onDispatch = { emergencyViewModel.dispatch(zoneId = null, latitude = null, longitude = null) },
      onDismiss = emergencyViewModel::dismissOverlay,
    )
  }
}

/**
 * Tab switch: replace the whole back stack with a single destination.
 */
private fun navigateTo(backStack: NavBackStack<NavKey>, key: NavKey) {
  backStack.clear()
  backStack.add(key)
}

/**
 * Pops one destination but never empties the stack — NavDisplay requires at
 * least one entry, and backing out of the last tab should stay on that tab.
 */
private fun popBackStack(backStack: NavBackStack<NavKey>) {
  if (backStack.size > 1) {
    backStack.removeLastOrNull()
  }
}
