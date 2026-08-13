package com.example.aegis.ui.incident

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aegis.theme.DangerRed
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
import com.example.aegis.ui.state.IncidentCheckState

@Composable
fun IncidentCheckScreen(
  onBack: () -> Unit,
  onSafe: () -> Unit,
  onNeedHelp: () -> Unit,
  modifier: Modifier = Modifier,
  state: IncidentCheckState = AegisSampleState.incidentCheck,
) {
  val progress = state.countdownSeconds.toFloat() / state.totalSeconds.toFloat()
  AegisBackground(modifier = modifier) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .statusBarsPadding()
          .padding(horizontal = 20.dp)
          .padding(bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      GlassIconButton(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", onClick = onBack)
      GlassCard(shape = RoundedCornerShape(34.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
          Text(text = state.type.title, style = MaterialTheme.typography.headlineSmall, color = DangerRed)
          Text(text = "ARE YOU OKAY?", style = MaterialTheme.typography.displayMedium, color = Ink)
          Text(
            text = "Emergency countdown: ${state.countdownSeconds}s",
            style = MaterialTheme.typography.titleMedium,
            color = InkSoft,
          )
          LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(12.dp),
            color = DangerRed,
            trackColor = DangerRed.copy(alpha = 0.16f),
          )
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ActionButton("I'M SAFE", SafeGreen, onSafe, Modifier.weight(1f))
            ActionButton("NEED HELP", DangerRed, onNeedHelp, Modifier.weight(1f))
          }
        }
      }
      Text(
        text = "This screen is ready for future motion detection. It does not send emergency transport by itself.",
        style = MaterialTheme.typography.bodySmall,
        color = InkSoft,
      )
    }
  }
}

@Composable
private fun ActionButton(text: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(18.dp), color = color, border = BorderStroke(1.dp, GlassBorder)) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      color = if (color == GlassSurface || color == ForestDark) Ink else Color.White,
      modifier = Modifier.padding(vertical = 16.dp, horizontal = 10.dp),
    )
  }
}
