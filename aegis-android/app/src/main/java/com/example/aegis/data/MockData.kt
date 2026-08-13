package com.example.aegis.data

import androidx.annotation.DrawableRes
import com.example.aegis.R

/** Safety level of a geofenced zone — mirrors the plan's risk bands. */
enum class ZoneStatus(val emoji: String, val label: String, val description: String) {
  SAFE("🟢", "Safe", "0 – 30 · Normal"),
  CAUTION("🟡", "Caution", "31 – 60 · Interactive prompt"),
  HIGH_RISK("🔴", "High Risk", "61+ · Control room advised"),
}

data class SafetyZone(
  val id: String,
  val name: String,
  val tagline: String,
  val description: String,
  val region: String,
  @DrawableRes val imageRes: Int,
  val status: ZoneStatus,
  val riskScore: Int, // 0..100
  val dates: String,
  val duration: String,
  val elevation: String,
  val peers: Int,
)

data class RescuePost(
  val name: String,
  val location: String,
  val distance: String,
  val eta: String,
  val rating: String,
  @DrawableRes val imageRes: Int,
)

object MockData {
  const val TOURIST_NAME = "Aryan"
  const val TOURIST_ID = "TST-8F29X4"
  const val TOURIST_HASH = "0xa7f8e32904b1c5a92d8312c6e4f9b08a3d17"
  const val CONTRACT_ADDRESS = "0x3B4c…A9f2"
  const val NETWORK = "Ethereum Sepolia"
  const val VALIDITY = "12 – 20 Aug 2026"
  const val VALIDITY_SHORT = "12 – 20 AUG"
  const val GNSS_FIX = "25.141° N, 91.261° E"
  const val MESH_PEERS = 2

  val activeZoneId: String = "cherrapunji"

  val zones: List<SafetyZone> =
    listOf(
      SafetyZone(
        id = "cherrapunji",
        name = "Cherrapunji Ridge",
        tagline = "The wettest place on Earth — guarded.",
        description =
          "Rain-lashed cliffs, living root bridges and thundering falls. AEGIS geofencing is live here with " +
            "dual-channel SOS and mesh peers within 15 m.",
        region = "🇮🇳 MEGHALAYA",
        imageRes = R.drawable.zone_cherrapunji,
        status = ZoneStatus.CAUTION,
        riskScore = 62,
        dates = VALIDITY_SHORT,
        duration = "7 days",
        elevation = "1,965 m",
        peers = 2,
      ),
      SafetyZone(
        id = "roots",
        name = "Living Root Bridges",
        tagline = "Centuries-old bridges woven from living ficus roots.",
        description =
          "A gentle jungle walk between Nongriat and Nongthymmai. Low risk, high reward — keep the mesh relay on.",
        region = "🇮🇳 MEGHALAYA",
        imageRes = R.drawable.zone_roots,
        status = ZoneStatus.SAFE,
        riskScore = 18,
        dates = VALIDITY_SHORT,
        duration = "3 days",
        elevation = "740 m",
        peers = 4,
      ),
      SafetyZone(
        id = "dawki",
        name = "Dawki River",
        tagline = "Crystal-clear waters at the Indo-Bangladesh border.",
        description =
          "Boats over a glassy river. Border-adjacent — keep the Tourist ID voucher ready for checkpoint scans.",
        region = "🇮🇳 MEGHALAYA",
        imageRes = R.drawable.zone_river,
        status = ZoneStatus.SAFE,
        riskScore = 24,
        dates = VALIDITY_SHORT,
        duration = "1 day",
        elevation = "420 m",
        peers = 1,
      ),
      SafetyZone(
        id = "nohkalikai",
        name = "Nohkalikai Falls",
        tagline = "India's tallest plunge waterfall.",
        description =
          "Slippery cliff paths after rainfall. High-risk band active — do not hike alone; check in every 30 min.",
        region = "🇮🇳 MEGHALAYA",
        imageRes = R.drawable.zone_valley,
        status = ZoneStatus.HIGH_RISK,
        riskScore = 78,
        dates = VALIDITY_SHORT,
        duration = "2 days",
        elevation = "1,120 m",
        peers = 0,
      ),
    )

  fun zoneById(id: String): SafetyZone = zones.firstOrNull { it.id == id } ?: zones.first()

  val rescuePost =
    RescuePost(
      name = "Mawsmai Rescue Post",
      location = "Sohra, Meghalaya",
      distance = "2.4 km",
      eta = "18 min",
      rating = "4.8",
      imageRes = R.drawable.rescue_post,
    )
}
