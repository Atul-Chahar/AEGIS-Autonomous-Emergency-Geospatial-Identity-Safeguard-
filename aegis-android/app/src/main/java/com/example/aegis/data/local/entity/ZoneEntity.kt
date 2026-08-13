package com.example.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Local spatial tile of a safety zone (offline-first geofencing source). */
@Entity(tableName = "zones")
data class ZoneEntity(
  @PrimaryKey val id: String,
  val name: String,
  val tagline: String,
  val description: String,
  val region: String,
  val status: String,
  val riskScore: Int,
  val dates: String,
  val duration: String,
  val elevation: String,
  val peers: Int,
)
