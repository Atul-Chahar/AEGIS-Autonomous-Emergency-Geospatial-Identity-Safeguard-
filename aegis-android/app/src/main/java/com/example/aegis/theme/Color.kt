package com.example.aegis.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
// AEGIS brand palette — "liquid sage"
// Inspired by the travel-app mockup: pale lime → deep sage
// gradient, frosted white glass, forest-ink typography and a
// warm sun-yellow accent.
// ─────────────────────────────────────────────────────────────

// Background gradient (light): pale lime at top → soft sage at bottom
val SagePale = Color(0xFFF3F8E6)
val SageLight = Color(0xFFE7F0D0)
val SageSoft = Color(0xFFD5E6B6)
val SageMid = Color(0xFFBCD693)
val SageDeep = Color(0xFF92B868)

// Neutral sages
val Sage500 = Color(0xFF7FA05B)
val Sage600 = Color(0xFF64854A)
val Sage700 = Color(0xFF4C6A39)

// Ink & forest darks
val Ink = Color(0xFF1A2419)
val InkSoft = Color(0xFF5B6C5B)
val ForestDark = Color(0xFF17382B) // deep forest green — dark cards, selected pills
val ForestDeep = Color(0xFF0D241B) // near-black green — floating nav pill, scrims

// Accents
val SunYellow = Color(0xFFF7C81B)
val SunYellowSoft = Color(0xFFFFF6D6)
val LimeGlow = Color(0xFFC6F24E)

// Status colors
val SafeGreen = Color(0xFF2FBF71)
val CautionAmber = Color(0xFFF5A623)
val DangerRed = Color(0xFFF04438)
val MeshCyan = Color(0xFF17B7D6)

// ── Liquid glass tokens ──────────────────────────────────────
val GlassSurface = Color(0xB3FFFDF8) // frosted white, ~70%
val GlassSurfaceStrong = Color(0xD9FFFDF8) // frosted white, ~85%
val GlassBorder = Color(0x59FFFFFF) // white, 35%
val GlassOnImage = Color(0x8C0E241B) // dark glass pill over photos, ~55%
val GlassOnImageBorder = Color(0x2EFFFFFF) // white, 18%
val GlassSoftShadow = Color(0x3364854A) // sage-tinted soft shadow
val GlassScrim = Color(0xB30D241B) // bottom scrim for image cards
