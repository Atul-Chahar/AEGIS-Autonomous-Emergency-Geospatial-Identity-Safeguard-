package com.example.aegis.data.remote

import com.example.aegis.data.remote.dto.GeofenceDto
import com.example.aegis.data.remote.dto.HealthDto
import com.example.aegis.data.remote.dto.IdentityRegisterRequest
import com.example.aegis.data.remote.dto.IdentityRegisterResponse
import com.example.aegis.data.remote.dto.IncidentDto
import com.example.aegis.data.remote.dto.SosRequestDto
import com.example.aegis.data.remote.dto.SosResponseDto

/**
 * Contract for the AEGIS backend REST + WebSocket gateway
 * (`aegis-backend/src/server.js`). Declared now so the transport/repository
 * stages have a stable surface; the OkHttp implementation ships in the next
 * stage (intentionally not stubbed with fake data).
 */
interface AegisApi {
  suspend fun health(): HealthDto

  suspend fun getGeofences(): List<GeofenceDto>

  suspend fun registerIdentity(request: IdentityRegisterRequest): IdentityRegisterResponse

  suspend fun submitSos(request: SosRequestDto): SosResponseDto

  suspend fun getIncidents(): List<IncidentDto>
}
