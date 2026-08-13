package com.example.aegis.safety

data class TrekRoute(
  val routeId: String,
  val name: String,
  val waypoints: List<GeoPoint>,
  val corridorWidthMeters: Double = 50.0,
)
