package com.example.aegis.safety

import com.example.aegis.location.LocationResult
import com.example.aegis.location.LocationSanityChecker
import kotlin.math.abs

enum class DeviationSeverity {
  ON_ROUTE,
  NEAR_CORRIDOR,
  MINOR_DEVIATION,
  CRITICAL_DEVIATION,
}

data class RouteDeviationResult(
  val distanceToRouteMeters: Double,
  val effectiveDistanceMeters: Double, // distance - accuracy margin
  val isDeviated: Boolean,
  val severity: DeviationSeverity,
)

class RouteDeviationEngine {

  fun evaluateDeviation(
    fix: LocationResult.Success,
    route: TrekRoute,
  ): RouteDeviationResult {
    if (route.waypoints.size < 2) {
      return RouteDeviationResult(
        distanceToRouteMeters = 0.0,
        effectiveDistanceMeters = 0.0,
        isDeviated = false,
        severity = DeviationSeverity.ON_ROUTE,
      )
    }

    val point = GeoPoint(fix.latitude, fix.longitude)
    var minDistanceMeters = Double.MAX_VALUE

    for (i in 0 until route.waypoints.size - 1) {
      val p1 = route.waypoints[i]
      val p2 = route.waypoints[i + 1]
      val dist = distanceToSegmentMeters(point, p1, p2)
      if (dist < minDistanceMeters) {
        minDistanceMeters = dist
      }
    }

    // Account for GPS accuracy margin
    val effectiveDistance = (minDistanceMeters - fix.accuracyMeters).coerceAtLeast(0.0)
    val isDeviated = effectiveDistance > route.corridorWidthMeters

    val severity = when {
      effectiveDistance <= route.corridorWidthMeters -> DeviationSeverity.ON_ROUTE
      effectiveDistance <= route.corridorWidthMeters * 1.5 -> DeviationSeverity.NEAR_CORRIDOR
      effectiveDistance <= route.corridorWidthMeters * 3.0 -> DeviationSeverity.MINOR_DEVIATION
      else -> DeviationSeverity.CRITICAL_DEVIATION
    }

    return RouteDeviationResult(
      distanceToRouteMeters = minDistanceMeters,
      effectiveDistanceMeters = effectiveDistance,
      isDeviated = isDeviated,
      severity = severity,
    )
  }

  private fun distanceToSegmentMeters(p: GeoPoint, a: GeoPoint, b: GeoPoint): Double {
    val dAB = LocationSanityChecker.haversineDistanceMeters(a.latitude, a.longitude, b.latitude, b.longitude)
    if (dAB == 0.0) return LocationSanityChecker.haversineDistanceMeters(p.latitude, p.longitude, a.latitude, a.longitude)

    val dAP = LocationSanityChecker.haversineDistanceMeters(a.latitude, a.longitude, p.latitude, p.longitude)
    val dBP = LocationSanityChecker.haversineDistanceMeters(b.latitude, b.longitude, p.latitude, p.longitude)

    // Check projection factor t using vector dot product in equirectangular projection
    val cosLat = Math.cos(Math.toRadians((a.latitude + b.latitude) / 2.0))
    val dx = (b.longitude - a.longitude) * cosLat
    val dy = (b.latitude - a.latitude)
    val px = (p.longitude - a.longitude) * cosLat
    val py = (p.latitude - a.latitude)

    val t = ((px * dx + py * dy) / (dx * dx + dy * dy)).coerceIn(0.0, 1.0)
    val projLat = a.latitude + t * (b.latitude - a.latitude)
    val projLon = a.longitude + t * (b.longitude - a.longitude)

    return LocationSanityChecker.haversineDistanceMeters(p.latitude, p.longitude, projLat, projLon)
  }
}
