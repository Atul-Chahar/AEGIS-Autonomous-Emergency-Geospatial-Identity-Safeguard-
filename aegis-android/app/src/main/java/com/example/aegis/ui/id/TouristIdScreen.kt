package com.example.aegis.ui.id

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.aegis.Activity
import com.example.aegis.Home
import com.example.aegis.TouristId
import com.example.aegis.Zones
import com.example.aegis.data.MockData
import com.example.aegis.theme.DangerRed
import com.example.aegis.theme.ForestDark
import com.example.aegis.theme.GlassBorder
import com.example.aegis.theme.GlassSurface
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
import com.example.aegis.ui.components.GlassIconButton
import com.example.aegis.ui.components.SectionHeader
import com.example.aegis.ui.components.SosOverlay

@Composable
fun TouristIdScreen(
  onBack: () -> Unit,
  onOpenHome: () -> Unit,
  onOpenZones: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var sosVisible by remember { mutableStateOf(false) }
  val context = LocalContext.current

  val navItems =
    listOf(
      BottomNavItem("Home", Icons.Filled.Home, Home),
      BottomNavItem("Zones", Icons.Filled.Place, Zones),
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
      // Top bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        GlassIconButton(
          icon = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          onClick = onBack,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          GlassIconButton(
            icon = Icons.Filled.Star,
            contentDescription = "Bookmark",
            onClick = {},
          )
          GlassIconButton(
            icon = Icons.Filled.Notifications,
            contentDescription = "Alerts",
            onClick = {},
          )
        }
      }

      Text(
        text = "Digital Tourist ID",
        style = MaterialTheme.typography.displayMedium,
        color = Ink,
      )
      Text(
        text = "Tamper-proof · privacy-first · offline verifiable",
        style = MaterialTheme.typography.bodyMedium,
        color = InkSoft,
      )

      // ── Pass hero card ─────────────────────────────────────
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
      ) {
        Column(
          modifier =
            Modifier
              .background(Brush.linearGradient(listOf(SageSoft, SageMid)))
              .padding(22.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🛡️", fontSize = 34.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Text(
                text = "AEGIS SAFEPASS",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
              )
              Text(
                text = "Autonomous Emergency & Identity Safeguard",
                style = MaterialTheme.typography.labelSmall,
                color = InkSoft,
              )
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = MockData.TOURIST_ID,
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
              )
              Text(
                text = "${MockData.NETWORK} · on-chain voucher",
                style = MaterialTheme.typography.labelSmall,
                color = InkSoft,
              )
            }
            Surface(
              shape = RoundedCornerShape(50),
              color = SafeGreen.copy(alpha = 0.16f),
              border = BorderStroke(1.dp, SafeGreen.copy(alpha = 0.55f)),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(text = "●", fontSize = 9.sp, color = SafeGreen)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = "ACTIVE",
                  style = MaterialTheme.typography.labelSmall,
                  color = SafeGreen,
                )
              }
            }
          }
        }
      }

      // ── Verification voucher (QR) ──────────────────────────
      GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        contentPadding = PaddingValues(22.dp),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text(
            text = "VERIFICATION VOUCHER",
            style = MaterialTheme.typography.labelSmall,
            color = InkSoft,
          )
          FakeQr(modifier = Modifier.size(190.dp), tint = ForestDark)
          Text(
            text = "Scan at checkpoints · homestays · police posts",
            style = MaterialTheme.typography.bodySmall,
            color = InkSoft,
          )
          Surface(
            shape = RoundedCornerShape(50),
            color = SafeGreen.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, SafeGreen.copy(alpha = 0.5f)),
          ) {
            Text(
              text = "✓ Valid · ${MockData.VALIDITY}",
              style = MaterialTheme.typography.labelMedium,
              color = SafeGreen,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
          }
        }
      }

      // ── On-chain proof ─────────────────────────────────────
      SectionHeader(title = "On-Chain Proof")
      GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        contentPadding = PaddingValues(20.dp),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          ProofRow(label = "Hash", value = MockData.TOURIST_HASH)
          ProofRow(label = "Contract", value = MockData.CONTRACT_ADDRESS)
          ProofRow(label = "Network", value = MockData.NETWORK)
          ProofRow(label = "Auto-expiry", value = "20 Aug 2026 · 18:00 IST")
          HorizontalDivider(color = Ink.copy(alpha = 0.1f))
          Text(
            text =
              "🔒 Privacy-first: only keccak256(TouristID + Salt) is committed on-chain. " +
                "No passport, Aadhaar or phone numbers ever touch the blockchain.",
            style = MaterialTheme.typography.bodySmall,
            color = InkSoft,
          )
        }
      }

      // ── Revoke ─────────────────────────────────────────────
      Surface(
        onClick = {
          Toast.makeText(context, "Revocation request sent to authority", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50),
        color = DangerRed.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
      ) {
        Text(
          text = "Revoke ID",
          style = MaterialTheme.typography.labelLarge,
          color = DangerRed,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(vertical = 14.dp),
        )
      }
    }

    AegisBottomNavScaffold(
      items = navItems,
      selected = TouristId,
      onSelect = { key: NavKey ->
        when (key) {
          Home -> onOpenHome()
          Zones -> onOpenZones()
          Activity ->
            Toast.makeText(context, "📊 Activity log coming soon", Toast.LENGTH_SHORT).show()
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

// ─────────────────────────────────────────────────────────────
// ProofRow — label/value detail line.
// ─────────────────────────────────────────────────────────────
@Composable
private fun ProofRow(label: String, value: String, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = InkSoft,
    )
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 400, heightDp = 880)
@Composable
private fun TouristIdScreenPreview() {
  com.example.aegis.theme.AEGISTheme {
    TouristIdScreen(onBack = {}, onOpenHome = {}, onOpenZones = {})
  }
}
