package com.example.aegis.ui.id

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aegis.identity.CanonicalIdentityHash
import com.example.aegis.qr.QrCodeGenerator
import com.example.aegis.theme.ForestDark
import com.example.aegis.theme.Ink
import com.example.aegis.theme.SafeGreen
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

  val touristId = identity?.touristId ?: "TST-MEGHALAYA-101"
  val salt = "AEGIS-SALT-2026"
  val canonicalHash = remember(touristId) { CanonicalIdentityHash.computeCanonicalHash(touristId, salt) }

  val qrPayload = remember(touristId, canonicalHash) {
    """{"pseudonymousId":"$touristId","idHash":"$canonicalHash","issuer":"AEGIS Authority Meghalaya"}"""
  }

  val qrBitmap = remember(qrPayload) {
    QrCodeGenerator.generateQrCodeBitmap(qrPayload)
  }

  val isOnChainConfirmed = state.issuanceNote.contains("confirmed", ignoreCase = true) || state.issuanceNote.contains("Sepolia", ignoreCase = true)

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
          "Your identity commitment voucher. The on-chain hash is the only commitment stored on the " +
            "public blockchain — zero raw passport, Aadhaar, or phone numbers.",
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
            text = touristId,
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            fontWeight = FontWeight.ExtraBold,
          )
          Spacer(modifier = Modifier.height(16.dp))

          // Real 2D QR Code Bitmap
          Box(
            modifier =
              Modifier
                .size(180.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center,
          ) {
            Image(
              bitmap = qrBitmap,
              contentDescription = "Real Tourist Identity QR Code",
              modifier = Modifier.fillMaxSize(),
            )
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
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "🔗 On-chain identity commitment",
              style = MaterialTheme.typography.titleSmall,
              color = Ink,
              fontWeight = FontWeight.ExtraBold,
            )
            Surface(
              shape = RoundedCornerShape(50),
              color = if (isOnChainConfirmed) SafeGreen.copy(alpha = 0.2f) else SunYellow.copy(alpha = 0.2f),
            ) {
              Text(
                text = if (isOnChainConfirmed) "ON CHAIN" else "PENDING ON-CHAIN SYNC",
                style = MaterialTheme.typography.labelSmall,
                color = if (isOnChainConfirmed) SafeGreen else Ink,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "keccak256(TouristID + \":\" + Salt)",
            style = MaterialTheme.typography.labelSmall,
            color = SageMid,
            fontWeight = FontWeight.SemiBold,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = canonicalHash,
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
        GlassCard(modifier = Modifier.weight(1f), shape = RoundedCornerShape(18.dp)) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "Status", style = MaterialTheme.typography.labelSmall, color = Ink.copy(alpha = 0.5f))
            Text(text = "🟢 Active", style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.Bold)
          }
        }
        GlassCard(modifier = Modifier.weight(1f), shape = RoundedCornerShape(18.dp)) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "Valid Until", style = MaterialTheme.typography.labelSmall, color = Ink.copy(alpha = 0.5f))
            Text(text = "20 AUG 2026", style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}
