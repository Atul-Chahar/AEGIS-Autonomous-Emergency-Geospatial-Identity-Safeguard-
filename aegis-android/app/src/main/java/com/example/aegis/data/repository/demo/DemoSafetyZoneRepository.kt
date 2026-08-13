package com.example.aegis.data.repository.demo

import com.example.aegis.data.repository.SafetyZoneRepository
import com.example.aegis.domain.model.RescuePost
import com.example.aegis.domain.model.SafetyZone
import com.example.aegis.domain.model.ZoneStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * DEMO / PREVIEW data source. Exists so the UI can be developed against the
 * real repository contract before Room sync lands. Production code must depend
 * on [SafetyZoneRepository], never on this class or its data.
 */
class DemoSafetyZoneRepository : SafetyZoneRepository {

  override fun observeZones(): Flow<List<SafetyZone>> = flow { emit(previewZones) }

  override suspend fun getZoneById(zoneId: String): SafetyZone? =
    previewZones.firstOrNull { it.id == zoneId }

  override fun getRescuePost(): RescuePost = previewRescuePost

  private companion object {
    val previewZones =
      listOf(
        SafetyZone(
          id = "cherrapunji",
          name = "Cherrapunji Ridge",
          tagline = "The wettest place on Earth — preview zone.",
          description =
            "Preview zone card. Geofencing, mesh peers and live risk scores are wired to real " +
              "sources in later stages; this screen currently renders demo data.",
          region = "🇮🇳 MEGHALAYA",
          status = ZoneStatus.CAUTION,
          riskScore = 62,
          dates = "12 – 20 AUG",
          duration = "7 days",
          elevation = "1,965 m",
          peers = 2,
        ),
        SafetyZone(
          id = "roots",
          name = "Living Root Bridges",
          tagline = "Centuries-old bridges woven from living ficus roots.",
          description = "Preview zone card. See IMPLEMENTATION_STATUS.md for what is real.",
          region = "🇮🇳 MEGHALAYA",
          status = ZoneStatus.SAFE,
          riskScore = 18,
          dates = "12 – 20 AUG",
          duration = "3 days",
          elevation = "740 m",
          peers = 4,
        ),
        SafetyZone(
          id = "dawki",
          name = "Dawki River",
          tagline = "Crystal-clear waters at the Indo-Bangladesh border.",
          description = "Preview zone card. See IMPLEMENTATION_STATUS.md for what is real.",
          region = "🇮🇳 MEGHALAYA",
          status = ZoneStatus.SAFE,
          riskScore = 24,
          dates = "12 – 20 AUG",
          duration = "1 day",
          elevation = "420 m",
          peers = 1,
        ),
        SafetyZone(
          id = "nohkalikai",
          name = "Nohkalikai Falls",
          tagline = "India's tallest plunge waterfall.",
          description = "Preview zone card. See IMPLEMENTATION_STATUS.md for what is real.",
          region = "🇮🇳 MEGHALAYA",
          status = ZoneStatus.HIGH_RISK,
          riskScore = 78,
          dates = "12 – 20 AUG",
          duration = "2 days",
          elevation = "1,120 m",
          peers = 0,
        ),
      )

    val previewRescuePost =
      RescuePost(
        name = "Mawsmai Rescue Post",
        location = "Sohra, Meghalaya",
        distance = "2.4 km",
        eta = "18 min",
        rating = "4.8",
      )
  }
}
