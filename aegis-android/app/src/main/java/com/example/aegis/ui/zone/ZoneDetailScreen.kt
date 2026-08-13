package com.example.aegis.ui.zone

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import com.example.aegis.theme.CautionAmber
import com.example.aegis.theme.SafeGreen
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aegis.domain.model.SafetyZone
import com.example.aegis.theme.Ink
import com.example.aegis.theme.SageMid
import com.example.aegis.theme.SunYellow
import com.example.aegis.ui.EmergencyViewModel
import com.example.aegis.ui.ZoneArtwork
import com.example.aegis.ui.components.AegisBackground
import com.example.aegis.ui.components.BackButton
import com.example.aegis.ui.components.GlassCard
import com.example.aegis.ui.components.RiskBar
import com.example.aegis.ui.components.SosButton
import com.example.aegis.ui.components.StatusPill
import com.example.aegis.ui.components.avatarStack
import com.example.aegis.ui.components.metaItem

@Composable
fun ZoneDetailScreen(
  zoneId: String,
  viewModel: ZoneDetailViewModel,
  emergencyViewModel: EmergencyViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val emergencyState by emergencyViewModel.uiState.collectAsStateWithLifecycle()
  val zone = state.zone

  Box(modifier = modifier.fillMaxSize()) {
    AegisBackground(modifier = Modifier.fillMaxSize()) {}

    if (zone == null) {
      CircularProgressIndicator(
        modifier = Modifier.align(Alignment.Center),
        color = MaterialTheme.colorScheme.primary,
      )
      return@Box
    }

    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
    ) {
      ZoneBanner(
        zone = zone,
        onBack = onBack,
        modifier = Modifier.fillMaxWidth(),
      )

      // Overlapping content card
      Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 18.dp,
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "🇮🇳 ${zone.region}",
            style = MaterialTheme.typography.labelLarge,
            color = SageMid,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = zone.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = zone.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          Spacer(modifier = Modifier.height(18.dp))

          Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            metaItem("⚠️", "Risk ${zone.riskScore}", "Live")
            metaItem("🛰", "${zone.peers} peers", "Mesh")
            metaItem("⏱", "—", "SOS ETA")
          }

          Spacer(modifier = Modifier.height(20.dp))

          Text(
            text = "Zone Risk Level",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.height(10.dp))
          RiskBar(score = zone.riskScore, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Risk bands: 0–30 Safe · 31–60 Caution · 61–100 High",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          Spacer(modifier = Modifier.height(24.dp))

          if (zone.peers > 0) {
            Text(
              text = "Mesh network peers",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(10.dp))
            avatarStack(peers = zone.peers)
            Spacer(modifier = Modifier.height(24.dp))
          }

          // SOS dispatch card — attempts a real dispatch; result is whatever
          // the transport actually returned (NotAvailable until wired).
          GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(16.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "🚨 Emergency",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f),
              )
              StatusPill(status = zone.status)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text =
                "Dispatch requires a live connection to the AEGIS emergency backend. " +
                  "Your SOS payload includes your Tourist ID and current zone.",
              style = MaterialTheme.typography.bodySmall,
              color = Ink.copy(alpha = 0.72f),
            )
            Spacer(modifier = Modifier.height(14.dp))
            SosButton(
              dispatchLabel = "PRESS TO DISPATCH SOS",
              dispatching = emergencyState.dispatching,
              enabled = !emergencyState.dispatching,
              onClick = {
                emergencyViewModel.dispatch(
                  zoneId = zone.id,
                  latitude = null,
                  longitude = null,
                )
              },
            )
            emergencyState.statusMessage?.let { msg ->
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = if (msg.contains("Delivered")) SafeGreen else CautionAmber,
                fontWeight = FontWeight.Bold,
              )
            }
            emergencyState.error?.let {
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
              )
            }
          }

          // Nearest rescue post
          state.rescuePost?.let { rescuePost ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Nearest rescue post",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(10.dp))
            GlassCard(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(20.dp),
              contentPadding = PaddingValues(0.dp),
            ) {
              Row(modifier = Modifier.padding(14.dp)) {
                Box(
                  modifier =
                    Modifier
                      .size(64.dp)
                      .clip(RoundedCornerShape(14.dp))
                      .background(
                        Brush.linearGradient(
                          listOf(SunYellow.copy(alpha = 0.9f), SunYellow.copy(alpha = 0.55f)),
                        ),
                      ),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(text = "🏥", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = rescuePost.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink,
                    fontWeight = FontWeight.ExtraBold,
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = rescuePost.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.copy(alpha = 0.66f),
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "⏱ ${rescuePost.eta} · ${rescuePost.distance} · ★ ${rescuePost.rating}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SageMid,
                    fontWeight = FontWeight.SemiBold,
                  )
                }
              }
            }
          }

          // Real local check-in rows (Room)
          if (state.checkInCount > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "✅ ${state.checkInCount} local check-in(s) recorded",
              style = MaterialTheme.typography.bodySmall,
              color = SageMid,
              fontWeight = FontWeight.SemiBold,
            )
          }
          state.checkInNotice?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = it,
              style = MaterialTheme.typography.bodySmall,
              color = Ink.copy(alpha = 0.66f),
            )
          }

          Spacer(modifier = Modifier.height(16.dp))
        }
      }

      Spacer(modifier = Modifier.height(96.dp))
    }

    // "I'm Safe" FAB — records a real local check-in row (offline-first).
    Surface(
      onClick = viewModel::checkIn,
      modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
      shape = CircleShape,
      color = SunYellow,
      shadowElevation = 12.dp,
    ) {
      Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
        Text(
          text = "✓",
          style = MaterialTheme.typography.headlineSmall,
          color = Ink,
          fontWeight = FontWeight.Black,
        )
      }
    }
  }
}

@Composable
private fun ZoneBanner(
  zone: SafetyZone,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val artworkRes = ZoneArtwork.imageFor(zone.id)

  Box(
    modifier =
      modifier
        .aspectRatio(1f)
        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
  ) {
    Image(
      painter = painterResource(artworkRes),
      contentDescription = zone.name,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(
                Color.Black.copy(alpha = 0.25f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.45f),
              ),
            ),
          ),
    )
    BackButton(
      onClick = onBack,
      modifier =
        Modifier
          .align(Alignment.TopStart)
          .statusBarsPadding()
          .padding(16.dp),
    )
    StatusPill(
      status = zone.status,
      modifier =
        Modifier
          .align(Alignment.TopEnd)
          .statusBarsPadding()
          .padding(16.dp),
    )
    Column(
      modifier =
        Modifier
          .align(Alignment.BottomStart)
          .padding(20.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "⛰ ${zone.elevation}",
          style = MaterialTheme.typography.labelMedium,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          modifier =
            Modifier
              .clip(RoundedCornerShape(50))
              .background(Color.Black.copy(alpha = 0.35f))
              .padding(horizontal = 10.dp, vertical = 5.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "📍 ${zone.region} region",
          style = MaterialTheme.typography.labelMedium,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          modifier =
            Modifier
              .clip(RoundedCornerShape(50))
              .background(Color.Black.copy(alpha = 0.35f))
              .padding(horizontal = 10.dp, vertical = 5.dp),
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = zone.name,
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White,
        fontWeight = FontWeight.ExtraBold,
      )
    }
  }
}
