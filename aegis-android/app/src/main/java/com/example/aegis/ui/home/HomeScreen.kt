package com.example.aegis.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.example.aegis.Activity
import com.example.aegis.Home
import com.example.aegis.Map
import com.example.aegis.TouristId
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
import com.example.aegis.ui.components.RegionTag
import com.example.aegis.ui.components.RiskMeter
import com.example.aegis.ui.components.SectionHeader
import com.example.aegis.ui.components.SosOverlay
import com.example.aegis.ui.state.AegisSampleState
import com.example.aegis.ui.state.GuardianLevel
import com.example.aegis.ui.state.JourneyHomeState
import com.example.aegis.ui.state.JourneyMode
import com.example.aegis.ui.state.JourneyProtectionItem

@Composable
fun HomeScreen(
  onOpenMap: () -> Unit,
  onOpenActivity: () -> Unit,
  onOpenTouristId: () -> Unit,
  onOpenTripSetup: () -> Unit,
  onOpenSafetyCenter: () -> Unit,
  onOpenZoneDetail: (String) -> Unit,
  modifier: Modifier = Modifier,
  state: JourneyHomeState = AegisSampleState.homeActiveTrip,
) {
  var sosVisible by remember { mutableStateOf(false) }
  val featured = AegisSampleState.photoZones.first()
  val navItems =
    listOf(
      BottomNavItem("Home", Icons.Filled.Home, Home),
      BottomNavItem("Map", Icons.Filled.Place, Map),
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = "Hi, ${state.greetingName}", style = MaterialTheme.typography.titleLarge, color = Ink)
          Text(text = "Your journey protection is ready", style = MaterialTheme.typography.bodySmall, color = InkSoft)
        }
        GuardianStatePill(state = state.guardian, onClick = onOpenSafetyCenter)
      }

      if (state.mode == JourneyMode.PRE_TRIP) {
        PreTripHero(state = state, onStart = onOpenTripSetup)
      } else {
        ActiveTripHero(state = state, onViewRoute = onOpenMap)
      }

      state.routeDeviation?.let {
        WarningCard(message = it.message, level = state.guardian.level)
      }

      SectionHeader(title = "Journey Protection")
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        state.protectionItems.forEach { ProtectionRow(item = it) }
      }

      SectionHeader(title = "Route Preview", action = "Open", onAction = onOpenMap)
      PhotoRouteCard(
        imageRes = featured.imageRes,
        title = featured.name,
        subtitle = featured.tagline,
        status = state.riskLabel,
        onClick = { onOpenZoneDetail(featured.id) },
      )
    }

    AegisBottomNavScaffold(
      items = navItems,
      selected = Home,
      onSelect = { key: NavKey ->
        when (key) {
          Map -> onOpenMap()
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

    if (sosVisible) {
      SosOverlay(onDismiss = { sosVisible = false })
    }
  }
}

@Composable
private fun PreTripHero(state: JourneyHomeState, onStart: () -> Unit) {
  GlassCard(shape = RoundedCornerShape(32.dp), contentPadding = PaddingValues(22.dp)) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      RegionTag(text = "MEGHALAYA")
      Text(text = "Start Safe Journey", style = MaterialTheme.typography.displayMedium, color = Ink)
      MetricGrid(
        items =
          listOf(
            "Destination" to state.destination,
            "Risk" to state.riskLabel,
            "Duration" to state.expectedDuration,
            "Offline" to state.offlineReadiness,
          ),
      )
      PrimaryJourneyButton(text = "START SAFE JOURNEY", onClick = onStart)
    }
  }
}

@Composable
private fun ActiveTripHero(state: JourneyHomeState, onViewRoute: () -> Unit) {
  GlassCard(shape = RoundedCornerShape(32.dp), contentPadding = PaddingValues(22.dp)) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text(text = "Guardian is watching this journey", style = MaterialTheme.typography.headlineMedium, color = Ink)
      Text(text = state.destination, style = MaterialTheme.typography.displayMedium, color = Ink)
      MetricGrid(
        items =
          listOf(
            "Time" to state.journeyDuration,
            "Distance" to state.journeyDistance,
            "Risk" to state.riskLabel,
            "Check-in" to state.checkInCountdown,
          ),
      )
      RiskMeter(score = if (state.riskLabel == "High Risk") 78 else 58)
      Text(text = "Nearest hazard: ${state.nearestHazard}", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
      PrimaryJourneyButton(text = "VIEW LIVE ROUTE", onClick = onViewRoute)
    }
  }
}

@Composable
private fun MetricGrid(items: List<Pair<String, String>>) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    items.chunked(2).forEach { row ->
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        row.forEach { item ->
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            color = ForestDark.copy(alpha = 0.06f),
            border = BorderStroke(1.dp, ForestDark.copy(alpha = 0.1f)),
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(text = item.first, style = MaterialTheme.typography.labelSmall, color = InkSoft)
              Text(text = item.second, style = MaterialTheme.typography.titleSmall, color = Ink, maxLines = 2)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ProtectionRow(item: JourneyProtectionItem) {
  val color =
    when (item.level) {
      GuardianLevel.ACTIVE -> SafeGreen
      GuardianLevel.LIMITED -> CautionAmber
      GuardianLevel.ATTENTION -> CautionAmber
      GuardianLevel.EMERGENCY -> DangerRed
    }
  Surface(shape = RoundedCornerShape(20.dp), color = GlassSurface, border = BorderStroke(1.dp, GlassBorder)) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(text = item.label, style = MaterialTheme.typography.titleSmall, color = Ink)
        Text(text = item.detail, style = MaterialTheme.typography.bodySmall, color = InkSoft, maxLines = 2)
      }
      Text(text = item.value, style = MaterialTheme.typography.labelMedium, color = color)
    }
  }
}

@Composable
private fun WarningCard(message: String, level: GuardianLevel) {
  val color = if (level == GuardianLevel.EMERGENCY) DangerRed else CautionAmber
  Surface(shape = RoundedCornerShape(22.dp), color = color.copy(alpha = 0.13f), border = BorderStroke(1.dp, color.copy(alpha = 0.5f))) {
    Text(text = message, style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.padding(16.dp))
  }
}

@Composable
private fun PhotoRouteCard(
  imageRes: Int,
  title: String,
  subtitle: String,
  status: String,
  onClick: () -> Unit,
) {
  Surface(onClick = onClick, shape = RoundedCornerShape(28.dp), color = ForestDark) {
    Box(modifier = Modifier.fillMaxWidth().height(230.dp)) {
      Image(painter = painterResource(imageRes), contentDescription = title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
      Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.28f)))
      Column(modifier = Modifier.align(Alignment.BottomStart).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = status, style = MaterialTheme.typography.labelMedium, color = Color.White)
        Text(text = title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.84f), maxLines = 2, overflow = TextOverflow.Ellipsis)
      }
    }
  }
}

@Composable
private fun PrimaryJourneyButton(text: String, onClick: () -> Unit) {
  Surface(onClick = onClick, shape = RoundedCornerShape(50), color = ForestDark, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
      Text(text = text, style = MaterialTheme.typography.labelLarge, color = Color.White)
      Spacer(modifier = Modifier.width(8.dp))
      Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
  }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun HomeScreenPreview() {
  com.example.aegis.theme.AEGISTheme {
    HomeScreen(onOpenMap = {}, onOpenActivity = {}, onOpenTouristId = {}, onOpenTripSetup = {}, onOpenSafetyCenter = {}, onOpenZoneDetail = {})
  }
}
