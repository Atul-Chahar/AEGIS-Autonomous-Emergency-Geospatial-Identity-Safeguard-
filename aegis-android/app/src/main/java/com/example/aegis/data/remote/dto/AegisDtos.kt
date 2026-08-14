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
  val name: String? = null,
  val activeSockets: Int? = null,
)

@Serializable
data class GeofenceDto(
  val id: String,
  val name: String,
  val riskLevel: String, // SAFE | CAUTION | HIGH_RISK
  val color: String,
  val coordinates: List<List<Double>>? = null,
  val coordinatesJson: String? = null,
)

/**
 * Backend contract (`POST /api/identity/register`) requires the pseudonymous
 * tourist id plus a device-generated salt. The salt never leaves the device
 * in raw form beyond this one commitment call.
 */
@Serializable
data class IdentityRegisterRequest(
  val touristId: String,
  val salt: String,
  val validDays: Int? = null,
)

/** Trip ingest contract for `POST /api/trips` (Android BlackBox sync). */
@Serializable
data class TripSyncRequest(
  val tripId: String,
  val touristId: String,
  val plannedRouteId: String? = null,
  val status: String = "ACTIVE",
  val startedAt: Long,
)

@Serializable
data class TripSyncDto(
  val id: String? = null,
  val tripId: String? = null,
  val touristId: String? = null,
  val status: String? = null,
  val plannedRouteId: String? = null,
)

/** Breadcrumb ingest contract for `POST /api/breadcrumbs` (Android BlackBox sync). */
@Serializable
data class BreadcrumbSyncRequest(
  val breadcrumbId: String,
  val tripId: String,
  val touristId: String,
  val lat: Double,
  val lon: Double,
  val accuracyMeters: Float? = null,
  val batteryPercent: Int? = null,
  val activityMode: String? = null,
  val timestamp: Long,
)

@Serializable
data class BreadcrumbSyncDto(
  val breadcrumbId: String? = null,
  val tripId: String? = null,
  val lat: Double? = null,
  val lon: Double? = null,
  val timestamp: String? = null,
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
