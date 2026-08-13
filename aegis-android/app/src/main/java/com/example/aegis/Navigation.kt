package com.example.aegis

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.aegis.ui.home.HomeScreen
import com.example.aegis.ui.id.TouristIdScreen
import com.example.aegis.ui.zone.ZoneDetailScreen
import com.example.aegis.ui.zones.ZonesScreen

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
            onOpenZones = { navigateTo(backStack, Zones) },
            onOpenTouristId = { navigateTo(backStack, TouristId) },
            onOpenZoneDetail = { zoneId -> backStack.add(ZoneDetail(zoneId)) },
          )
        }
        entry<Zones> {
          ZonesScreen(
            onBack = { backStack.removeLastOrNull() },
            onOpenHome = { navigateTo(backStack, Home) },
            onOpenTouristId = { navigateTo(backStack, TouristId) },
            onOpenZoneDetail = { zoneId -> backStack.add(ZoneDetail(zoneId)) },
          )
        }
        entry<TouristId> {
          TouristIdScreen(
            onBack = { backStack.removeLastOrNull() },
            onOpenHome = { navigateTo(backStack, Home) },
            onOpenZones = { navigateTo(backStack, Zones) },
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
