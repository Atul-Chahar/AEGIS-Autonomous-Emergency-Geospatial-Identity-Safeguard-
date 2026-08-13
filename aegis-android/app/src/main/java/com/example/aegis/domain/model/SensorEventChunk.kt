package com.example.aegis.domain.model

data class SensorEventChunk(
  val chunkId: String,
  val tripId: String,
  val eventType: String,
  val eventTimestamp: Long,
  val activityMode: String = "UNKNOWN",
  val confidence: Float = 1.0f,
  val encryptedPayload: String,
  val createdAt: Long = System.currentTimeMillis(),
)
