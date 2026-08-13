package com.example.aegis.domain.model

/**
 * A geofenced safety zone. Pure domain data — no Android types.
 * Imagery is resolved in the UI layer via [com.example.aegis.ui.ZoneArtwork].
 */
data class SafetyZone(
  val id: String,
  val name: String,
  val tagline: String,
  val description: String,
  val region: String,
  val status: ZoneStatus,
  val riskScore: Int, // 0..100
  val dates: String,
  val duration: String,
  val elevation: String,
  val peers: Int,
)
