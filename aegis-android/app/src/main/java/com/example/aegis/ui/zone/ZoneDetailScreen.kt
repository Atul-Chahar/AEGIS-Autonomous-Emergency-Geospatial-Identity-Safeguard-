package com.example.aegis.ui.zone

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aegis.data.MockData
import com.example.aegis.data.SafetyZone
import com.example.aegis.data.ZoneStatus
import com.example.aegis.theme.CautionAmber
import com.example.aegis.theme.DangerRed
import com.example.aegis.theme.ForestDark
import com.example.aegis.theme.GlassBorder
import com.example.aegis.theme.GlassOnImage
import com.example.aegis.theme.GlassOnImageBorder
import com.example.aegis.theme.GlassScrim
import com.example.aegis.theme.GlassSurface
import com.example.aegis.theme.GlassSurfaceStrong
import com.example.aegis.theme.Ink
import com.example.aegis.theme.InkSoft
import com.example.aegis.theme.SafeGreen
import com.example.aegis.theme.SunYellow
import com.example.aegis.ui.components.AvatarStack
import com.example.aegis.ui.components.GlassCard
import com.example.aegis.ui.components.GlassIconButton
import com.example.aegis.ui.components.MetaItem
import com.example.aegis.ui.components.RegionTag
import com.example.aegis.ui.components.RiskMeter
import com.example.aegis.ui.components.SectionHeader
import com.example.aegis.ui.components.SosOverlay
import com.example.aegis.ui.components.StatusPill
import com.example.aegis.ui.components.SunFab

@Composable
fun ZoneDetailScreen(
  zoneId: String,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val zone = MockData.zoneById(zoneId)
  var sosVisible by remember { mutableStateOf(false) }
  var checkedIn by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
    ) {
      // ── Full-bleed banner ──────────────────────────────────
      Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
        Image(
          painter = painterResource(id = zone.imageRes),
          contentDescription = zone.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
        Box(
          modifier =
            Modifier
              .fillMaxSize()
              .background(Brush.verticalGradient(listOf(Color.Transparent, GlassScrim))),
        )

        // Top overlays — back + live guardian pill
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .statusBarsPadding()
              .padding(20.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          GlassIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            onClick = onBack,
            dark = true,
          )
          GuardianLivePill(status = zone.status)
        }

        // Bottom overlays — risk badge + title (left), support status (right)
        Row(
          modifier =
            Modifier
              .align(Alignment.BottomStart)
              .fillMaxWidth()
              .padding(20.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom,
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = GlassOnImage,
              border = BorderStroke(1.dp, GlassOnImageBorder),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(text = "⚠️", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Risk ${zone.riskScore} · ${zone.status.label}",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White,
                )
              }
            }
            Text(
              text = zone.name,
              style = MaterialTheme.typography.displayMedium,
              color = Color.White,
            )
          }
          Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AvatarStack(peers = zone.peers, size = 34.dp, dark = true)
            Text(
              text = if (zone.peers > 0) "Nearby relay support" else "Relay support limited",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.85f),
            )
          }
        }
      }

      // ── Overlapping content card ───────────────────────────
      Box(modifier = Modifier.offset(y = (-28).dp).padding(horizontal = 20.dp)) {
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(32.dp),
          color = GlassSurfaceStrong.copy(alpha = 0.95f),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(22.dp),
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                RegionTag(text = zone.region)
                StatusPill(status = zone.status)
              }
              Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = InkSoft,
              )
            }

            Text(
              text = "Discovering the Magic of Meghalaya's Monsoon",
              style = MaterialTheme.typography.headlineSmall,
              color = Ink,
            )
            Text(
              text = safetyDescription(zone),
              style = MaterialTheme.typography.bodyMedium,
              color = InkSoft,
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              MetaItem(emoji = "📅", text = zone.dates)
              MetaItem(emoji = "📍", text = zone.elevation)
                MetaItem(emoji = "🛟", text = if (zone.peers > 0) "Relay ready" else "Relay limited")
            }

            RiskMeter(score = zone.riskScore)

            // ── SOS dispatch card ─────────────────────────────
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(24.dp),
              color = ForestDark,
              border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            ) {
              Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(text = "🚨", fontSize = 22.sp)
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = "Emergency SOS",
                      style = MaterialTheme.typography.titleMedium,
                      color = Color.White,
                    )
                    Text(
                      text = "Opens press-and-hold confirmation",
                      style = MaterialTheme.typography.bodySmall,
                      color = Color.White.copy(alpha = 0.7f),
                    )
                  }
                }
                Button(
                  onClick = { sosVisible = true },
                  shape = RoundedCornerShape(16.dp),
                  colors =
                    ButtonDefaults.buttonColors(
                      containerColor = DangerRed,
                      contentColor = Color.White,
                    ),
                  modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                  Text(
                    text = "OPEN SOS CONFIRMATION",
                    style = MaterialTheme.typography.labelLarge,
                  )
                }
              }
            }
          }
        }
      }

      // ── Nearest rescue post ────────────────────────────────
      Column(
        modifier = Modifier.padding(horizontal = 20.dp).padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        SectionHeader(title = "Nearest Rescue Post")
        RescuePostCard(zone = zone)
        Spacer(modifier = Modifier.height(40.dp))
      }
    }

    // Floating "I'm Safe" check-in (thumb zone)
    SunFab(
      onClick = { checkedIn = !checkedIn },
      modifier =
        Modifier
          .align(Alignment.BottomEnd)
          .navigationBarsPadding()
          .padding(20.dp),
    )

    if (checkedIn) {
      Surface(
        modifier =
          Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 96.dp),
        shape = RoundedCornerShape(50),
        color = SafeGreen,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
        shadowElevation = 12.dp,
      ) {
        Text(
          text = "✓ Safety check saved locally",
          style = MaterialTheme.typography.labelMedium,
          color = Color.White,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
      }
    }

    if (sosVisible) {
      SosOverlay(onDismiss = { sosVisible = false })
    }
  }
}

private fun safetyDescription(zone: SafetyZone): String =
  when (zone.status) {
    ZoneStatus.SAFE ->
      "${zone.name} is currently suitable for a calm journey. Stay on the planned route and keep check-ins active."
    ZoneStatus.CAUTION ->
      "${zone.name} needs extra attention today. Expect slippery sections and keep Journey Protection active."
    ZoneStatus.HIGH_RISK ->
      "${zone.name} requires caution. Avoid hiking alone and follow local safety guidance before continuing."
  }

// ─────────────────────────────────────────────────────────────
// GuardianLivePill — dark glass status pill over the banner.
// ─────────────────────────────────────────────────────────────
@Composable
private fun GuardianLivePill(status: ZoneStatus, modifier: Modifier = Modifier) {
  val dot =
    when (status) {
      ZoneStatus.SAFE -> SafeGreen
      ZoneStatus.CAUTION -> CautionAmber
      ZoneStatus.HIGH_RISK -> DangerRed
    }
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(50),
    color = GlassOnImage,
    border = BorderStroke(1.dp, GlassOnImageBorder),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier.size(8.dp).clip(CircleShape).background(dot),
      )
      Spacer(modifier = Modifier.width(7.dp))
      Text(
        text = "Live · ${status.label}",
        style = MaterialTheme.typography.labelMedium,
        color = Color.White,
      )
    }
  }
}

// ─────────────────────────────────────────────────────────────
// RescuePostCard — the mockup's hotel sub-card, now the nearest
// rescue/checkpoint post.
// ─────────────────────────────────────────────────────────────
@Composable
private fun RescuePostCard(zone: SafetyZone, modifier: Modifier = Modifier) {
  val post = MockData.rescuePost
  GlassCard(
    onClick = {},
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier.size(88.dp).clip(RoundedCornerShape(20.dp)),
      ) {
        Image(
          painter = painterResource(id = post.imageRes),
          contentDescription = post.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
          text = post.name,
          style = MaterialTheme.typography.titleMedium,
          color = Ink,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = "📍 ${post.location}",
          style = MaterialTheme.typography.bodySmall,
          color = InkSoft,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "★", fontSize = 12.sp, color = SunYellow)
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = post.rating,
            style = MaterialTheme.typography.labelMedium,
            color = Ink,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = post.distance,
            style = MaterialTheme.typography.labelMedium,
            color = SafeGreen,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "${post.eta} ETA",
            style = MaterialTheme.typography.labelSmall,
            color = InkSoft,
          )
        }
      }
    }
  }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 400, heightDp = 880)
@Composable
private fun ZoneDetailScreenPreview() {
  com.example.aegis.theme.AEGISTheme {
    ZoneDetailScreen(zoneId = MockData.activeZoneId, onBack = {})
  }
}
