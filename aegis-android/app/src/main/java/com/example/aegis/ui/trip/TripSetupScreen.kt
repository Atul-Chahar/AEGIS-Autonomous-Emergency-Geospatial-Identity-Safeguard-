package com.example.aegis.ui.trip

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.aegis.ui.state.TripSetupState

@Composable
fun TripSetupScreen(
  onBack: () -> Unit,
  onStartJourney: () -> Unit,
  modifier: Modifier = Modifier,
  state: TripSetupState = AegisSampleState.tripSetup,
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
      GlassIconButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", onClick = onBack)
      Text(text = "Start Safe Journey", style = MaterialTheme.typography.displayMedium, color = Ink)
      Text(
        text = "Review the essentials before Journey Protection starts recording on this device.",
        style = MaterialTheme.typography.bodyMedium,
        color = InkSoft,
      )

      GlassCard(shape = RoundedCornerShape(30.dp), contentPadding = PaddingValues(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          SetupRow("Destination", state.destination)
          SetupRow("Planned route", state.plannedRoute)
          SetupRow("Expected return", state.expectedReturnTime)
          SetupRow("Emergency contact", state.emergencyContact)
          SetupRow("Offline map", state.offlineMapReadiness)
          SetupRow("Journey BlackBox", state.blackBoxReadiness)
          SetupRow("Check-in interval", state.checkInInterval)
        }
      }

      Surface(onClick = onStartJourney, shape = RoundedCornerShape(50), color = ForestDark) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "START SAFE JOURNEY",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "🛰", style = MaterialTheme.typography.labelLarge)
        }
      }
    }
  }
}

@Composable
private fun SetupRow(label: String, value: String) {
  Surface(shape = RoundedCornerShape(18.dp), color = GlassSurface, border = BorderStroke(1.dp, GlassBorder)) {
    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(modifier = Modifier.width(4.dp).height(34.dp).clip(CircleShape).background(SafeGreen))
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = InkSoft)
        Text(text = value, style = MaterialTheme.typography.titleSmall, color = Ink)
      }
    }
  }
}
