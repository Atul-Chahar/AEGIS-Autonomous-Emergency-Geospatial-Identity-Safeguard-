package com.example.aegis

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.aegis.ui.activity.ActivityScreen
import com.example.aegis.ui.activity.JourneyBlackBoxScreen
import com.example.aegis.ui.home.HomeScreen
import com.example.aegis.ui.id.TouristIdScreen
import com.example.aegis.ui.incident.IncidentCheckScreen
import com.example.aegis.ui.map.MapScreen
import com.example.aegis.ui.safety.SafetyCenterScreen
import com.example.aegis.ui.trip.TripSetupScreen
import com.example.aegis.ui.zone.ZoneDetailScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Home)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Home> {
          HomeScreen(
            onOpenMap = { navigateTo(backStack, Map) },
            onOpenActivity = { navigateTo(backStack, Activity) },
            onOpenTouristId = { navigateTo(backStack, TouristId) },
            onOpenTripSetup = { backStack.add(TripSetup) },
            onOpenSafetyCenter = { backStack.add(SafetyCenter) },
            onOpenZoneDetail = { zoneId -> backStack.add(ZoneDetail(zoneId)) },
          )
        }
        entry<Map> {
          MapScreen(
            onOpenHome = { navigateTo(backStack, Home) },
            onOpenActivity = { navigateTo(backStack, Activity) },
            onOpenTouristId = { navigateTo(backStack, TouristId) },
            onOpenSafetyCenter = { backStack.add(SafetyCenter) },
            onOpenZoneDetail = { zoneId -> backStack.add(ZoneDetail(zoneId)) },
          )
        }
        entry<Activity> {
          ActivityScreen(
            onOpenHome = { navigateTo(backStack, Home) },
            onOpenMap = { navigateTo(backStack, Map) },
            onOpenTouristId = { navigateTo(backStack, TouristId) },
            onOpenSafetyCenter = { backStack.add(SafetyCenter) },
            onOpenBlackBox = { backStack.add(JourneyBlackBox) },
          )
        }
        entry<TouristId> {
          TouristIdScreen(
            onOpenHome = { navigateTo(backStack, Home) },
            onOpenMap = { navigateTo(backStack, Map) },
            onOpenActivity = { navigateTo(backStack, Activity) },
          )
        }
        entry<TripSetup> {
          TripSetupScreen(
            onBack = { backStack.removeLastOrNull() },
            onStartJourney = { navigateTo(backStack, Home) },
          )
        }
        entry<SafetyCenter> { SafetyCenterScreen(onBack = { backStack.removeLastOrNull() }) }
        entry<JourneyBlackBox> { JourneyBlackBoxScreen(onBack = { backStack.removeLastOrNull() }) }
        entry<IncidentCheck> {
          IncidentCheckScreen(
            onBack = { backStack.removeLastOrNull() },
            onSafe = { backStack.removeLastOrNull() },
            onNeedHelp = { backStack.removeLastOrNull() },
          )
        }
        entry<ZoneDetail> { detail ->
          ZoneDetailScreen(
            zoneId = detail.zoneId,
            onBack = { backStack.removeLastOrNull() },
          )
        }
      },
  )
}

/** Tab switch: replace the whole back stack with a single destination. */
private fun navigateTo(backStack: NavBackStack<NavKey>, key: NavKey) {
  backStack.clear()
  backStack.add(key)
}
