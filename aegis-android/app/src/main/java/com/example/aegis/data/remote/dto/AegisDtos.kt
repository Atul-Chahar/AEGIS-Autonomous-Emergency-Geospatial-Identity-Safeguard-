package com.example.aegis.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTOs mirroring the AEGIS backend (`aegis-backend/src/server.js`) JSON
 * contract. Used by the next-stage remote repositories; keep in sync with the
 * backend payloads.
 */

@Serializable
data class HealthDto(
  val status: String,
  val name: String,
  val activeSockets: Int? = null,
)

@Serializable
data class GeofenceDto(
  val id: String,
  val name: String,
  val riskLevel: String, // SAFE | CAUTION | HIGH_RISK
  val color: String,
  val coordinates: List<List<Double>>,
)

@Serializable
data class IdentityRegisterRequest(
  val name: String,
  val tripStart: String? = null,
  val tripEnd: String,
  val route: List<String> = emptyList(),
  val emergencyContact: String? = null,
)

@Serializable
data class IdentityRegisterResponse(
  val success: Boolean,
  val touristId: String,
  val idHash: String? = null,
  val itineraryHash: String? = null,
  val validDays: Int? = null,
  val qrPayload: String? = null,
)

@Serializable
data class SosRequestDto(
  val packetId: String? = null,
  val touristId: String,
  val idHash: String? = null,
  val lat: Double? = null,
  val lon: Double? = null,
  val batteryPct: Int? = null,
  val channel: String = "HTTPS",
  val rawSmsPayload: String? = null,
)

@Serializable
data class SosResponseDto(
  val success: Boolean,
  val incidentId: String? = null,
  val packetId: String? = null,
  val message: String? = null,
)

@Serializable
data class IncidentDto(
  val id: String,
  val touristId: String,
  val idHash: String? = null,
  val lat: Double? = null,
  val lon: Double? = null,
  val batteryPct: Int? = null,
  val channel: String? = null,
  val timestamp: String? = null,
  val status: String? = null,
  val riskScore: Int? = null,
)

/** Envelope broadcast over the WebSocket by the backend (e.g. `EMERGENCY_SOS`). */
@Serializable
data class WebSocketEventDto(
  val type: String,
  val payload: IncidentDto? = null,
  val timestamp: String? = null,
)
