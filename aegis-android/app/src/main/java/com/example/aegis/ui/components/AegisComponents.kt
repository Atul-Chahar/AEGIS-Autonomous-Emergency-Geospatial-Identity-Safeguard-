package com.example.aegis.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavKey
import com.example.aegis.domain.model.SosDispatchResult
import com.example.aegis.domain.model.ZoneStatus
import com.example.aegis.theme.CautionAmber
import com.example.aegis.theme.DangerRed
import com.example.aegis.theme.ForestDark
import com.example.aegis.theme.ForestDeep
import com.example.aegis.theme.GlassBorder
import com.example.aegis.theme.GlassOnImage
import com.example.aegis.theme.GlassOnImageBorder
import com.example.aegis.theme.GlassSoftShadow
import com.example.aegis.theme.GlassSurface
import com.example.aegis.theme.Ink
import com.example.aegis.theme.InkSoft
import com.example.aegis.theme.LimeGlow
import com.example.aegis.theme.MeshCyan
import com.example.aegis.theme.SafeGreen
import com.example.aegis.theme.SageLight
import com.example.aegis.theme.SageMid
import com.example.aegis.theme.SagePale
import com.example.aegis.theme.SageSoft
import com.example.aegis.theme.SunYellow
import com.example.aegis.ui.state.GuardianLevel
import com.example.aegis.ui.state.GuardianSystemState
import com.example.aegis.ui.state.SosProgressStep
import com.example.aegis.ui.state.SosStepStatus
import com.example.aegis.ui.state.buildSosSteps
import com.example.aegis.ui.state.offlineMessageFor
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────
// AegisBackground — full-screen sage gradient with soft "liquid"
// glow blobs (radial gradients, renders on every API level).
// ─────────────────────────────────────────────────────────────
@Composable
fun AegisBackground(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(listOf(SagePale, SageLight, SageSoft))),
  ) {
    Box(
      modifier =
        Modifier
          .align(Alignment.TopEnd)
          .offset(x = 60.dp, y = (-70).dp)
          .size(260.dp)
          .background(
            Brush.radialGradient(
              listOf(SunYellow.copy(alpha = 0.45f), Color.Transparent),
              center = Offset(0.5f, 0.5f),
            ),
          ),
    )
    Box(
      modifier =
        Modifier
          .align(Alignment.BottomStart)
          .offset(x = (-60).dp, y = 80.dp)
          .size(320.dp)
          .background(
            Brush.radialGradient(
              listOf(SageMid.copy(alpha = 0.5f), Color.Transparent),
              center = Offset(0.5f, 0.5f),
            ),
          ),
    )
    Box(
      modifier =
        Modifier
          .align(Alignment.BottomEnd)
          .offset(x = 90.dp, y = 60.dp)
          .size(240.dp)
          .background(
            Brush.radialGradient(
              listOf(LimeGlow.copy(alpha = 0.3f), Color.Transparent),
              center = Offset(0.5f, 0.5f),
            ),
          ),
    )
    content()
  }
}

// ─────────────────────────────────────────────────────────────
// GlassCard — frosted white card with a soft sage-tinted shadow
// and a hairline white border. The core "liquid glass" surface.
// ─────────────────────────────────────────────────────────────
@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(28.dp),
  color: Color = GlassSurface,
  border: Color = GlassBorder,
  contentPadding: PaddingValues = PaddingValues(20.dp),
  onClick: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  val elevated =
    modifier.shadow(elevation = 20.dp, shape = shape, ambientColor = GlassSoftShadow, spotColor = GlassSoftShadow)
  if (onClick != null) {
    Surface(
      onClick = onClick,
      modifier = elevated,
      shape = shape,
      color = color,
      border = BorderStroke(1.dp, border),
    ) {
      Column(modifier = Modifier.padding(contentPadding), content = content)
    }
  } else {
    Surface(
      modifier = elevated,
      shape = shape,
      color = color,
      border = BorderStroke(1.dp, border),
    ) {
      Column(modifier = Modifier.padding(contentPadding), content = content)
    }
  }
}

// ─────────────────────────────────────────────────────────────
// FilterPill — selectable category chip: dark forest when
// selected, frosted white otherwise (mirrors the mockup pills).
// ─────────────────────────────────────────────────────────────
@Composable
fun FilterPill(
  text: String,
  selected: Boolean,
  onClick: () -> Unit,
  emoji: String? = null,
  modifier: Modifier = Modifier,
) {
  Surface(
    onClick = onClick,
    modifier = modifier.height(42.dp),
    shape = RoundedCornerShape(50),
    color = if (selected) ForestDark else GlassSurface,
    border = BorderStroke(1.dp, if (selected) Color.Transparent else GlassBorder),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      if (emoji != null) {
        Text(text = emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) Color.White else Ink,
      )
    }
  }
}

// ─────────────────────────────────────────────────────────────
// StatusPill — tinted status chip (Safe / Caution / High Risk).
// ─────────────────────────────────────────────────────────────
@Composable
fun StatusPill(
  status: ZoneStatus,
  dark: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val color =
    when (status) {
      ZoneStatus.SAFE -> SafeGreen
      ZoneStatus.CAUTION -> CautionAmber
      ZoneStatus.HIGH_RISK -> DangerRed
      ZoneStatus.UNKNOWN -> InkSoft
    }
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(50),
    color = color.copy(alpha = if (dark) 0.28f else 0.14f),
    border = BorderStroke(1.dp, color.copy(alpha = if (dark) 0.6f else 0.45f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(text = status.emoji, fontSize = 12.sp)
      Spacer(modifier = Modifier.width(5.dp))
      Text(
        text = status.label,
        style = MaterialTheme.typography.labelSmall,
        color = if (dark) Color.White else color,
      )
    }
  }
}

// ─────────────────────────────────────────────────────────────
// RegionTag — flag + region pill (🇮🇳 MEGHALAYA).
// ─────────────────────────────────────────────────────────────
@Composable
fun RegionTag(
  text: String,
  dark: Boolean = false,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(50),
    color = if (dark) Color.White.copy(alpha = 0.14f) else GlassSurface,
    border = BorderStroke(1.dp, if (dark) Color.White.copy(alpha = 0.22f) else GlassBorder),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = if (dark) Color.White else Ink,
      modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
    )
  }
}

// ─────────────────────────────────────────────────────────────
// MetaItem — "📅 7 days" style inline metadata.
// ─────────────────────────────────────────────────────────────
@Composable
fun MetaItem(
  emoji: String,
  text: String,
  modifier: Modifier = Modifier,
  tint: Color = Ink,
  dark: Boolean = false,
) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    Text(text = emoji, fontSize = 13.sp)
    Spacer(modifier = Modifier.width(5.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.labelMedium,
      color = if (dark) Color.White.copy(alpha = 0.85f) else tint,
    )
  }
}

// ─────────────────────────────────────────────────────────────
// AvatarStack — overlapping nearby-support avatars (initials on sage
// gradients, mockup style).
// ─────────────────────────────────────────────────────────────
private val avatarPalette =
  listOf(
    Brush.linearGradient(listOf(Color(0xFF8FB565), Color(0xFF4C6A39))),
    Brush.linearGradient(listOf(MeshCyan, Color(0xFF0E6E86))),
    Brush.linearGradient(listOf(Color(0xFFF5A623), Color(0xFFC47B12))),
    Brush.linearGradient(listOf(Color(0xFF9B6BC4), Color(0xFF6B3E96))),
  )

@Composable
fun AvatarStack(
  peers: Int,
  modifier: Modifier = Modifier,
  size: Dp = 30.dp,
  dark: Boolean = false,
) {
  val shown = minOf(peers, avatarPalette.size)
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    for (i in 0 until shown) {
      Box(
        modifier =
          Modifier
            .size(size)
            .offset(x = if (i == 0) 0.dp else -(size / 3f))
            .clip(CircleShape)
            .background(avatarPalette[i])
            .border(
              width = 2.dp,
              color = if (dark) Color.White.copy(alpha = 0.35f) else Color.White,
              shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = ('A' + i).toString(),
          fontSize = (size.value * 0.42f).sp,
          fontWeight = FontWeight.Bold,
          color = Color.White,
        )
      }
    }
    if (peers > shown) {
      Box(
        modifier =
          Modifier
            .offset(x = -(size / 3f) * 2)
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.85f))
            .border(width = 2.dp, color = Color.White, shape = CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "+${peers - shown}",
          fontSize = (size.value * 0.32f).sp,
          fontWeight = FontWeight.Bold,
          color = ForestDark,
        )
      }
    }
  }
}

// ─────────────────────────────────────────────────────────────
// GlassIconButton — circular frosted button (back / menu / star).
// ─────────────────────────────────────────────────────────────
@Composable
fun GlassIconButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  dark: Boolean = false,
  size: Dp = 44.dp,
) {
  Surface(
    onClick = onClick,
    modifier = modifier.size(size),
    shape = CircleShape,
    color = if (dark) GlassOnImage else GlassSurface,
    border = BorderStroke(1.dp, if (dark) GlassOnImageBorder else GlassBorder),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = if (dark) Color.White else Ink,
        modifier = Modifier.size(size * 0.46f),
      )
    }
  }
}

// ─────────────────────────────────────────────────────────────
// RiskMeter — live risk score bar with a marker on the
// Safe → Caution → High band.
// ─────────────────────────────────────────────────────────────
@Composable
fun GuardianStatePill(
  state: GuardianSystemState,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val color =
    when (state.level) {
      GuardianLevel.ACTIVE -> SafeGreen
      GuardianLevel.LIMITED -> SunYellow
      GuardianLevel.ATTENTION -> CautionAmber
      GuardianLevel.EMERGENCY -> DangerRed
    }

  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = RoundedCornerShape(22.dp),
    color = GlassSurface,
    border = BorderStroke(1.dp, color.copy(alpha = 0.48f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
      Column {
        Text(text = state.level.title, style = MaterialTheme.typography.labelMedium, color = Ink)
        Text(text = state.level.subtitle, style = MaterialTheme.typography.labelSmall, color = InkSoft)
      }
    }
  }
}

@Composable
fun RiskMeter(score: Int, modifier: Modifier = Modifier) {
  val bandColor =
    when {
      score >= 61 -> DangerRed
      score >= 31 -> CautionAmber
      else -> SafeGreen
    }
  Column(modifier = modifier) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "LIVE RISK SCORE",
        style = MaterialTheme.typography.labelSmall,
        color = InkSoft,
      )
      Text(
        text = "$score / 100",
        style = MaterialTheme.typography.labelLarge,
        color = bandColor,
      )
    }
    Spacer(modifier = Modifier.height(8.dp))
    BoxWithConstraints(
      modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(50)),
    ) {
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(Brush.horizontalGradient(listOf(SafeGreen, CautionAmber, DangerRed))),
      )
      val markerTravel = maxWidth - 14.dp
      Box(
        modifier =
          Modifier
            .offset(x = markerTravel * (score.coerceIn(0, 100) / 100f))
            .size(14.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, bandColor, CircleShape),
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text(text = "Safe", style = MaterialTheme.typography.labelSmall, color = SafeGreen)
      Text(text = "Caution", style = MaterialTheme.typography.labelSmall, color = CautionAmber)
      Text(text = "High Risk", style = MaterialTheme.typography.labelSmall, color = DangerRed)
    }
  }
}

// ─────────────────────────────────────────────────────────────
// FakeQr — deterministic QR-style pattern drawn on a Canvas
// (no bitmap assets needed, offline-safe).
// ─────────────────────────────────────────────────────────────
@Composable
fun FakeQr(modifier: Modifier = Modifier, tint: Color = Ink) {
  Canvas(modifier = modifier) {
    val cells = 23
    val cell = size.minDimension / cells
    var seed = 0x5EED_2026

    fun inFinder(row: Int, col: Int): Boolean {
      val corner = { r: Int, c: Int -> row in r until r + 7 && col in c until c + 7 }
      return corner(0, 0) || corner(0, cells - 7) || corner(cells - 7, 0)
    }

    for (row in 0 until cells) {
      for (col in 0 until cells) {
        if (inFinder(row, col)) continue
        seed = seed * 1103515245 + 12345
        val on = ((seed ushr 16) and 1) == 1
        if (on) {
          drawRect(color = tint, topLeft = Offset(col * cell, row * cell), size = Size(cell, cell))
        }
      }
    }

    fun drawFinder(row: Int, col: Int) {
      val left = col * cell
      val top = row * cell
      drawRect(color = tint, topLeft = Offset(left, top), size = Size(cell * 7, cell * 7))
      drawRect(
        color = Color.White,
        topLeft = Offset(left + cell, top + cell),
        size = Size(cell * 5, cell * 5),
      )
      drawRect(
        color = tint,
        topLeft = Offset(left + cell * 2, top + cell * 2),
        size = Size(cell * 3, cell * 3),
      )
    }

    drawFinder(0, 0)
    drawFinder(0, cells - 7)
    drawFinder(cells - 7, 0)
  }
}

// ─────────────────────────────────────────────────────────────
// AegisBottomNav — floating dark glass pill with five slots and
// a raised red SOS trigger in the center (thumb zone, per mockup).
// ─────────────────────────────────────────────────────────────
data class BottomNavItem(
  val label: String,
  val icon: ImageVector,
  val key: NavKey,
)

@Composable
fun AegisBottomNavScaffold(
  items: List<BottomNavItem>,
  selected: NavKey,
  onSelect: (NavKey) -> Unit,
  onSos: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxWidth()) {
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(34.dp),
      color = ForestDeep.copy(alpha = 0.94f),
      border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
      shadowElevation = 24.dp,
    ) {
      Row(
        modifier = Modifier.height(68.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        items.take(2).forEach {
          NavSlot(item = it, selected = selected, onSelect = onSelect, modifier = Modifier.weight(1f))
        }
        Box(modifier = Modifier.weight(1f)) // center gap for the raised SOS
        items.drop(2).forEach {
          NavSlot(item = it, selected = selected, onSelect = onSelect, modifier = Modifier.weight(1f))
        }
      }
    }
    // Raised SOS trigger
    Box(
      modifier = Modifier.align(Alignment.Center).offset(y = (-14).dp).size(64.dp),
      contentAlignment = Alignment.Center,
    ) {
      Surface(
        onClick = onSos,
        modifier = Modifier.size(58.dp),
        shape = CircleShape,
        color = DangerRed,
        border = BorderStroke(3.dp, Color.White.copy(alpha = 0.9f)),
        shadowElevation = 16.dp,
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(text = "🚨", fontSize = 22.sp)
        }
      }
    }
  }
}

@Composable
private fun NavSlot(
  item: BottomNavItem,
  selected: NavKey,
  onSelect: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
) {
  val isSelected = selected == item.key
  Column(
    modifier =
      modifier
        .height(68.dp)
        .clip(RoundedCornerShape(24.dp))
        .clickable { onSelect(item.key) },
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = item.icon,
      contentDescription = item.label,
      tint = if (isSelected) SunYellow else Color.White.copy(alpha = 0.55f),
      modifier = Modifier.size(22.dp),
    )
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = item.label,
      style = MaterialTheme.typography.labelSmall,
      color = if (isSelected) SunYellow else Color.White.copy(alpha = 0.55f),
    )
  }
}

// ─────────────────────────────────────────────────────────────
// SosOverlay — full-screen emergency sheet with press-and-hold
// confirmation and honest transport progress (spec §7). A step only
// shows a checkmark when the real dispatch state says it succeeded.
// ─────────────────────────────────────────────────────────────
@Composable
fun SosOverlay(
  payloadPreview: String?,
  dispatching: Boolean,
  dispatchResult: SosDispatchResult?,
  hasLocationFix: Boolean,
  blackBoxAttached: Boolean,
  onDispatch: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val steps = buildSosSteps(dispatching, dispatchResult, hasLocationFix, blackBoxAttached)
  val offlineMessage = offlineMessageFor(dispatchResult)

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier = modifier.fillMaxSize().background(ForestDeep.copy(alpha = 0.72f)),
      contentAlignment = Alignment.Center,
    ) {
      GlassCard(
        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFAFFFDF8),
        contentPadding = PaddingValues(22.dp),
      ) {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
            modifier = Modifier.fillMaxWidth().height(88.dp),
            contentAlignment = Alignment.Center,
          ) {
            PulsingRing()
            Box(
              modifier = Modifier.size(60.dp).clip(CircleShape).background(DangerRed),
              contentAlignment = Alignment.Center,
            ) {
              Text(text = "🚨", fontSize = 26.sp)
            }
          }

          Text(
            text = "EMERGENCY SOS",
            style = MaterialTheme.typography.headlineMedium,
            color = DangerRed,
            modifier = Modifier.align(Alignment.CenterHorizontally),
          )
          Text(
            text =
              "Your ID, location and Journey Protection record are prepared locally. " +
                "Hold to confirm and send through every available channel.",
            style = MaterialTheme.typography.bodyMedium,
            color = InkSoft,
          )

          // Honest transport progress — checkmarks only on real success.
          steps.forEach { step ->
            SosProgressRow(step = step)
          }

          offlineMessage?.let { message ->
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = CautionAmber.copy(alpha = 0.14f),
              border = BorderStroke(1.dp, CautionAmber.copy(alpha = 0.5f)),
            ) {
              Text(
                text = message,
                style = MaterialTheme.typography.labelMedium,
                color = CautionAmber,
                modifier = Modifier.padding(12.dp),
              )
            }
          }

          // Technical payload detail (kept small and secondary).
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = ForestDark.copy(alpha = 0.06f),
            border = BorderStroke(1.dp, ForestDark.copy(alpha = 0.1f)),
          ) {
            Text(
              text = payloadPreview ?: "Preparing payload…",
              style = MaterialTheme.typography.labelSmall,
              color = InkSoft,
              modifier = Modifier.padding(10.dp),
            )
          }

          PressAndHoldSosButton(
            label = if (dispatching) "SENDING…" else "PRESS & HOLD TO SEND",
            enabled = !dispatching,
            onComplete = onDispatch,
            modifier = Modifier.fillMaxWidth().height(58.dp),
          )

          TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterHorizontally),
          ) {
            Text(
              text = "Cancel",
              color = InkSoft,
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }
      }
    }
  }
}

/** One honest step row: spinner for in-progress, check only when succeeded. */
@Composable
private fun SosProgressRow(step: SosProgressStep, modifier: Modifier = Modifier) {
  val (tint, glyph) =
    when (step.status) {
      SosStepStatus.SUCCEEDED -> SafeGreen to "✓"
      SosStepStatus.IN_PROGRESS -> CautionAmber to "◌"
      SosStepStatus.FAILED -> DangerRed to "✕"
      SosStepStatus.PENDING -> InkSoft to "○"
    }
  Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier.size(24.dp).clip(CircleShape).background(tint.copy(alpha = 0.16f)),
      contentAlignment = Alignment.Center,
    ) {
      if (step.status == SosStepStatus.IN_PROGRESS) {
        CircularProgressIndicator(
          modifier = Modifier.size(14.dp),
          color = tint,
          strokeWidth = 2.dp,
        )
      } else {
        Text(text = glyph, style = MaterialTheme.typography.labelSmall, color = tint, fontWeight = FontWeight.Bold)
      }
    }
    Spacer(modifier = Modifier.width(10.dp))
    Text(
      text = step.label,
      style = MaterialTheme.typography.labelMedium,
      color = if (step.status == SosStepStatus.SUCCEEDED) Ink else InkSoft,
      modifier = Modifier.weight(1f),
    )
  }
}

/** Press-and-hold dispatch button — reduces accidental activation (spec §7). */
@Composable
fun PressAndHoldSosButton(
  label: String,
  onComplete: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  holdMillis: Int = 1400,
) {
  var pressed by remember { mutableStateOf(false) }
  var progress by remember { mutableFloatStateOf(0f) }
  var fired by remember { mutableStateOf(false) }

  // Animate hold progress while the finger is down. Keyed only on `pressed`:
  // `enabled` toggles while a dispatch is in flight, and including it in the
  // keys would restart this effect mid-hold and re-fire onComplete repeatedly.
  LaunchedEffect(pressed) {
    if (!pressed) {
      progress = 0f
      fired = false
      return@LaunchedEffect
    }
    val start = System.nanoTime()
    while (pressed) {
      if (!enabled) {
        // Dispatch in flight — abort and reset without re-arming.
        progress = 0f
        fired = true
        return@LaunchedEffect
      }
      progress = ((System.nanoTime() - start) / 1_000_000f / holdMillis).coerceIn(0f, 1f)
      if (progress >= 1f && !fired) {
        fired = true
        onComplete()
        progress = 1f
        break
      }
      delay(16)
    }
  }

  Surface(
    modifier = modifier.pointerInput(enabled) {
      awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        if (!enabled) return@awaitEachGesture
        pressed = true
        waitForUpOrCancellation()
        pressed = false
      }
    },
    shape = RoundedCornerShape(18.dp),
    color = if (enabled) DangerRed else DangerRed.copy(alpha = 0.55f),
  ) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      // Accurate hold-progress fill: sweeps left → right inside the rounded
      // button (a full ring gets clipped into a broken arc by the wide shape).
      Box(
        modifier =
          Modifier
            .align(Alignment.CenterStart)
            .fillMaxHeight()
            .fillMaxWidth(progress)
            .background(Color.White.copy(alpha = 0.20f)),
      )
      // Leading edge of the fill so the sweep is visible against the red.
      Box(
        modifier =
          Modifier
            .align(Alignment.CenterStart)
            .fillMaxHeight()
            .fillMaxWidth(progress)
            .padding(0.dp),
      ) {
        Box(
          modifier =
            Modifier
              .align(Alignment.CenterStart)
              .fillMaxHeight()
              .width(3.dp)
              .background(Color.White.copy(alpha = 0.9f)),
        )
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelLarge,
          color = Color.White,
          fontWeight = FontWeight.Bold,
        )
        if (enabled) {
          Text(
            text = "Hold ${holdMillis / 1000}.${(holdMillis % 1000) / 100}s",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.75f),
          )
        }
      }
    }
  }
}

@Composable
private fun PulsingRing() {
  val transition = rememberInfiniteTransition()
  val scale by
    transition.animateFloat(
      initialValue = 1f,
      targetValue = 2.1f,
      animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1300)),
    )
  val alpha by
    transition.animateFloat(
      initialValue = 0.5f,
      targetValue = 0f,
      animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1300)),
    )
  Box(
    modifier =
      Modifier
        .size(64.dp * scale)
        .clip(CircleShape)
        .background(DangerRed.copy(alpha = alpha)),
  )
}

// ─────────────────────────────────────────────────────────────
// SectionHeader — bold section title with an optional action.
// ─────────────────────────────────────────────────────────────
@Composable
fun SectionHeader(
  title: String,
  modifier: Modifier = Modifier,
  action: String? = null,
  onAction: (() -> Unit)? = null,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = title, style = MaterialTheme.typography.headlineSmall, color = Ink)
    if (action != null && onAction != null) {
      Text(
        text = action,
        style = MaterialTheme.typography.labelLarge,
        color = ForestDark,
      )
    }
  }
}

// ─────────────────────────────────────────────────────────────
// SunFab — yellow circular FAB (the "I'm Safe" check-in).
// ─────────────────────────────────────────────────────────────
@Composable
fun SunFab(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    onClick = onClick,
    modifier = modifier.size(62.dp),
    shape = CircleShape,
    color = SunYellow,
    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
    shadowElevation = 18.dp,
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = "I'm Safe — check in",
        tint = Ink,
        modifier = Modifier.size(28.dp),
      )
    }
  }
}

// ─────────────────────────────────────────────────────────────
// BackButton — frosted circular back arrow (GlassIconButton + ArrowBack).
// ─────────────────────────────────────────────────────────────
@Composable
fun BackButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  dark: Boolean = false,
) {
  GlassIconButton(
    icon = Icons.Filled.ArrowBack,
    contentDescription = "Back",
    onClick = onClick,
    modifier = modifier,
    dark = dark,
  )
}

// ─────────────────────────────────────────────────────────────
// SosButton — full-width red dispatch button with honest states.
// ─────────────────────────────────────────────────────────────
@Composable
fun SosButton(
  dispatchLabel: String,
  dispatching: Boolean,
  enabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    shape = RoundedCornerShape(16.dp),
    colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White),
    modifier = modifier.fillMaxWidth().height(54.dp),
  ) {
    Text(
      text = if (dispatching) "DISPATCHING…" else dispatchLabel,
      style = MaterialTheme.typography.labelLarge,
    )
  }
}

// ─────────────────────────────────────────────────────────────
// RiskBar — compact gradient risk band (used in zone detail card).
// ─────────────────────────────────────────────────────────────
@Composable
fun RiskBar(score: Int, modifier: Modifier = Modifier) {
  val bandColor =
    when {
      score >= 61 -> DangerRed
      score >= 31 -> CautionAmber
      else -> SafeGreen
    }
  BoxWithConstraints(
    modifier = modifier.height(10.dp).clip(RoundedCornerShape(50)),
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(Brush.horizontalGradient(listOf(SafeGreen, CautionAmber, DangerRed))),
    )
    val markerTravel = maxWidth - 12.dp
    Box(
      modifier =
        Modifier
          .offset(x = markerTravel * (score.coerceIn(0, 100) / 100f))
          .size(12.dp)
          .clip(CircleShape)
          .background(Color.White)
          .border(2.dp, bandColor, CircleShape),
    )
  }
}

// lowercase convenience aliases used by detail screens
@Composable
fun metaItem(emoji: String, text: String, label: String = "", modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    MetaItem(emoji = emoji, text = text)
    if (label.isNotEmpty()) {
      Spacer(modifier = Modifier.height(2.dp))
      Text(text = label, style = MaterialTheme.typography.labelSmall, color = InkSoft)
    }
  }
}

@Composable
fun avatarStack(peers: Int, modifier: Modifier = Modifier) {
  AvatarStack(peers = peers, modifier = modifier)
}
