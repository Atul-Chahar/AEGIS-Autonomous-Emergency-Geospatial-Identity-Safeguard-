package com.example.aegis.ui.map

import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation3.runtime.NavKey
import com.example.aegis.Activity
import com.example.aegis.Home
import com.example.aegis.Map
import com.example.aegis.TouristId
import com.example.aegis.domain.model.ZoneStatus
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
import com.example.aegis.ui.state.AegisSampleState
import com.example.aegis.ui.state.MapLayerState
import com.example.aegis.ui.state.MapUiState
import com.example.aegis.ui.state.MapZoneMarker
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline

@Composable
fun MapScreen(
  onOpenHome: () -> Unit,
  onOpenActivity: () -> Unit,
  onOpenTouristId: () -> Unit,
  onOpenSafetyCenter: () -> Unit,
  onOpenZoneDetail: (String) -> Unit,
  onSos: () -> Unit,
  modifier: Modifier = Modifier,
  state: MapUiState = AegisSampleState.map,
) {
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
          GuardianStatePill(state.guardian, onClick = onOpenSafetyCenter)
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
    RealOsmMap(
      state = state,
      modifier =
        Modifier
          .fillMaxSize()
          .clip(RoundedCornerShape(34.dp)),
    )

    // Live status chip over the real map (real breadcrumb data, never fake).
    GlassCard(
      modifier = Modifier.align(Alignment.TopStart).padding(14.dp),
      shape = RoundedCornerShape(20.dp),
      contentPadding = PaddingValues(12.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = state.currentLocationLabel, style = MaterialTheme.typography.labelMedium, color = Ink, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Text(text = state.routeSummary, style = MaterialTheme.typography.labelSmall, color = InkSoft)
        Text(text = state.corridorStatus, style = MaterialTheme.typography.labelSmall, color = if (state.corridorStatus.contains("off")) CautionAmber else SafeGreen)
      }
    }

    GlassCard(
      modifier = Modifier.align(Alignment.BottomCenter).padding(14.dp).fillMaxWidth(),
      shape = RoundedCornerShape(28.dp),
      contentPadding = PaddingValues(18.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.align(Alignment.CenterHorizontally).width(42.dp).height(4.dp).clip(RoundedCornerShape(50)).background(InkSoft.copy(alpha = 0.34f)))
        Text(text = state.selectedDetailTitle, style = MaterialTheme.typography.headlineSmall, color = Ink)
        Text(text = state.selectedDetailBody, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
          state.zones.take(4).forEach { zone ->
            ZoneChip(zone = zone, onClick = { onOpenZoneDetail(zone.id) })
          }
        }
      }
    }
  }
}

/**
 * Real OpenStreetMap basemap (osmdroid) — the same OSM tiles the web
 * dashboard uses via Leaflet. Overlays are drawn from REAL data only:
 * zone markers at real zone centroids, the recorded breadcrumb trail, and
 * the live position fix.
 */
@Composable
private fun RealOsmMap(state: MapUiState, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val mapView =
    remember {
      Configuration.getInstance().userAgentValue = context.packageName
      MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        controller.setZoom(11.0)
      }
    }

  DisposableEffect(mapView) {
    mapView.onResume()
    onDispose { mapView.onPause() }
  }

  LaunchedEffect(state.trailPoints, state.zones) {
    val trail = state.trailPoints
    val position = trail.lastOrNull()

    // Default camera: last fix, else first zone, else Meghalaya region.
    val focus =
      position
        ?: state.zones.firstNotNullOfOrNull { zoneCentroid(it.id) }
        ?: (25.30 to 91.60)
    mapView.controller.setCenter(GeoPoint(focus.first, focus.second))
    if (position != null) mapView.controller.setZoom(13.0)

    mapView.overlays.clear()

    // Safety-zone markers at real centroids, colored by live risk status.
    state.zones.forEach { zone ->
      val centroid = zoneCentroid(zone.id) ?: return@forEach
      val color = zoneColorArgb(zone.status)
      mapView.overlays.add(
        circlePolygon(
          center = GeoPoint(centroid.first, centroid.second),
          radiusMeters = 400.0,
          outlineColor = color,
          fillColor = (color and 0x00FFFFFF) or 0x22000000,
        ),
      )
    }

    // Real breadcrumb trail (only when at least two fixes exist).
    if (trail.size >= 2) {
      mapView.overlays.add(
        Polyline().apply {
          setPoints(trail.map { GeoPoint(it.first, it.second) })
          outlinePaint.color = androidx.compose.ui.graphics.Color(0xFF2563EB).toArgb()
          outlinePaint.strokeWidth = 10f
        },
      )
    }

    // Live position fix.
    if (position != null) {
      mapView.overlays.add(
        circlePolygon(
          center = GeoPoint(position.first, position.second),
          radiusMeters = 50.0,
          outlineColor = androidx.compose.ui.graphics.Color(0xFF2563EB).toArgb(),
          fillColor = androidx.compose.ui.graphics.Color(0x552563EB).toArgb(),
        ),
      )
    }

    mapView.invalidate()
  }

  AndroidView(factory = { mapView }, modifier = modifier)
}

@Composable
private fun ZoneChip(zone: MapZoneMarker, onClick: () -> Unit) {
  val color =
    when (zone.status) {
      ZoneStatus.SAFE -> SafeGreen
      ZoneStatus.CAUTION -> CautionAmber
      ZoneStatus.HIGH_RISK -> DangerRed
      ZoneStatus.UNKNOWN -> InkSoft
    }
  Surface(onClick = onClick, shape = RoundedCornerShape(18.dp), color = color.copy(alpha = 0.12f), border = BorderStroke(1.dp, color.copy(alpha = 0.45f))) {
    Column(modifier = Modifier.width(156.dp).padding(12.dp)) {
      Text(text = zone.name, style = MaterialTheme.typography.labelMedium, color = Ink, maxLines = 2)
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = zone.status.label, style = MaterialTheme.typography.labelSmall, color = color)
    }
  }
}

/** Real geographic centroids for the app's safety zones (Meghalaya). */
private fun zoneCentroid(zoneId: String): Pair<Double, Double>? =
  when (zoneId) {
    "cherrapunji" -> 25.275 to 91.730
    "roots" -> 25.250 to 91.675 // Nongriat living-root corridor
    "dawki" -> 25.181 to 91.297 // Umngot river at Dawki
    "nohkalikai" -> 25.275 to 91.688 // Nohkalikai falls canyon
    else -> null
  }

private fun zoneColorArgb(status: ZoneStatus): Int =
  when (status) {
    ZoneStatus.SAFE -> SafeGreen.toArgb()
    ZoneStatus.CAUTION -> CautionAmber.toArgb()
    ZoneStatus.HIGH_RISK -> DangerRed.toArgb()
    ZoneStatus.UNKNOWN -> InkSoft.toArgb()
  }

/** Approximates a circle on the map with a dense polygon (core osmdroid has no CircleOverlay). */
private fun circlePolygon(
  center: GeoPoint,
  radiusMeters: Double,
  outlineColor: Int,
  fillColor: Int,
): Polygon =
  Polygon().apply {
    val points = mutableListOf<GeoPoint>()
    val steps = 36
    for (i in 0 until steps) {
      val angle = Math.toRadians(360.0 * i / steps)
      val dLat = (radiusMeters / 111_320.0) * Math.cos(angle)
      val dLon = (radiusMeters / (111_320.0 * Math.cos(Math.toRadians(center.latitude)))) * Math.sin(angle)
      points.add(GeoPoint(center.latitude + dLat, center.longitude + dLon))
    }
    setPoints(points)
    outlinePaint.color = outlineColor
    outlinePaint.style = Paint.Style.STROKE
    outlinePaint.strokeWidth = 4f
    fillPaint.color = fillColor
  }
