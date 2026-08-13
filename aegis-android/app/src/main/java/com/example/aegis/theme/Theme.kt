package com.example.aegis.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * AEGIS uses a fixed brand palette ("liquid sage") instead of
 * dynamic wallpaper color — the sage/lime gradient and frosted
 * glass are the product's identity (per the design mockup).
 * Dark mode gets a deep-forest variant.
 */
private val AegisLightColors =
  lightColorScheme(
    primary = ForestDark,
    onPrimary = Color.White,
    primaryContainer = SageSoft,
    onPrimaryContainer = Ink,
    secondary = Sage600,
    onSecondary = Color.White,
    secondaryContainer = SagePale,
    onSecondaryContainer = Ink,
    tertiary = SunYellow,
    onTertiary = Ink,
    tertiaryContainer = SunYellowSoft,
    onTertiaryContainer = Ink,
    background = SagePale,
    onBackground = Ink,
    surface = Color(0xFFFFFDF8),
    onSurface = Ink,
    surfaceVariant = SageLight,
    onSurfaceVariant = InkSoft,
    error = DangerRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD5),
    onErrorContainer = Color(0xFF410001),
    outline = Sage600.copy(alpha = 0.4f),
    outlineVariant = SageLight,
  )

private val AegisDarkColors =
  darkColorScheme(
    primary = SageSoft,
    onPrimary = ForestDeep,
    primaryContainer = ForestDark,
    onPrimaryContainer = SagePale,
    secondary = SageMid,
    onSecondary = ForestDeep,
    secondaryContainer = ForestDark,
    onSecondaryContainer = SageSoft,
    tertiary = SunYellow,
    onTertiary = ForestDeep,
    tertiaryContainer = Color(0xFF4A3F00),
    onTertiaryContainer = SunYellowSoft,
    background = ForestDeep,
    onBackground = SagePale,
    surface = ForestDark,
    onSurface = SagePale,
    surfaceVariant = Color(0xFF22382D),
    onSurfaceVariant = Color(0xFFB7C8B0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = SageSoft.copy(alpha = 0.35f),
    outlineVariant = Color(0xFF22382D),
  )

@Composable
fun AEGISTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = if (darkTheme) AegisDarkColors else AegisLightColors,
    typography = AegisTypography,
    content = content,
  )
}
