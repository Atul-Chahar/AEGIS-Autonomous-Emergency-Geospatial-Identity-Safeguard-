package com.example.aegis.safety

import com.example.aegis.domain.model.ZoneStatus

data class ZoneClassificationResult(
  val matchedPolygonId: String?,
  val matchedPolygonName: String?,
  val status: ZoneStatus,
  val riskScore: Int,
)

class OfflineGeofenceEngine(
  customPolygons: List<GeoPolygon>? = null,
) {
  private val polygons: List<GeoPolygon> = customPolygons ?: defaultMeghalayaGeofences

  fun classifyLocation(latitude: Double, longitude: Double): ZoneClassificationResult {
    val point = GeoPoint(latitude, longitude)

    for (polygon in polygons) {
      if (polygon.containsPoint(point)) {
        return ZoneClassificationResult(
          matchedPolygonId = polygon.id,
          matchedPolygonName = polygon.name,
          status = polygon.status,
          riskScore = polygon.riskScore,
        )
      }
    }

    return ZoneClassificationResult(
      matchedPolygonId = null,
      matchedPolygonName = null,
      status = ZoneStatus.UNKNOWN,
      riskScore = 50,
    )
  }

  companion object {
    val defaultMeghalayaGeofences = listOf(
      // Cherrapunji Ridge (Caution zone, 25.26 to 25.30 N, 91.68 to 91.75 E)
      GeoPolygon(
        id = "cherrapunji",
        name = "Cherrapunji Ridge",
        status = ZoneStatus.CAUTION,
        riskScore = 62,
        boundary = listOf(
          GeoPoint(25.2600, 91.6800),
          GeoPoint(25.3000, 91.6800),
          GeoPoint(25.3000, 91.7500),
          GeoPoint(25.2600, 91.7500),
        )
      ),
      // Nohkalikai Falls High-Risk Cliff (High Risk zone, 25.27 to 25.28 N, 91.65 to 91.67 E)
      GeoPolygon(
        id = "nohkalikai",
        name = "Nohkalikai Falls High-Risk Cliff",
        status = ZoneStatus.HIGH_RISK,
        riskScore = 78,
        boundary = listOf(
          GeoPoint(25.2700, 91.6500),
          GeoPoint(25.2800, 91.6500),
          GeoPoint(25.2800, 91.6700),
          GeoPoint(25.2700, 91.6700),
        )
      ),
      // Living Root Bridges Safe Sanctuary (Safe zone, 25.24 to 25.26 N, 91.66 to 91.69 E)
      GeoPolygon(
        id = "roots",
        name = "Living Root Bridges",
        status = ZoneStatus.SAFE,
        riskScore = 18,
        boundary = listOf(
          GeoPoint(25.2400, 91.6600),
          GeoPoint(25.2600, 91.6600),
          GeoPoint(25.2600, 91.6900),
          GeoPoint(25.2400, 91.6900),
        )
      ),
    )
  }
}
