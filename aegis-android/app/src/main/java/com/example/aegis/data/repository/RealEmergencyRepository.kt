package com.example.aegis.data.repository

import com.example.aegis.data.local.dao.OutboxDao
import com.example.aegis.data.local.entity.OutboxEntity
import com.example.aegis.data.remote.AegisApi
import com.example.aegis.data.remote.SmsFallbackAdapter
import com.example.aegis.data.remote.dto.SosRequestDto
import com.example.aegis.domain.model.RescuePacket
import com.example.aegis.domain.model.SosDispatchResult
import com.example.aegis.domain.model.SosRequest
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class RealEmergencyRepository(
  private val outboxDao: OutboxDao,
  private val api: AegisApi? = null,
  private val blackBoxRepository: BlackBoxRepository? = null,
  private val smsAdapter: SmsFallbackAdapter = SmsFallbackAdapter(),
) : EmergencyRepository {

  override suspend fun dispatchSos(request: SosRequest): SosDispatchResult {
    // 1. Gather real latest breadcrumb telemetry
    val latestBreadcrumb = blackBoxRepository?.observeLatestBreadcrumb()?.firstOrNull()
    val activeTrip = blackBoxRepository?.observeActiveTrip()?.firstOrNull()

    val now = System.currentTimeMillis()
    val isStale = if (latestBreadcrumb != null) {
      (now - latestBreadcrumb.timestamp) > (15 * 60 * 1000) // 15 mins
    } else false

    val effectiveLat = request.latitude ?: latestBreadcrumb?.latitude
    val effectiveLon = request.longitude ?: latestBreadcrumb?.longitude
    val effectiveBat = request.batteryPct ?: latestBreadcrumb?.batteryPercent

    // 2. Build transport-independent RescuePacket
    val packet = RescuePacket(
      packetId = UUID.randomUUID().toString(),
      version = 1,
      eventType = "SOS_ALERT",
      priority = "CRITICAL",
      touristId = request.touristId,
      tripId = activeTrip?.tripId,
      timestamp = now,
      latitude = effectiveLat,
      longitude = effectiveLon,
      locationAccuracy = latestBreadcrumb?.horizontalAccuracyMeters,
      batteryPercent = effectiveBat,
      activityMode = "UNKNOWN",
      incidentConfidence = 1.0f,
      latestBreadcrumbId = latestBreadcrumb?.breadcrumbId,
      createdAt = now,
      isStaleLocation = isStale,
    )

    val payloadJson = serializeRescuePacket(packet)

    // 3. MANDATORY OUTBOX FIRST: Save packet to Room SQLite outbox as PENDING
    val outboxEntity = OutboxEntity(
      packetId = packet.packetId,
      eventType = packet.eventType,
      payloadJson = payloadJson,
      status = "PENDING",
      createdAt = now,
    )
    outboxDao.insert(outboxEntity)

    val smsPayload = smsAdapter.formatSmsPayload(packet)

    // 4. Attempt ONLINE HTTPS delivery if API is available
    if (api != null) {
      try {
        val dto = SosRequestDto(
          packetId = packet.packetId,
          touristId = packet.touristId,
          lat = packet.latitude,
          lon = packet.longitude,
          batteryPct = packet.batteryPercent,
          channel = "HTTPS",
          rawSmsPayload = null,
        )
        val response = api.submitSos(dto)
        if (response.success && response.incidentId != null) {
          // Server Ack received! Update outbox entity
          outboxDao.markSent(
            packetId = packet.packetId,
            status = "SENT",
            serverAckId = response.incidentId,
            transportUsed = "HTTPS",
          )
          return SosDispatchResult.Sent(
            transport = "Internet (HTTPS)",
            ackId = response.incidentId,
            timestamp = now,
          )
        }
      } catch (e: Exception) {
        // HTTPS transmission failed / offline -> Outbox remains PENDING
        outboxDao.markFailed(packet.packetId, e.message ?: "Network unreachable")
      }
    }

    // 5. OFFLINE FALLBACK: Return Pending state with SMS handoff ready payload
    return SosDispatchResult.PendingSmsFallback(
      packetId = packet.packetId,
      smsPayload = smsPayload,
      reason = "Waiting for connectivity — SMS handoff ready",
    )
  }

  private fun serializeRescuePacket(packet: RescuePacket): String {
    return """
      {
        "packetId": "${packet.packetId}",
        "touristId": "${packet.touristId}",
        "lat": ${packet.latitude ?: "null"},
        "lon": ${packet.longitude ?: "null"},
        "batteryPct": ${packet.batteryPercent ?: "null"},
        "isStale": ${packet.isStaleLocation}
      }
    """.trimIndent()
  }
}
