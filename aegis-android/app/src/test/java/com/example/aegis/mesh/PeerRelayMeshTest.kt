package com.example.aegis.mesh

import com.example.aegis.data.local.dao.RelayInboxDao
import com.example.aegis.data.local.entity.RelayInboxEntity
import com.example.aegis.data.remote.AegisApi
import com.example.aegis.data.remote.dto.GeofenceDto
import com.example.aegis.data.remote.dto.HealthDto
import com.example.aegis.data.remote.dto.IdentityRegisterRequest
import com.example.aegis.data.remote.dto.IdentityRegisterResponse
import com.example.aegis.data.remote.dto.IncidentDto
import com.example.aegis.data.remote.dto.SosRequestDto
import com.example.aegis.data.remote.dto.SosResponseDto
import com.example.aegis.domain.model.RescuePacket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PeerRelayMeshTest {

  private lateinit var deduplicator: PacketDeduplicator
  private lateinit var fakeDao: FakeRelayInboxDao
  private lateinit var relayInbox: RelayInbox
  private lateinit var relayOutbox: RelayOutbox

  @Before
  fun setup() {
    deduplicator = PacketDeduplicator(maxHopCount = 5, defaultTtlMinutes = 120)
    fakeDao = FakeRelayInboxDao()
    relayInbox = RelayInbox(fakeDao, deduplicator)
    relayOutbox = RelayOutbox(fakeDao)
  }

  @Test
  fun `deduplication prevents loop by dropping repeated packet IDs`() {
    val packet = RescuePacket(packetId = "PACKET-101", touristId = "TST-A")

    val (res1, p1) = deduplicator.validateAndPrepareRelay(packet)
    assertEquals(PacketValidationResult.VALID_FOR_RELAY, res1)
    assertNotNull(p1)
    assertEquals(1, p1?.hopCount)

    // Second receipt of same packet ID
    val (res2, p2) = deduplicator.validateAndPrepareRelay(packet)
    assertEquals(PacketValidationResult.DUPLICATE, res2)
    assertTrue(p2 == null)
  }

  @Test
  fun `hop count exceeding max or TTL expiration drops packet`() {
    // 1. Max hop count exceeded
    val maxHopPacket = RescuePacket(packetId = "PACKET-MAX-HOP", touristId = "TST-A", hopCount = 5)
    val (resHop, _) = deduplicator.validateAndPrepareRelay(maxHopPacket)
    assertEquals(PacketValidationResult.TTL_EXCEEDED, resHop)

    // 2. Expired packet
    val oldTime = System.currentTimeMillis() - (180 * 60 * 1000) // 3 hours ago (TTL is 120 mins)
    val expiredPacket = RescuePacket(packetId = "PACKET-EXPIRED", touristId = "TST-A", createdAt = oldTime, ttl = 120)
    val (resExp, _) = deduplicator.validateAndPrepareRelay(expiredPacket)
    assertEquals(PacketValidationResult.EXPIRED, resExp)
  }

  @Test
  fun `Phone B stores packet in RelayInbox even if Phone A turns off`() = runTest {
    val packetFromPhoneA = RescuePacket(
      packetId = "SOS-PHONE-A-001",
      touristId = "TST-PHONE-A",
      latitude = 25.27,
      longitude = 91.69,
      priority = "CRITICAL",
    )

    // Phone B receives packet over Nearby Connection
    val stored = relayInbox.receiveRelayPacket(packetFromPhoneA)
    assertTrue("Phone B must store valid relay packet in Room DB", stored)

    // Simulate Phone A powered off / disconnected: Phone B retains packet in Room SQLite
    val pendingInPhoneB = fakeDao.getPendingRelayPackets()
    assertEquals(1, pendingInPhoneB.size)
    assertEquals("SOS-PHONE-A-001", pendingInPhoneB[0].packetId)
    assertEquals("STORED_PENDING_RELAY", pendingInPhoneB[0].status)
    assertEquals("TST-PHONE-A", pendingInPhoneB[0].originTouristId)
  }

  @Test
  fun `Phone B reconnects to internet and RelayOutbox forwards packet to backend`() = runTest {
    val packet = RescuePacket(
      packetId = "RELAY-TO-BACKEND-001",
      touristId = "TST-PHONE-A",
      priority = "CRITICAL",
    )
    relayInbox.receiveRelayPacket(packet)

    val mockApi = object : AegisApi {
      override suspend fun health(): HealthDto = error("Not needed")
      override suspend fun getGeofences(): List<GeofenceDto> = emptyList()
      override suspend fun registerIdentity(request: IdentityRegisterRequest): IdentityRegisterResponse = error("Not needed")
      override suspend fun submitSos(request: SosRequestDto): SosResponseDto {
        return SosResponseDto(success = true, incidentId = "INC-RELAYED-99", packetId = request.packetId)
      }
      override suspend fun getIncidents(): List<IncidentDto> = emptyList()
    }

    val outbox = RelayOutbox(fakeDao, mockApi)

    val forwardedCount = outbox.flushPendingRelaysToBackend()
    assertEquals(1, forwardedCount)

    val storedPacket = fakeDao.map["RELAY-TO-BACKEND-001"]
    assertEquals("RELAYED_TO_INTERNET", storedPacket?.status)
    assertEquals("INC-RELAYED-99", storedPacket?.serverAckId)
  }

  @Test
  fun `CRITICAL emergency packets take highest priority in RelayInbox query`() = runTest {
    val normalPacket = RescuePacket(packetId = "NORMAL-01", touristId = "TST-1", priority = "NORMAL", createdAt = 1000L)
    val criticalPacket = RescuePacket(packetId = "CRITICAL-01", touristId = "TST-2", priority = "CRITICAL", createdAt = 2000L)

    relayInbox.receiveRelayPacket(normalPacket)
    relayInbox.receiveRelayPacket(criticalPacket)

    val pending = fakeDao.getPendingRelayPackets()
    assertEquals(2, pending.size)
    assertEquals("CRITICAL-01", pending[0].packetId) // CRITICAL queued first
    assertEquals("NORMAL-01", pending[1].packetId)
  }

  private class FakeRelayInboxDao : RelayInboxDao {
    val map = mutableMapOf<String, RelayInboxEntity>()

    override suspend fun insert(packet: RelayInboxEntity): Long {
      map[packet.packetId] = packet
      return map.size.toLong()
    }

    override suspend fun update(packet: RelayInboxEntity) {
      map[packet.packetId] = packet
    }

    override suspend fun getPacketById(packetId: String): RelayInboxEntity? = map[packetId]

    override suspend fun getPendingRelayPackets(): List<RelayInboxEntity> {
      return map.values
        .filter { it.status == "STORED_PENDING_RELAY" }
        .sortedWith(
          compareBy<RelayInboxEntity> { if (it.priority == "CRITICAL") 1 else 2 }
            .thenBy { it.receivedAtEpochMillis }
        )
    }

    override fun observePendingRelayCount(): Flow<Int> = flowOf(map.values.count { it.status == "STORED_PENDING_RELAY" })

    override suspend fun markRelayed(packetId: String, serverAckId: String, relayedAt: Long) {
      val existing = map[packetId]
      if (existing != null) {
        map[packetId] = existing.copy(
          status = "RELAYED_TO_INTERNET",
          serverAckId = serverAckId,
          relayedAtEpochMillis = relayedAt,
        )
      }
    }
  }
}
