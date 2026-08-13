package com.example.aegis.ui.activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.aegis.ui.state.ActivityTimelineState
import com.example.aegis.ui.state.AegisSampleState
import com.example.aegis.ui.state.JourneyActivityEvent
import com.example.aegis.ui.state.TimelineEventLevel

@Composable
fun ActivityScreen(
  onOpenHome: () -> Unit,
  onOpenMap: () -> Unit,
  onOpenTouristId: () -> Unit,
  onOpenSafetyCenter: () -> Unit,
  onOpenBlackBox: () -> Unit,
  onSos: () -> Unit,
  modifier: Modifier = Modifier,
  state: ActivityTimelineState = AegisSampleState.activity,
) {
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
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
          Text(text = "Activity", style = MaterialTheme.typography.displayMedium, color = Ink)
          Text(text = "Journey timeline", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
        }
        GuardianStatePill(state.guardian, onClick = onOpenSafetyCenter)
      }

      GlassCard(onClick = onOpenBlackBox, shape = RoundedCornerShape(28.dp)) {
        Text(text = "Journey BlackBox", style = MaterialTheme.typography.headlineSmall, color = Ink)
        Text(text = "${state.blackBoxSummary.recordingState} · last breadcrumb ${state.blackBoxSummary.lastBreadcrumbTime}", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
        Text(text = "${state.blackBoxSummary.pendingBreadcrumbs} pending breadcrumbs", style = MaterialTheme.typography.labelMedium, color = CautionAmber)
      }

      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        state.events.forEach { TimelineRow(event = it) }
      }
    }

    AegisBottomNavScaffold(
      items = navItems,
      selected = Activity,
      onSelect = { key: NavKey ->
        when (key) {
          Home -> onOpenHome()
          Map -> onOpenMap()
          TouristId -> onOpenTouristId()
          else -> Unit
        }
      },
      onSos = onSos,
      modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 20.dp).padding(bottom = 10.dp),
    )
  }
}

@Composable
fun TimelineRow(event: JourneyActivityEvent, modifier: Modifier = Modifier) {
  val color =
    when (event.level) {
      TimelineEventLevel.NORMAL -> SafeGreen
      TimelineEventLevel.CAUTION -> CautionAmber
      TimelineEventLevel.ATTENTION -> CautionAmber
      TimelineEventLevel.EMERGENCY -> DangerRed
    }
  Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
      }
    }
    Spacer(modifier = Modifier.width(12.dp))
    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), color = GlassSurface, border = BorderStroke(1.dp, GlassBorder)) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text(text = event.title, style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
          Text(text = event.time, style = MaterialTheme.typography.labelSmall, color = InkSoft)
        }
        Text(text = event.subtitle, style = MaterialTheme.typography.bodySmall, color = InkSoft)
      }
    }
  }
}
