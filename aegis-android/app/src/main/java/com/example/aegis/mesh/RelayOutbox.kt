package com.example.aegis.mesh

import com.example.aegis.data.local.dao.RelayInboxDao
import com.example.aegis.data.remote.AegisApi
import com.example.aegis.data.remote.dto.SosRequestDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class RelayOutbox(
  private val relayInboxDao: RelayInboxDao,
  private val api: AegisApi? = null,
) {

  /**
   * Forwards stored inbox packets to backend /api/sos when internet
   * connectivity is restored. The tourist's real coordinates are read from
   * the packet JSON stored on the origin device — the gateway must never
   * receive placeholder coordinates for a relayed emergency.
   */
  suspend fun flushPendingRelaysToBackend(): Int {
    if (api == null) return 0
    val pendingList = relayInboxDao.getPendingRelayPackets()
    var successCount = 0

    for (entity in pendingList) {
      try {
        val geo = parsePacketGeo(entity.payloadJson)
        val dto = SosRequestDto(
          packetId = entity.packetId,
          touristId = entity.originTouristId,
          lat = geo?.first,
          lon = geo?.second,
          batteryPct = geo?.third,
          channel = "BLE_MESH_RELAY",
          rawSmsPayload = null,
        )

        val response = api.submitSos(dto)
        if (response.success && response.incidentId != null) {
          relayInboxDao.markRelayed(
            packetId = entity.packetId,
            serverAckId = response.incidentId,
          )
          successCount++
        }
      } catch (e: Exception) {
        // Continue trying next stored packets
      }
    }

    return successCount
  }

  /** Extracts (lat, lon, batteryPct) from the stored relay packet JSON if present. */
  private fun parsePacketGeo(payloadJson: String): Triple<Double, Double, Int>? =
    try {
      val obj: JsonObject = Json { ignoreUnknownKeys = true }.parseToJsonElement(payloadJson).jsonObject
      val lat = obj["lat"]?.jsonPrimitive?.doubleOrNull ?: obj["latitude"]?.jsonPrimitive?.doubleOrNull ?: return null
      val lon = obj["lon"]?.jsonPrimitive?.doubleOrNull ?: obj["longitude"]?.jsonPrimitive?.doubleOrNull ?: return null
      val battery =
        obj["batteryPct"]?.jsonPrimitive?.intOrNull
          ?: obj["batteryPercent"]?.jsonPrimitive?.intOrNull
          ?: 0
      Triple(lat, lon, battery)
    } catch (e: Exception) {
      null
    }
}
