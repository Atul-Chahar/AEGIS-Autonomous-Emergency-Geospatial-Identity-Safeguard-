package com.example.aegis.domain.model

import java.util.UUID

/**
 * Transport-independent emergency packet.
 * Can be serialized over HTTPS, WebSockets, compact SMS, or BLE Mesh.
 */
data class RescuePacket(
  val packetId: String = UUID.randomUUID().toString(),
  val version: Int = 1,
  val eventType: String = "SOS_ALERT",
  val priority: String = "CRITICAL",
  val touristId: String,
  val tripId: String? = null,
  val timestamp: Long = System.currentTimeMillis(),
  val latitude: Double? = null,
  val longitude: Double? = null,
  val locationAccuracy: Float? = null,
  val batteryPercent: Int? = null,
  val activityMode: String = "UNKNOWN",
  val incidentConfidence: Float? = 1.0f,
  val latestBreadcrumbId: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val hopCount: Int = 0,
  val ttl: Int = 5,
  val signature: String? = null,
  val transportUsed: String? = null,
  val isStaleLocation: Boolean = false,
)
