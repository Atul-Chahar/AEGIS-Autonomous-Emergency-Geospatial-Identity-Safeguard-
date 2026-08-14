package com.example.aegis.ui.activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aegis.theme.ForestDark
import com.example.aegis.theme.GlassBorder
import com.example.aegis.theme.GlassSurface
import com.example.aegis.theme.Ink
import com.example.aegis.theme.InkSoft
import com.example.aegis.theme.SafeGreen
import com.example.aegis.ui.components.AegisBackground
import com.example.aegis.ui.components.GlassCard
import com.example.aegis.ui.components.GlassIconButton
import com.example.aegis.ui.state.AegisSampleState
import com.example.aegis.ui.state.JourneyBlackBoxState

@Composable
fun JourneyBlackBoxScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  state: JourneyBlackBoxState = AegisSampleState.blackBox,
) {
  AegisBackground(modifier = modifier) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .statusBarsPadding()
          .padding(horizontal = 20.dp)
          .padding(bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      GlassIconButton(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", onClick = onBack)
      Text(text = "Journey BlackBox", style = MaterialTheme.typography.displayMedium, color = Ink)
      Text(text = "Live record of this journey's breadcrumbs, battery and sync state — stored in the local BlackBox and synced to the gateway when online.", style = MaterialTheme.typography.bodyMedium, color = InkSoft)

      GlassCard(shape = RoundedCornerShape(30.dp)) {
        StatRow("Recording state", state.recordingState)
        StatRow("Last breadcrumb", state.lastBreadcrumbTime)
        StatRow("Location accuracy", state.locationAccuracy)
        StatRow("Current activity", state.currentActivityType)
        StatRow("Battery", state.battery)
      }

      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        CountCard("Stored", state.storedBreadcrumbs.toString(), Modifier.weight(1f))
        CountCard("Synced", state.syncedBreadcrumbs.toString(), Modifier.weight(1f))
        CountCard("Pending", state.pendingBreadcrumbs.toString(), Modifier.weight(1f))
      }

      Text(text = "Recent safety events", style = MaterialTheme.typography.headlineSmall, color = Ink)
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        state.recentEvents.forEach { TimelineRow(event = it) }
      }
    }
  }
}

@Composable
private fun StatRow(label: String, value: String) {
  Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
    Text(text = value, style = MaterialTheme.typography.titleSmall, color = Ink)
  }
}

@Composable
private fun CountCard(label: String, value: String, modifier: Modifier = Modifier) {
  Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), color = GlassSurface, border = BorderStroke(1.dp, GlassBorder)) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(text = value, style = MaterialTheme.typography.headlineSmall, color = if (label == "Pending") ForestDark else SafeGreen)
      Text(text = label, style = MaterialTheme.typography.labelSmall, color = InkSoft)
    }
  }
}
