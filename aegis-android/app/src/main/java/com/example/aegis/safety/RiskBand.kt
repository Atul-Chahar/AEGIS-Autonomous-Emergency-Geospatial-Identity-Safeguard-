package com.example.aegis.safety

/** Risk bands per the AEGIS plan — must stay consistent across Android, Web and backend. */
enum class RiskBand(val label: String, val description: String) {
  SAFE("Safe", "0 – 30 · Normal"),
  CAUTION("Caution", "31 – 60 · Interactive prompt"),
  HIGH_RISK("High Risk", "61+ · Control room advised");

  companion object {
    fun from(score: Int): RiskBand =
      when {
        score <= 30 -> SAFE
        score <= 60 -> CAUTION
        else -> HIGH_RISK
      }
  }
}
