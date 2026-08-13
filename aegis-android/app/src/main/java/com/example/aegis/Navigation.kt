package com.example.aegis

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.aegis.ui.AegisViewModelFactory
import com.example.aegis.ui.EmergencyViewModel
import com.example.aegis.ui.components.SosOverlay
import com.example.aegis.ui.home.HomeScreen
import com.example.aegis.ui.home.HomeViewModel
import com.example.aegis.ui.id.TouristIdScreen
import com.example.aegis.ui.id.TouristIdViewModel
import com.example.aegis.ui.zone.ZoneDetailScreen
import com.example.aegis.ui.zone.ZoneDetailViewModel
import com.example.aegis.ui.zones.ZonesScreen
import com.example.aegis.ui.zones.ZonesViewModel
import com.example.aegis.ui.zoneDetailViewModelFactory

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Home)

  // Shared across screens: the SOS overlay opens from the raised nav trigger
  // and the zone-detail button, and both must see the same dispatch state.
  val emergencyViewModel: EmergencyViewModel =
    viewModel(factory = AegisViewModelFactory)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Home> {
          val homeViewModel: HomeViewModel = viewModel(factory = AegisViewModelFactory)
          HomeScreen(
            viewModel = homeViewModel,
            onOpenZones = { navigateTo(backStack, Zones) },
            onOpenTouristId = { navigateTo(backStack, TouristId) },
            onOpenZoneDetail = { zoneId -> backStack.add(ZoneDetail(zoneId)) },
            onSos = emergencyViewModel::openOverlay,
          )
        }
        entry<Zones> {
          val zonesViewModel: ZonesViewModel = viewModel(factory = AegisViewModelFactory)
          ZonesScreen(
            viewModel = zonesViewModel,
            onBack = { backStack.removeLastOrNull() },
            onOpenHome = { navigateTo(backStack, Home) },
            onOpenTouristId = { navigateTo(backStack, TouristId) },
            onOpenZoneDetail = { zoneId -> backStack.add(ZoneDetail(zoneId)) },
            onSos = emergencyViewModel::openOverlay,
          )
        }
        entry<TouristId> {
          val touristIdViewModel: TouristIdViewModel = viewModel(factory = AegisViewModelFactory)
          TouristIdScreen(
            viewModel = touristIdViewModel,
            onBack = { backStack.removeLastOrNull() },
            onOpenHome = { navigateTo(backStack, Home) },
            onOpenZones = { navigateTo(backStack, Zones) },
          )
        }
        entry<ZoneDetail> { detail ->
          val zoneDetailViewModel: ZoneDetailViewModel =
            viewModel(factory = zoneDetailViewModelFactory(detail.zoneId))
          ZoneDetailScreen(
            zoneId = detail.zoneId,
            viewModel = zoneDetailViewModel,
            emergencyViewModel = emergencyViewModel,
            onBack = { backStack.removeLastOrNull() },
          )
        }
      },
  )

  // Global SOS overlay: opened from the raised nav trigger on any screen.
  val emergencyState by emergencyViewModel.uiState.collectAsStateWithLifecycle()
  if (emergencyState.overlayVisible) {
    SosOverlay(
      payloadPreview = emergencyState.payloadPreview,
      dispatchResult = emergencyState.dispatchResult,
      onDispatch = { emergencyViewModel.dispatch(zoneId = null, latitude = null, longitude = null) },
      onDismiss = emergencyViewModel::dismissOverlay,
    )
  }
}

/** Tab switch: replace the whole back stack with a single destination. */
private fun navigateTo(backStack: NavBackStack<NavKey>, key: NavKey) {
  backStack.clear()
  backStack.add(key)
}
