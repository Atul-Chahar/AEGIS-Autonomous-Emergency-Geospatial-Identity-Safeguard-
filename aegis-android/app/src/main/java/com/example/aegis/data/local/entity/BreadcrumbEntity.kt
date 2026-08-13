package com.example.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "breadcrumbs",
  indices = [
    Index(value = ["tripId"]),
    Index(value = ["timestamp"]),
  ],
)
data class BreadcrumbEntity(
  @PrimaryKey val breadcrumbId: String,
  val tripId: String,
  val timestamp: Long,
  val latitude: Double,
  val longitude: Double,
  val horizontalAccuracyMeters: Float,
  val altitudeMeters: Double? = null,
  val speedMps: Float,
  val bearingDegrees: Float,
  val batteryPercent: Int,
  val activityMode: String,
  val locationSource: String,
  val isEstimated: Boolean,
  val syncState: String,
)
