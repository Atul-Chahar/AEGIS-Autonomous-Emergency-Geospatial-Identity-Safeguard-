package com.example.aegis.domain.model

enum class TripStatus {
  ACTIVE,
  COMPLETED,
  CANCELLED,
}

data class Trip(
  val tripId: String,
  val touristId: String,
  val startedAt: Long,
  val endedAt: Long? = null,
  val plannedRouteId: String? = null,
  val status: TripStatus = TripStatus.ACTIVE,
)
