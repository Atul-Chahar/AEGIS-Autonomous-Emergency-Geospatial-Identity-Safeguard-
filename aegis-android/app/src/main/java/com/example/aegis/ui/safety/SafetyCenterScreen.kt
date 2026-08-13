package com.example.aegis.ui.safety

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aegis.theme.CautionAmber
import com.example.aegis.theme.DangerRed
import com.example.aegis.theme.GlassBorder
import com.example.aegis.theme.GlassSurface
import com.example.aegis.theme.Ink
import com.example.aegis.theme.InkSoft
import com.example.aegis.theme.SafeGreen
import com.example.aegis.theme.SunYellow
import com.example.aegis.ui.components.AegisBackground
import com.example.aegis.ui.components.GlassCard
import com.example.aegis.ui.components.GlassIconButton
import com.example.aegis.ui.components.GuardianStatePill
import com.example.aegis.ui.state.AegisSampleState
import com.example.aegis.ui.state.GuardianLevel
import com.example.aegis.ui.state.SafetyCenterRow
import com.example.aegis.ui.state.SafetyCenterState

@Composable
fun SafetyCenterScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  state: SafetyCenterState = AegisSampleState.safetyCenter,
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
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        GlassIconButton(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", onClick = onBack)
        GuardianStatePill(state.guardian, onClick = {})
      }
      Text(text = "Safety Center", style = MaterialTheme.typography.displayMedium, color = Ink)
      Text(text = "Advanced protection settings prepared for real systems later.", style = MaterialTheme.typography.bodyMedium, color = InkSoft)

      state.sections.forEach { section ->
        Text(text = section.title, style = MaterialTheme.typography.headlineSmall, color = Ink)
        GlassCard(shape = RoundedCornerShape(26.dp)) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            section.rows.forEach { SafetyRow(row = it) }
          }
        }
      }
    }
  }
}

@Composable
private fun SafetyRow(row: SafetyCenterRow) {
  val color =
    when (row.level) {
      GuardianLevel.ACTIVE -> SafeGreen
      GuardianLevel.LIMITED -> SunYellow
      GuardianLevel.ATTENTION -> CautionAmber
      GuardianLevel.EMERGENCY -> DangerRed
    }
  Surface(shape = RoundedCornerShape(18.dp), color = GlassSurface, border = BorderStroke(1.dp, GlassBorder)) {
    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(color.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(color))
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(text = row.label, style = MaterialTheme.typography.titleSmall, color = Ink)
        Text(text = row.detail, style = MaterialTheme.typography.bodySmall, color = InkSoft)
      }
      Text(text = row.value, style = MaterialTheme.typography.labelSmall, color = color)
    }
  }
}
