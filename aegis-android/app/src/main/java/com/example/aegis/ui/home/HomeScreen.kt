package com.example.aegis.ui.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.example.aegis.Activity
import com.example.aegis.Home
import com.example.aegis.TouristId
import com.example.aegis.Zones
import com.example.aegis.domain.model.SafetyZone
import com.example.aegis.domain.model.TouristIdentity
import com.example.aegis.domain.model.ZoneStatus
import com.example.aegis.domain.usecase.GetTouristIdentityUseCase
import com.example.aegis.domain.usecase.ObserveSafetyZonesUseCase
import com.example.aegis.data.repository.demo.DemoIdentityRepository
import com.example.aegis.data.repository.demo.DemoSafetyZoneRepository
import com.example.aegis.theme.ForestDark
import com.example.aegis.theme.GlassBorder
import com.example.aegis.theme.GlassOnImage
import com.example.aegis.theme.GlassOnImageBorder
import com.example.aegis.theme.GlassScrim
import com.example.aegis.theme.GlassSoftShadow
import com.example.aegis.theme.CautionAmber
import com.example.aegis.theme.DangerRed
import com.example.aegis.theme.GlassSurface
import com.example.aegis.theme.Ink
import com.example.aegis.theme.InkSoft
import com.example.aegis.theme.SafeGreen
import com.example.aegis.ui.ZoneArtwork
import com.example.aegis.ui.components.AegisBackground
import com.example.aegis.ui.components.AegisBottomNavScaffold
import com.example.aegis.ui.components.BottomNavItem
import com.example.aegis.ui.components.FakeQr
import com.example.aegis.ui.components.FilterPill
import com.example.aegis.ui.components.GlassCard
import com.example.aegis.ui.components.MetaItem
import com.example.aegis.ui.components.RegionTag
import com.example.aegis.ui.components.SectionHeader
import com.example.aegis.ui.components.StatusPill
import com.example.aegis.ui.permissions.rememberLocationPermissionState

@Composable
fun HomeScreen(
  viewModel: HomeViewModel,
  onOpenZones: () -> Unit,
  onOpenTouristId: () -> Unit,
  onOpenZoneDetail: (String) -> Unit,
  onSos: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedCategory by remember { mutableIntStateOf(0) }
  val context = LocalContext.current
  val locationPermission = rememberLocationPermissionState()

  val zones by viewModel.zones.collectAsStateWithLifecycle()
  val featured = viewModel.featuredZone.collectAsStateWithLifecycle().value
  val identity by viewModel.identity.collectAsStateWithLifecycle()
  val isTracking by viewModel.isTrackingActive.collectAsStateWithLifecycle()
  val locationText by viewModel.locationText.collectAsStateWithLifecycle()
  val routeDeviationText by viewModel.routeDeviationText.collectAsStateWithLifecycle()
  val isMeshActive by viewModel.isMeshActive.collectAsStateWithLifecycle()
  val activePeerCount by viewModel.activePeerCount.collectAsStateWithLifecycle()

  val navItems =
    listOf(
      BottomNavItem("Home", Icons.Filled.Home, Home),
      BottomNavItem("Map", Icons.Filled.Place, Zones),
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
      // Header — greeting + guardian status widget
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Hi, ${viewModel.touristName} 👏",
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
          )
          Text(
            text = if (isMeshActive) "Offline relay available - $activePeerCount nearby" else "Your guardian is watching over you",
            style = MaterialTheme.typography.bodySmall,
            color = if (isMeshActive) SafeGreen else InkSoft,
          )
        }
        GuardianWidget(status = featured?.status, isMeshActive = isMeshActive, peerCount = activePeerCount)
      }

      // Hero — region tag + big title + scan pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          RegionTag(text = "🇮🇳 MEGHALAYA")
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Safe\nPassage",
            style = MaterialTheme.typography.displayLarge,
            color = Ink,
            lineHeight = 44.sp,
          )
        }
        ScanIdButton(onClick = onOpenTouristId)
      }

      // Category filter pills
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        val categories =
          listOf(
            "Location" to "??",`r`n            "Journey Log" to "??",`r`n            "Offline Relay" to "??",`r`n            "Check-in" to "?",
          )
        categories.forEachIndexed { index, (label, emoji) ->
          FilterPill(
            text = label,
            emoji = emoji,
            selected = index == selectedCategory,
            onClick = {
              selectedCategory = index
              if (label == "Journey Log") onOpenTouristId()
            },
          )
        }
      }

      // Featured safety-zone card with live BlackBox tracking state
      if (featured != null) {
        FeaturedZoneCard(
          zone = featured,
          isTracking = isTracking,
          locationText = locationText,
          routeDeviationText = routeDeviationText,
          onStartRoute = {
            if (!locationPermission.isGranted) locationPermission.request()
            viewModel.startRoute(context, featured.id)
          },
          onStopRoute = {
            viewModel.stopRoute(context)
          },
          onDetailClick = {
            onOpenZoneDetail(featured.id)
          },
        )
      } else {
        GlassCard(modifier = Modifier.fillMaxWidth().height(220.dp)) {
          Text(
            text = "Loading zones…",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
          )
        }
      }

      // Guardian ID strip
      SectionHeader(title = "Your Guardian ID", action = "View", onAction = onOpenTouristId)
      GuardianIdStrip(identity = identity, onClick = onOpenTouristId)
    }

    AegisBottomNavScaffold(
      items = navItems,
      selected = Home,
      onSelect = { key: NavKey ->
        when (key) {
          Zones -> onOpenZones()
          TouristId -> onOpenTouristId()
          Activity ->
            Toast.makeText(context, "📊 Activity log coming soon", Toast.LENGTH_SHORT).show()
          else -> Unit
        }
      },
      onSos = onSos,
      modifier =
        Modifier
          .align(Alignment.BottomCenter)
          .navigationBarsPadding()
          .padding(horizontal = 20.dp)
          .padding(bottom = 10.dp),
    )
  }
}

// ─────────────────────────────────────────────────────────────
// Guardian widget — live safety status of the featured zone.
// ─────────────────────────────────────────────────────────────
@Composable
private fun GuardianWidget(
  status: ZoneStatus?,
  isMeshActive: Boolean = false,
  peerCount: Int = 0,
  modifier: Modifier = Modifier,
) {
  val (emoji, label) =
    if (isMeshActive) {
      "📡" to "Relay Available ($peerCount)"
    } else {
      when (status) {
        ZoneStatus.SAFE -> "🟢" to "Safe Zone"
        ZoneStatus.CAUTION -> "🟡" to "Caution Zone"
        ZoneStatus.HIGH_RISK -> "🔴" to "High Risk"
        ZoneStatus.UNKNOWN -> "⚪" to "Unknown"
        null -> "🛰" to "Guarding"
      }
    }
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(50),
    color = GlassSurface,
    border = BorderStroke(1.dp, if (isMeshActive) SafeGreen.copy(alpha = 0.6f) else GlassBorder),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (isMeshActive) SafeGreen.copy(alpha = 0.25f) else SafeGreen.copy(alpha = 0.16f))
            .border(1.dp, SafeGreen.copy(alpha = 0.55f), CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Text(text = if (isMeshActive) "📡" else "🛰", fontSize = 16.sp)
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = if (isMeshActive) "Relay Available" else "Guardian",
          style = MaterialTheme.typography.labelSmall,
          color = if (isMeshActive) SafeGreen else InkSoft,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = emoji, fontSize = 10.sp)
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color =
              when (status) {
                ZoneStatus.SAFE -> SafeGreen
                ZoneStatus.CAUTION -> CautionAmber
                ZoneStatus.HIGH_RISK -> DangerRed
                ZoneStatus.UNKNOWN -> InkSoft
                null -> Ink
              },
          )
        }
      }
    }
  }
}

// ─────────────────────────────────────────────────────────────
// ScanIdButton — QR verification shortcut (verification itself ships later).
// ─────────────────────────────────────────────────────────────
@Composable
private fun ScanIdButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = RoundedCornerShape(22.dp),
    color = GlassSurface,
    border = BorderStroke(1.dp, GlassBorder),
    shadowElevation = 8.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      FakeQr(modifier = Modifier.size(30.dp), tint = Ink)
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Scan ID",
        style = MaterialTheme.typography.labelSmall,
        color = Ink,
      )
    }
  }
}

// ─────────────────────────────────────────────────────────────
// FeaturedZoneCard — image card with scrim, status, meta row and CTA.
// ─────────────────────────────────────────────────────────────
@Composable
private fun FeaturedZoneCard(
  zone: SafetyZone,
  isJourney Protection: Boolean,
  locationText: String,
  routeDeviationText: String,
  onStartRoute: () -> Unit,
  onStopRoute: () -> Unit,
  onDetailClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val shape = RoundedCornerShape(32.dp)
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .height(370.dp)
        .shadow(24.dp, shape, ambientColor = GlassSoftShadow, spotColor = GlassSoftShadow)
        .clip(shape)
        .background(ForestDark),
  ) {
    Image(
      painter = painterResource(id = ZoneArtwork.imageFor(zone.id)),
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
    Column(
      modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        RegionTag(text = zone.region, dark = true)
        StatusPill(status = zone.status, dark = true)
      }
      Text(
        text = zone.name,
        style = MaterialTheme.typography.headlineLarge,
        color = Color.White,
      )
      Text(
        text = zone.tagline,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.85f),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )

      // Real BlackBox location fix display & Route Corridor Status
      Text(
        text = if (isTracking) "🛰 Journey Protection: $locationText" else "📍 $locationText",
        style = MaterialTheme.typography.labelSmall,
        color = if (isTracking) SafeGreen else Color.White.copy(alpha = 0.75f),
      )
      Text(
        text = routeDeviationText,
        style = MaterialTheme.typography.labelSmall,
        color = if (routeDeviationText.contains("⚠️")) CautionAmber else SafeGreen.copy(alpha = 0.9f),
      )

      Spacer(modifier = Modifier.height(2.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(
          onClick = if (isTracking) onStopRoute else onStartRoute,
          shape = RoundedCornerShape(50),
          color = if (isTracking) DangerRed else GlassOnImage,
          border = BorderStroke(1.dp, GlassOnImageBorder),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = if (isTracking) "Stop Tracking" else "Start Safe Journey",
              style = MaterialTheme.typography.labelLarge,
              color = Color.White,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp),
            )
          }
        }

        Surface(
          onClick = onDetailClick,
          shape = RoundedCornerShape(50),
          color = GlassOnImage.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, GlassOnImageBorder),
        ) {
          Text(
            text = "Details",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
          )
        }
      }
    }
  }
}

// ─────────────────────────────────────────────────────────────
// GuardianIdStrip — local identity voucher (honest, no on-chain claims).
// ─────────────────────────────────────────────────────────────
@Composable
private fun GuardianIdStrip(identity: TouristIdentity?, onClick: () -> Unit, modifier: Modifier = Modifier) {
  GlassCard(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    contentPadding = PaddingValues(16.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier =
          Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ForestDark),
        contentAlignment = Alignment.Center,
      ) {
        FakeQr(modifier = Modifier.size(40.dp), tint = Color.White)
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = identity?.touristId ?: "—",
          style = MaterialTheme.typography.titleMedium,
          color = Ink,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "●", fontSize = 8.sp, color = SafeGreen)
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Local voucher · Offline-first · Preview",
            style = MaterialTheme.typography.labelSmall,
            color = SafeGreen,
          )
        }
      }
      Icon(
        imageVector = Icons.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = InkSoft,
      )
    }
  }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun HomeScreenPreview() {
  com.example.aegis.theme.AEGISTheme {
    HomeScreen(
      viewModel =
        HomeViewModel(
          observeZones = ObserveSafetyZonesUseCase(DemoSafetyZoneRepository()),
          observeIdentity = GetTouristIdentityUseCase(DemoIdentityRepository()),
          blackBoxRepository = com.example.aegis.data.repository.demo.DemoBlackBoxRepository(),
        ),
      onOpenZones = {},
      onOpenTouristId = {},
      onOpenZoneDetail = {},
      onSos = {},
    )
  }
}

