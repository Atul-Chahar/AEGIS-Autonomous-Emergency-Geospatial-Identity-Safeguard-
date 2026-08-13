package com.example.aegis.ui.activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.example.aegis.Activity
import com.example.aegis.Home
import com.example.aegis.TouristId
import com.example.aegis.Zones
import com.example.aegis.theme.CautionAmber
import com.example.aegis.theme.ForestDark
import com.example.aegis.theme.GlassBorder
import com.example.aegis.theme.GlassSurface
import com.example.aegis.theme.Ink
import com.example.aegis.theme.InkSoft
import com.example.aegis.theme.SafeGreen
import com.example.aegis.ui.components.AegisBackground
import com.example.aegis.ui.components.AegisBottomNavScaffold
import com.example.aegis.ui.components.BottomNavItem
import com.example.aegis.ui.components.GlassCard
import com.example.aegis.ui.components.SosOverlay
import com.example.aegis.ui.home.HomeViewModel

@Composable
fun ActivityScreen(
  viewModel: HomeViewModel,
  onOpenHome: () -> Unit,
  onOpenZones: () -> Unit,
  onOpenTouristId: () -> Unit,
  onSos: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isTracking by viewModel.isTrackingActive.collectAsStateWithLifecycle()
  val locationText by viewModel.locationText.collectAsStateWithLifecycle()
  val routeGuidance by viewModel.routeDeviationText.collectAsStateWithLifecycle()
  val activePeerCount by viewModel.activePeerCount.collectAsStateWithLifecycle()
  val latestBreadcrumb by viewModel.latestBreadcrumb.collectAsStateWithLifecycle()
  val navItems =
    listOf(
      BottomNavItem("Home", Icons.Filled.Home, Home),
      BottomNavItem("Map", Icons.Filled.Place, Zones),
      BottomNavItem("Activity", Icons.Filled.Notifications, Activity),
      BottomNavItem("ID", Icons.Filled.Person, TouristId),
    )

  AegisBackground(modifier = modifier) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .statusBarsPadding()
          .padding(horizontal = 20.dp)
          .padding(bottom = 150.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      Text(text = "Activity", style = MaterialTheme.typography.displayMedium, color = Ink)
      Text(text = "Journey events from local protection systems.", style = MaterialTheme.typography.bodyMedium, color = InkSoft)

      GlassCard(shape = RoundedCornerShape(28.dp)) {
        Text(text = "Journey Protection", style = MaterialTheme.typography.headlineSmall, color = Ink)
        Text(
          text = if (isTracking) "Recording this journey locally" else "Ready to record your next journey",
          style = MaterialTheme.typography.bodyMedium,
          color = if (isTracking) SafeGreen else InkSoft,
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TimelineRow("Location", locationText, if (latestBreadcrumb != null) "Updated recently" else "Waiting")
        TimelineRow("Route", routeGuidance, if (routeGuidance.contains("away")) "Attention" else "Normal")
        TimelineRow("Offline Relay", if (activePeerCount > 0) "$activePeerCount nearby support device(s)" else "No nearby relay right now", if (activePeerCount > 0) "Available" else "Limited")
      }
    }

    AegisBottomNavScaffold(
      items = navItems,
      selected = Activity,
      onSelect = { key: NavKey ->
        when (key) {
          Home -> onOpenHome()
          Zones -> onOpenZones()
          TouristId -> onOpenTouristId()
          else -> Unit
        }
      },
      onSos = onSos,
      modifier =
        Modifier
          .align(Alignment.BottomCenter)
          .navigationBarsPadding()
          .padding(horizontal = 20.dp)
          .padding(bottom = 10.dp),
    )
  }
}

@Composable
private fun TimelineRow(title: String, body: String, status: String) {
  val color = if (status == "Attention" || status == "Limited") CautionAmber else SafeGreen
  Surface(shape = RoundedCornerShape(22.dp), color = GlassSurface, border = BorderStroke(1.dp, GlassBorder)) {
    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(modifier = Modifier.size(34.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
        Surface(shape = CircleShape, color = color) {
          Box(modifier = Modifier.size(10.dp))
        }
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, color = Ink)
        Text(text = body, style = MaterialTheme.typography.bodySmall, color = InkSoft)
      }
      Text(text = status, style = MaterialTheme.typography.labelSmall, color = color)
    }
  }
}
