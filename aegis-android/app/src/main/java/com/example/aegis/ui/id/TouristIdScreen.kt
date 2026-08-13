package com.example.aegis.ui.id

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.example.aegis.Activity
import com.example.aegis.Home
import com.example.aegis.Map
import com.example.aegis.TouristId
import com.example.aegis.theme.ForestDark
import com.example.aegis.theme.GlassBorder
import com.example.aegis.theme.Ink
import com.example.aegis.theme.InkSoft
import com.example.aegis.theme.SafeGreen
import com.example.aegis.theme.SageMid
import com.example.aegis.theme.SageSoft
import com.example.aegis.ui.components.AegisBackground
import com.example.aegis.ui.components.AegisBottomNavScaffold
import com.example.aegis.ui.components.BottomNavItem
import com.example.aegis.ui.components.FakeQr
import com.example.aegis.ui.components.GlassCard
import com.example.aegis.ui.components.SosOverlay
import com.example.aegis.ui.state.AegisSampleState
import com.example.aegis.ui.state.DigitalIdUiState

@Composable
fun TouristIdScreen(
  onOpenHome: () -> Unit,
  onOpenMap: () -> Unit,
  onOpenActivity: () -> Unit,
  modifier: Modifier = Modifier,
  state: DigitalIdUiState = AegisSampleState.digitalId,
) {
  var sosVisible by remember { mutableStateOf(false) }
  var technicalExpanded by remember { mutableStateOf(false) }
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
      Text(text = "Digital ID", style = MaterialTheme.typography.displayMedium, color = Ink)
      Text(text = "Private, pseudonymous, and ready for offline verification.", style = MaterialTheme.typography.bodyMedium, color = InkSoft)

      Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), color = Color.Transparent, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))) {
        Column(
          modifier = Modifier.background(Brush.linearGradient(listOf(SageSoft, SageMid))).padding(22.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Text(text = "AEGIS SAFEPASS", style = MaterialTheme.typography.titleLarge, color = Ink)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
              Text(text = state.touristId, style = MaterialTheme.typography.headlineMedium, color = Ink)
              Text(text = "Pseudonymous tourist ID", style = MaterialTheme.typography.labelSmall, color = InkSoft)
            }
            Surface(shape = RoundedCornerShape(50), color = SafeGreen.copy(alpha = 0.16f), border = BorderStroke(1.dp, SafeGreen.copy(alpha = 0.55f))) {
              Text(text = state.status.uppercase(), style = MaterialTheme.typography.labelSmall, color = SafeGreen, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
            }
          }
        }
      }

      GlassCard(shape = RoundedCornerShape(32.dp), contentPadding = PaddingValues(22.dp)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(text = "VERIFICATION QR", style = MaterialTheme.typography.labelSmall, color = InkSoft)
          FakeQr(modifier = Modifier.size(190.dp), tint = ForestDark)
          Text(text = "Valid ${state.validity}", style = MaterialTheme.typography.labelMedium, color = SafeGreen)
          Text(text = state.privacyExplanation, style = MaterialTheme.typography.bodySmall, color = InkSoft, textAlign = TextAlign.Center)
        }
      }

      GlassCard(onClick = { technicalExpanded = !technicalExpanded }, shape = RoundedCornerShape(26.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text(text = "Technical verification", style = MaterialTheme.typography.headlineSmall, color = Ink)
          Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = InkSoft)
        }
        if (technicalExpanded) {
          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = Ink.copy(alpha = 0.1f))
          ProofRow("Hash", state.hash)
          ProofRow("Contract", state.contractAddress)
          ProofRow("Network", state.network)
        }
      }
    }

    AegisBottomNavScaffold(
      items = navItems,
      selected = TouristId,
      onSelect = { key: NavKey ->
        when (key) {
          Home -> onOpenHome()
          Map -> onOpenMap()
          Activity -> onOpenActivity()
          else -> Unit
        }
      },
      onSos = { sosVisible = true },
      modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 20.dp).padding(bottom = 10.dp),
    )
    if (sosVisible) SosOverlay(onDismiss = { sosVisible = false })
  }
}

@Composable
private fun ProofRow(label: String, value: String) {
  Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
    Text(text = label, style = MaterialTheme.typography.labelSmall, color = InkSoft)
    Spacer(modifier = Modifier.width(14.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.labelMedium,
      color = Ink,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.End,
      modifier = Modifier.weight(1f),
    )
  }
}
