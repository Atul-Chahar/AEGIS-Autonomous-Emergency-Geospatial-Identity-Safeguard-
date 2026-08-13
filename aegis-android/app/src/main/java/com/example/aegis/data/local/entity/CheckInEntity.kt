package com.example.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A locally-recorded check-in / safety event. */
@Entity(tableName = "check_ins")
data class CheckInEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val touristId: String,
  val latitude: Double?,
  val longitude: Double?,
  val timestampEpochMillis: Long,
)
