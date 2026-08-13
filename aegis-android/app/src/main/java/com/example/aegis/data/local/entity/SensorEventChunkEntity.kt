package com.example.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "sensor_event_chunks",
  indices = [Index(value = ["tripId"])],
)
data class SensorEventChunkEntity(
  @PrimaryKey val chunkId: String,
  val tripId: String,
  val eventType: String,
  val eventTimestamp: Long,
  val activityMode: String,
  val confidence: Float,
  val encryptedPayload: String,
  val createdAt: Long,
)
