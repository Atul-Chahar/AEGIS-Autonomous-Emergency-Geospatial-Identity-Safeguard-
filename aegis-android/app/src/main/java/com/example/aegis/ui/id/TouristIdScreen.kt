package com.example.aegis.ui.id

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aegis.theme.ForestDark
import com.example.aegis.theme.Ink
import com.example.aegis.theme.SageMid
import com.example.aegis.theme.SunYellow
import com.example.aegis.ui.components.AegisBackground
import com.example.aegis.ui.components.BackButton
import com.example.aegis.ui.components.GlassCard

@Composable
fun TouristIdScreen(
  viewModel: TouristIdViewModel,
  onBack: () -> Unit,
  onOpenHome: () -> Unit,
  onOpenZones: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val identity = state.identity

  Box(modifier = modifier.fillMaxSize()) {
    AegisBackground(modifier = Modifier.fillMaxSize()) {}

    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .statusBarsPadding()
          .padding(horizontal = 20.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
      ) {
        BackButton(onClick = onBack)
        Spacer(modifier = Modifier.weight(1f))
        Text(
          text = "SAFEPASS",
          style = MaterialTheme.typography.labelMedium,
          color = SageMid,
          fontWeight = FontWeight.ExtraBold,
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Tourist\nIdentity",
        style = MaterialTheme.typography.displaySmall,
        color = Ink,
        fontWeight = FontWeight.ExtraBold,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text =
          "Your identity voucher. Public verification uses only a salted cryptographic hash — " +
            "never raw passport, Aadhaar, phone, or contact details.",
        style = MaterialTheme.typography.bodyMedium,
        color = Ink.copy(alpha = 0.66f),
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Voucher card
      GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp)) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = "AEGIS SAFEPASS",
            style = MaterialTheme.typography.labelMedium,
            color = SageMid,
            fontWeight = FontWeight.ExtraBold,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = identity?.touristId ?: "—",
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            fontWeight = FontWeight.ExtraBold,
          )
          Spacer(modifier = Modifier.height(16.dp))

          // QR voucher (procedural placeholder — real QR comes with identity issuance)
          Box(
            modifier =
              Modifier
                .size(180.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center,
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val cell = size.minDimension / 21f
              var seed = identity?.touristId?.hashCode() ?: 0x5A15
              repeat(21) { row ->
                repeat(21) { col ->
                  seed = seed * 31 + 7
                  val filled = (seed ushr 3) % 5 < 2
                  val inFinder = (row < 6 && col < 6) || (row < 6 && col > 14) || (row > 14 && col < 6)
                  val draw =
                    if (inFinder) {
                      (row == 0 || row == 5 || col == 0 || col == 5) ||
                        ((row == 2 || row == 3) && (col == 2 || col == 3))
                    } else {
                      filled
                    }
                  if (draw) {
                    drawRect(
                      color = ForestDark,
                      topLeft = Offset(col * cell, row * cell),
                      size = Size(cell + 0.5f, cell + 0.5f),
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "SCAN AT CHECKPOINTS",
            style = MaterialTheme.typography.labelSmall,
            color = Ink.copy(alpha = 0.45f),
            fontWeight = FontWeight.Bold,
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // On-chain proof
      GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "🔗 On-chain identity proof",
            style = MaterialTheme.typography.titleSmall,
            color = Ink,
            fontWeight = FontWeight.ExtraBold,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "keccak256(TouristID + Salt)",
            style = MaterialTheme.typography.labelSmall,
            color = SageMid,
            fontWeight = FontWeight.SemiBold,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = state.onChainHash ?: "pending issuance",
            style = MaterialTheme.typography.bodySmall,
            color = Ink.copy(alpha = 0.72f),
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = state.issuanceNote,
            style = MaterialTheme.typography.bodySmall,
            color = Ink.copy(alpha = 0.55f),
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Validity chips
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Chip(label = "Valid ${identity?.validFrom ?: "—"} → ${identity?.validTo ?: "—"}")
        Chip(label = identity?.status?.label ?: "PENDING")
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Honest note — no fake claims
      Text(
        text =
          "Status: identity issuance & on-chain registration are not yet wired. " +
            "The voucher above is a preview until the identity service connects.",
        style = MaterialTheme.typography.bodySmall,
        color = Ink.copy(alpha = 0.55f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )

      Spacer(modifier = Modifier.height(48.dp))
    }
  }
}

@Composable
private fun Chip(label: String) {
  Surface(
    shape = RoundedCornerShape(50),
    color = SunYellow.copy(alpha = 0.22f),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = Ink,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
    )
  }
}
