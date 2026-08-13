package com.example.aegis.mesh

import com.example.aegis.data.local.dao.RelayInboxDao
import com.example.aegis.data.remote.AegisApi
import com.example.aegis.data.remote.dto.SosRequestDto

class RelayOutbox(
  private val relayInboxDao: RelayInboxDao,
  private val api: AegisApi? = null,
) {

  /** Forwards stored inbox packets to backend /api/sos when internet connectivity is restored. */
  suspend fun flushPendingRelaysToBackend(): Int {
    if (api == null) return 0
    val pendingList = relayInboxDao.getPendingRelayPackets()
    var successCount = 0

    for (entity in pendingList) {
      try {
        val dto = SosRequestDto(
          packetId = entity.packetId,
          touristId = entity.originTouristId,
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
}
