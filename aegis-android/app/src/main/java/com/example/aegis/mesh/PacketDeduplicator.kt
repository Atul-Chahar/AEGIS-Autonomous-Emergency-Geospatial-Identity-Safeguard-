package com.example.aegis.mesh

import com.example.aegis.domain.model.RescuePacket

enum class PacketValidationResult {
  VALID_FOR_RELAY,
  DUPLICATE,
  EXPIRED,
  TTL_EXCEEDED,
}

class PacketDeduplicator(
  private val maxHopCount: Int = 5,
  private val defaultTtlMinutes: Int = 120, // 2 hours
) {
  private val seenPacketIds = mutableSetOf<String>()

  @Synchronized
  fun validateAndPrepareRelay(packet: RescuePacket): Pair<PacketValidationResult, RescuePacket?> {
    // 1. Loop prevention check: drop if already seen
    if (seenPacketIds.contains(packet.packetId)) {
      return Pair(PacketValidationResult.DUPLICATE, null)
    }

    // 2. Expiry check: drop if expired
    val now = System.currentTimeMillis()
    val ttlMillis = (packet.ttl.coerceAtLeast(1)) * 60 * 1000L
    if (now - packet.createdAt > ttlMillis) {
      return Pair(PacketValidationResult.EXPIRED, null)
    }

    // 3. TTL / Hop count check
    if (packet.hopCount >= maxHopCount) {
      return Pair(PacketValidationResult.TTL_EXCEEDED, null)
    }

    // Mark as seen to prevent loops
    seenPacketIds.add(packet.packetId)

    // Prepare packet with incremented hop count
    val updatedPacket = packet.copy(
      hopCount = packet.hopCount + 1,
      transportUsed = "BLE_MESH",
    )

    return Pair(PacketValidationResult.VALID_FOR_RELAY, updatedPacket)
  }

  @Synchronized
  fun markSeen(packetId: String) {
    seenPacketIds.add(packetId)
  }

  @Synchronized
  fun isSeen(packetId: String): Boolean {
    return seenPacketIds.contains(packetId)
  }

  @Synchronized
  fun clear() {
    seenPacketIds.clear()
  }
}
