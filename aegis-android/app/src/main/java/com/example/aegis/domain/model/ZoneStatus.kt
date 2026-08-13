package com.example.aegis.domain.model

/** Safety level of a geofenced zone. Colors follow the DESIGN.md status palette. */
enum class ZoneStatus(val emoji: String, val label: String) {
  SAFE("🟢", "Safe"),
  CAUTION("🟡", "Caution"),
  HIGH_RISK("🔴", "High Risk"),
  UNKNOWN("⚪", "Unknown"),
}
