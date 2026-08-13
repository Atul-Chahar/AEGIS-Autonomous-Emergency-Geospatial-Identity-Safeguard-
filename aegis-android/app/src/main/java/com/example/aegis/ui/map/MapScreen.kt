package com.example.aegis.ui.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.example.aegis.Activity
import com.example.aegis.Home
import com.example.aegis.Map
import com.example.aegis.TouristId
import com.example.aegis.data.ZoneStatus
import com.example.aegis.theme.CautionAmber
import com.example.aegis.theme.DangerRed
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
import com.example.aegis.ui.components.GuardianStatePill
import com.example.aegis.ui.components.SosOverlay
import com.example.aegis.ui.state.AegisSampleState
import com.example.aegis.ui.state.MapLayerState
import com.example.aegis.ui.state.MapUiState
import com.example.aegis.ui.state.MapZoneMarker

@Composable
fun MapScreen(
  onOpenHome: () -> Unit,
  onOpenActivity: () -> Unit,
  onOpenTouristId: () -> Unit,
  onOpenSafetyCenter: () -> Unit,
  onOpenZoneDetail: (String) -> Unit,
  modifier: Modifier = Modifier,
  state: MapUiState = AegisSampleState.map,
) {
  var sosVisible by remember { mutableStateOf(false) }
  val navItems =
    listOf(
      BottomNavItem("Home", Icons.Filled.Home, Home),
      BottomNavItem("Map", Icons.Filled.Place, Map),
      BottomNavItem("Activity", Icons.Filled.Notifications, Activity),
      BottomNavItem("ID", Icons.Filled.Person, TouristId),
    )

  AegisBackground(modifier = modifier) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 150.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Column {
            Text(text = "Map", style = MaterialTheme.typography.displayMedium, color = Ink)
            Text(text = state.destination, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
          }
          GuardianStatePill(AegisSampleState.homeActiveTrip.guardian, onClick = onOpenSafetyCenter)
        }

        LayerControls(layers = state.layers)
        MapSurface(state = state, onOpenZoneDetail = onOpenZoneDetail)
      }

      AegisBottomNavScaffold(
        items = navItems,
        selected = Map,
        onSelect = { key: NavKey ->
          when (key) {
            Home -> onOpenHome()
            Activity -> onOpenActivity()
            TouristId -> onOpenTouristId()
            else -> Unit
          }
        },
        onSos = { sosVisible = true },
        modifier =
          Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 10.dp),
      )

      if (sosVisible) SosOverlay(onDismiss = { sosVisible = false })
    }
  }
}

@Composable
private fun LayerControls(layers: List<MapLayerState>) {
  Row(
    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    layers.forEach { layer ->
      Surface(
        shape = RoundedCornerShape(50),
        color = if (layer.enabled) ForestDark else GlassSurface,
        border = BorderStroke(1.dp, if (layer.enabled) Color.Transparent else GlassBorder),
      ) {
        Text(
          text = layer.label,
          style = MaterialTheme.typography.labelLarge,
          color = if (layer.enabled) Color.White else Ink,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
        )
      }
    }
  }
}

@Composable
private fun MapSurface(state: MapUiState, onOpenZoneDetail: (String) -> Unit) {
  Box(modifier = Modifier.fillMaxWidth().height(620.dp)) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      shape = RoundedCornerShape(34.dp),
      color = ForestDark,
      border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
    ) {
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF183426), Color(0xFF0F1F19)))),
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val pathColor = Color(0xFFD5E6B6)
          drawLine(pathColor.copy(alpha = 0.18f), Offset(size.width * 0.18f, size.height * 0.78f), Offset(size.width * 0.74f, size.height * 0.18f), strokeWidth = 44f, cap = StrokeCap.Round)
          drawLine(pathColor, Offset(size.width * 0.18f, size.height * 0.78f), Offset(size.width * 0.74f, size.height * 0.18f), strokeWidth = 10f, cap = StrokeCap.Round)
          drawCircle(SafeGreen, radius = 12f, center = Offset(size.width * 0.42f, size.height * 0.52f))
          drawCircle(CautionAmber, radius = 16f, center = Offset(size.width * 0.58f, size.height * 0.36f))
          drawCircle(DangerRed, radius = 15f, center = Offset(size.width * 0.74f, size.height * 0.2f))
        }
        Column(modifier = Modifier.align(Alignment.TopStart).padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(text = state.currentLocationLabel, style = MaterialTheme.typography.titleMedium, color = Color.White)
          Text(text = state.routeSummary, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.74f))
        }
      }
    }

    GlassCard(
      modifier = Modifier.align(Alignment.BottomCenter).padding(14.dp).fillMaxWidth(),
      shape = RoundedCornerShape(28.dp),
      contentPadding = PaddingValues(18.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.align(Alignment.CenterHorizontally).width(42.dp).height(4.dp).clip(CircleShape).background(InkSoft.copy(alpha = 0.34f)))
        Text(text = state.selectedDetailTitle, style = MaterialTheme.typography.headlineSmall, color = Ink)
        Text(text = state.selectedDetailBody, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
          state.zones.take(4).forEachIndexed { index, zone ->
            ZoneChip(zone = zone, onClick = { onOpenZoneDetail(AegisSampleState.photoZones.getOrNull(index)?.id ?: AegisSampleState.photoZones.first().id) })
          }
        }
      }
    }
  }
}

@Composable
private fun ZoneChip(zone: MapZoneMarker, onClick: () -> Unit) {
  val color =
    when (zone.status) {
      ZoneStatus.SAFE -> SafeGreen
      ZoneStatus.CAUTION -> CautionAmber
      ZoneStatus.HIGH_RISK -> DangerRed
    }
  Surface(onClick = onClick, shape = RoundedCornerShape(18.dp), color = color.copy(alpha = 0.12f), border = BorderStroke(1.dp, color.copy(alpha = 0.45f))) {
    Column(modifier = Modifier.width(156.dp).padding(12.dp)) {
      Text(text = zone.name, style = MaterialTheme.typography.labelMedium, color = Ink, maxLines = 2)
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = zone.status.label, style = MaterialTheme.typography.labelSmall, color = color)
    }
  }
}
