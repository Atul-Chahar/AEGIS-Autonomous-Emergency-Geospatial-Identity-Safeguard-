package com.example.aegis.mesh

import com.example.aegis.data.local.dao.RelayInboxDao
import com.example.aegis.data.local.entity.RelayInboxEntity
import com.example.aegis.domain.model.RescuePacket

class RelayInbox(
  private val relayInboxDao: RelayInboxDao,
  private val deduplicator: PacketDeduplicator = PacketDeduplicator(),
) {

  /** Stores incoming relayed packet in Room SQLite database after deduplication & validation. */
  suspend fun receiveRelayPacket(packet: RescuePacket): Boolean {
    val (validation, preparedPacket) = deduplicator.validateAndPrepareRelay(packet)
    if (validation != PacketValidationResult.VALID_FOR_RELAY || preparedPacket == null) {
      return false
    }

    val payloadJson = serializeRescuePacket(preparedPacket)

    val entity = RelayInboxEntity(
      packetId = preparedPacket.packetId,
      originTouristId = preparedPacket.touristId,
      payloadJson = payloadJson,
      priority = preparedPacket.priority,
      hopCount = preparedPacket.hopCount,
      ttl = preparedPacket.ttl,
      receivedAtEpochMillis = System.currentTimeMillis(),
      status = "STORED_PENDING_RELAY",
    )

    relayInboxDao.insert(entity)
    return true
  }

  private fun serializeRescuePacket(packet: RescuePacket): String {
    return """
      {
        "packetId": "${packet.packetId}",
        "touristId": "${packet.touristId}",
        "lat": ${packet.latitude ?: "null"},
        "lon": ${packet.longitude ?: "null"},
        "batteryPct": ${packet.batteryPercent ?: "null"},
        "hopCount": ${packet.hopCount},
        "ttl": ${packet.ttl},
        "priority": "${packet.priority}"
      }
    """.trimIndent()
  }
}
