package com.example.aegis.ui.zones

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.example.aegis.Activity
import com.example.aegis.Home
import com.example.aegis.TouristId
import com.example.aegis.Zones
import com.example.aegis.data.MockData
import com.example.aegis.data.SafetyZone
import com.example.aegis.data.ZoneStatus
import com.example.aegis.theme.ForestDark
import com.example.aegis.theme.GlassBorder
import com.example.aegis.theme.GlassSurface
import com.example.aegis.theme.GlassSurfaceStrong
import com.example.aegis.theme.Ink
import com.example.aegis.theme.InkSoft
import com.example.aegis.theme.SageSoft
import com.example.aegis.ui.components.AegisBackground
import com.example.aegis.ui.components.AegisBottomNavScaffold
import com.example.aegis.ui.components.AvatarStack
import com.example.aegis.ui.components.BottomNavItem
import com.example.aegis.ui.components.FilterPill
import com.example.aegis.ui.components.GlassIconButton
import com.example.aegis.ui.components.MetaItem
import com.example.aegis.ui.components.RegionTag
import com.example.aegis.ui.components.SosOverlay
import com.example.aegis.ui.components.StatusPill

@Composable
fun ZonesScreen(
  onBack: () -> Unit,
  onOpenHome: () -> Unit,
  onOpenTouristId: () -> Unit,
  onOpenZoneDetail: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var sosVisible by remember { mutableStateOf(false) }
  var filter by remember { mutableStateOf<ZoneStatus?>(null) }
  val context = LocalContext.current
  val active = MockData.zoneById(MockData.activeZoneId)
  val zones =
    if (filter == null) MockData.zones else MockData.zones.filter { it.status == filter }

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
      // Top bar — back + [star, menu]
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
            contentDescription = "Bookmarked zones",
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
        text = "Safety Zones",
        style = MaterialTheme.typography.displayMedium,
        color = Ink,
      )
      Text(
        text = "${MockData.zones.size} guarded routes near you",
        style = MaterialTheme.typography.bodyMedium,
        color = InkSoft,
      )

      // Active expanded card (mockup's dark top card)
      ActiveZoneCard(zone = active, onClick = { onOpenZoneDetail(active.id) })

      // Status filter pills
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        FilterPill(text = "All", selected = filter == null, onClick = { filter = null })
        ZoneStatus.entries.forEach { status ->
          FilterPill(
            text = status.label,
            emoji = status.emoji,
            selected = filter == status,
            onClick = { filter = status },
          )
        }
      }

      // Stacked peek cards
      StackedZoneCards(zones = zones, onOpenZoneDetail = onOpenZoneDetail)
    }

    AegisBottomNavScaffold(
      items = navItems,
      selected = Zones,
      onSelect = { key: NavKey ->
        when (key) {
          Home -> onOpenHome()
          TouristId -> onOpenTouristId()
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
// ActiveZoneCard — the expanded dark forest card with region,
// status, dates, peer avatars and a circular arrow.
// ─────────────────────────────────────────────────────────────
@Composable
private fun ActiveZoneCard(
  zone: SafetyZone,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(32.dp),
    color = ForestDark,
    shadowElevation = 20.dp,
  ) {
    Column(
      modifier = Modifier.padding(22.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          RegionTag(text = zone.region, dark = true)
          StatusPill(status = zone.status, dark = true)
        }
        Icon(
          imageVector = Icons.Filled.FavoriteBorder,
          contentDescription = "Bookmark zone",
          tint = Color.White.copy(alpha = 0.75f),
          modifier = Modifier.size(20.dp),
        )
      }
      Text(
        text = zone.name,
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        MetaItem(emoji = "📅", text = zone.dates, dark = true)
        MetaItem(emoji = "📍", text = zone.elevation, dark = true)
        MetaItem(emoji = "📡", text = "${zone.peers} peers", dark = true)
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          AvatarStack(peers = zone.peers, dark = true)
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "peers in mesh",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
          )
        }
        Surface(
          shape = CircleShape,
          color = Color.White,
        ) {
          Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Open zone",
              tint = ForestDark,
              modifier = Modifier.size(20.dp),
            )
          }
        }
      }
    }
  }
}

// ─────────────────────────────────────────────────────────────
// StackedZoneCards — peek-through glass cards behind the active
// card, each with image + region + status (mockup "Popular"
// stack).
// ─────────────────────────────────────────────────────────────
@Composable
private fun StackedZoneCards(
  zones: List<SafetyZone>,
  onOpenZoneDetail: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (zones.isEmpty()) {
    Text(
      text = "No zones in this band right now.",
      style = MaterialTheme.typography.bodyMedium,
      color = InkSoft,
      modifier = modifier.padding(vertical = 12.dp),
    )
    return
  }
  val preview = zones.take(3)
  val cardHeight = 104.dp
  val peek = 30.dp
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .height(cardHeight + peek * (preview.size - 1)),
  ) {
    for (i in preview.indices.reversed()) {
      val zone = preview[i]
      Surface(
        onClick = { onOpenZoneDetail(zone.id) },
        modifier =
          Modifier
            .align(Alignment.TopStart)
            .offset(y = (i * peek.value).dp)
            .fillMaxWidth()
            .height(cardHeight)
            .graphicsLayer { alpha = 1f - i * 0.12f },
        shape = RoundedCornerShape(26.dp),
        color = GlassSurfaceStrong,
        border = BorderStroke(1.dp, GlassBorder),
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier.size(76.dp).clip(RoundedCornerShape(18.dp)).background(SageSoft),
          ) {
            Image(
              painter = painterResource(id = zone.imageRes),
              contentDescription = zone.name,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = zone.region,
                style = MaterialTheme.typography.labelSmall,
                color = InkSoft,
              )
              StatusPill(status = zone.status)
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
              text = zone.name,
              style = MaterialTheme.typography.titleMedium,
              color = Ink,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "${zone.duration} · ${zone.elevation}",
              style = MaterialTheme.typography.bodySmall,
              color = InkSoft,
            )
          }
          Icon(
            imageVector = Icons.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = InkSoft,
          )
        }
      }
    }
  }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun ZonesScreenPreview() {
  com.example.aegis.theme.AEGISTheme {
    ZonesScreen(onBack = {}, onOpenHome = {}, onOpenTouristId = {}, onOpenZoneDetail = {})
  }
}
