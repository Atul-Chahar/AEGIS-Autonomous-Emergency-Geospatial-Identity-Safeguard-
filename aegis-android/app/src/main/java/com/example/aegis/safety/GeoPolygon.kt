package com.example.aegis.safety

import com.example.aegis.domain.model.ZoneStatus

data class GeoPolygon(
  val id: String,
  val name: String,
  val status: ZoneStatus,
  val riskScore: Int,
  val boundary: List<GeoPoint>,
) {
  fun containsPoint(point: GeoPoint): Boolean {
    if (boundary.size < 3) return false
    var inside = false
    var j = boundary.size - 1
    for (i in boundary.indices) {
      val pi = boundary[i]
      val pj = boundary[j]
      if ((pi.longitude > point.longitude) != (pj.longitude > point.longitude) &&
        (point.latitude < (pj.latitude - pi.latitude) * (point.longitude - pi.longitude) / (pj.longitude - pi.longitude) + pi.latitude)
      ) {
        inside = !inside
      }
      j = i
    }
    return inside
  }
}
