package com.example.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
  @PrimaryKey val tripId: String,
  val touristId: String,
  val startedAt: Long,
  val endedAt: Long? = null,
  val plannedRouteId: String? = null,
  val status: String = "ACTIVE",
)
