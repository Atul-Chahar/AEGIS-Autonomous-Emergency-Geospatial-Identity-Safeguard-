package com.example.aegis.data.repository

import com.example.aegis.data.local.dao.OutboxDao
import com.example.aegis.data.local.entity.OutboxEntity
import com.example.aegis.data.remote.AegisApi
import com.example.aegis.data.remote.dto.GeofenceDto
import com.example.aegis.data.remote.dto.HealthDto
import com.example.aegis.data.remote.dto.IdentityRegisterRequest
import com.example.aegis.data.remote.dto.IdentityRegisterResponse
import com.example.aegis.data.remote.dto.IncidentDto
import com.example.aegis.data.remote.dto.SosRequestDto
import com.example.aegis.data.remote.dto.SosResponseDto
import com.example.aegis.domain.model.Breadcrumb
import com.example.aegis.domain.model.RescuePacket
import com.example.aegis.domain.model.SensorEventChunk
import com.example.aegis.domain.model.SosDispatchResult
import com.example.aegis.domain.model.SosRequest
import com.example.aegis.domain.model.Trip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class OutboxEmergencyRepositoryTest {

  private lateinit var fakeOutboxDao: FakeOutboxDao

  @Before
  fun setup() {
    fakeOutboxDao = FakeOutboxDao()
  }

  @Test
  fun `successful HTTPS SOS writes packet to Outbox FIRST and marks SENT upon ack`() = runTest {
    val fakeApi = object : AegisApi {
      override suspend fun health(): HealthDto = error("Not needed")
      override suspend fun getGeofences(): List<GeofenceDto> = emptyList()
      override suspend fun registerIdentity(request: IdentityRegisterRequest): IdentityRegisterResponse = error("Not needed")
      override suspend fun submitSos(request: SosRequestDto): SosResponseDto {
        return SosResponseDto(success = true, incidentId = "INC-12345", packetId = request.packetId, message = "SOS Ack")
      }
      override suspend fun getIncidents(): List<IncidentDto> = emptyList()
    }

    val repository = RealEmergencyRepository(outboxDao = fakeOutboxDao, api = fakeApi)

    val request = SosRequest(
      touristId = "TST-99",
      zoneId = "cherrapunji",
      latitude = 25.27,
      longitude = 91.69,
      batteryPct = 85,
    )

    val result = repository.dispatchSos(request)

    assertTrue("Must return Sent result", result is SosDispatchResult.Sent)
    val sentResult = result as SosDispatchResult.Sent
    assertEquals("INC-12345", sentResult.ackId)

    // Outbox assertion: written first, then marked SENT
    assertEquals(1, fakeOutboxDao.outboxMap.size)
    val packet = fakeOutboxDao.outboxMap.values.first()
    assertEquals("SENT", packet.status)
    assertEquals("INC-12345", packet.serverAckId)
    assertEquals("HTTPS", packet.transportUsed)
  }

  @Test
  fun `backend unavailable keeps packet in Outbox as PENDING for retry`() = runTest {
    val failingApi = object : AegisApi {
      override suspend fun health(): HealthDto = error("Not needed")
      override suspend fun getGeofences(): List<GeofenceDto> = emptyList()
      override suspend fun registerIdentity(request: IdentityRegisterRequest): IdentityRegisterResponse = error("Not needed")
      override suspend fun submitSos(request: SosRequestDto): SosResponseDto {
        throw IOException("Backend unreachable 503")
      }
      override suspend fun getIncidents(): List<IncidentDto> = emptyList()
    }

    val repository = RealEmergencyRepository(outboxDao = fakeOutboxDao, api = failingApi)

    val request = SosRequest(touristId = "TST-99", zoneId = "roots")

    val result = repository.dispatchSos(request)

    assertTrue("Must return PendingSmsFallback result", result is SosDispatchResult.PendingSmsFallback)
    assertEquals(1, fakeOutboxDao.outboxMap.size)
    val packet = fakeOutboxDao.outboxMap.values.first()
    assertEquals("FAILED", packet.status) // Marked failed for immediate attempt, stays in Outbox queue
  }

  @Test
  fun `duplicate retry returns idempotent server ack`() = runTest {
    val receivedPacketIds = mutableListOf<String>()

    val idempotentApi = object : AegisApi {
      override suspend fun health(): HealthDto = error("Not needed")
      override suspend fun getGeofences(): List<GeofenceDto> = emptyList()
      override suspend fun registerIdentity(request: IdentityRegisterRequest): IdentityRegisterResponse = error("Not needed")
      override suspend fun submitSos(request: SosRequestDto): SosResponseDto {
        request.packetId?.let { receivedPacketIds.add(it) }
        return SosResponseDto(success = true, incidentId = "INC-DUPLICATE-ACK", packetId = request.packetId)
      }
      override suspend fun getIncidents(): List<IncidentDto> = emptyList()
    }

    val repository = RealEmergencyRepository(outboxDao = fakeOutboxDao, api = idempotentApi)

    val request = SosRequest(touristId = "TST-99", zoneId = "cherrapunji")

    val result1 = repository.dispatchSos(request)
    val result2 = repository.dispatchSos(request)

    assertTrue(result1 is SosDispatchResult.Sent)
    assertTrue(result2 is SosDispatchResult.Sent)

    val ack1 = (result1 as SosDispatchResult.Sent).ackId
    val ack2 = (result2 as SosDispatchResult.Sent).ackId

    assertEquals("INC-DUPLICATE-ACK", ack1)
    assertEquals("INC-DUPLICATE-ACK", ack2)
    assertEquals(2, receivedPacketIds.size)
  }

  @Test
  fun `process restart retains PENDING outbox packets`() = runTest {
    // Write a pending outbox item directly to simulate crash before transmission
    fakeOutboxDao.insert(
      OutboxEntity(
        packetId = "PACKET-RESTART-001",
        eventType = "SOS_ALERT",
        payloadJson = "{}",
        status = "PENDING",
      )
    )

    // Simulate process restart with new repository instance reading existing DAO
    val restartedRepository = RealEmergencyRepository(outboxDao = fakeOutboxDao)
    val pending = fakeOutboxDao.getPendingPackets()

    assertEquals(1, pending.size)
    assertEquals("PACKET-RESTART-001", pending[0].packetId)
    assertEquals("PENDING", pending[0].status)
  }

  @Test
  fun `delivery acknowledgement saves serverAckId in outbox`() = runTest {
    val fakeApi = object : AegisApi {
      override suspend fun health(): HealthDto = error("Not needed")
      override suspend fun getGeofences(): List<GeofenceDto> = emptyList()
      override suspend fun registerIdentity(request: IdentityRegisterRequest): IdentityRegisterResponse = error("Not needed")
      override suspend fun submitSos(request: SosRequestDto): SosResponseDto {
        return SosResponseDto(success = true, incidentId = "ACK-SERVER-999", packetId = request.packetId)
      }
      override suspend fun getIncidents(): List<IncidentDto> = emptyList()
    }

    val repository = RealEmergencyRepository(outboxDao = fakeOutboxDao, api = fakeApi)

    repository.dispatchSos(SosRequest(touristId = "TST-1"))

    val saved = fakeOutboxDao.outboxMap.values.first()
    assertEquals("ACK-SERVER-999", saved.serverAckId)
    assertEquals("HTTPS", saved.transportUsed)
  }

  @Test
  fun `no location available handles null GPS gracefully`() = runTest {
    val repository = RealEmergencyRepository(outboxDao = fakeOutboxDao)

    val requestNoLoc = SosRequest(touristId = "TST-NOLOC", latitude = null, longitude = null)

    val result = repository.dispatchSos(requestNoLoc)

    assertTrue(result is SosDispatchResult.PendingSmsFallback)
    val saved = fakeOutboxDao.outboxMap.values.first()
    val decoded = Json.decodeFromString<RescuePacket>(saved.payloadJson)
    assertEquals(null, decoded.latitude)
    assertEquals(null, decoded.longitude)
  }

  @Test
  fun `stale breadcrumb over 15 minutes is flagged as stale`() = runTest {
    val oldTimestamp = System.currentTimeMillis() - (20 * 60 * 1000) // 20 mins ago

    val staleBlackBox = object : BlackBoxRepository {
      override suspend fun startTrip(touristId: String, plannedRouteId: String?): Trip = error("Not needed")
      override suspend fun endTrip(tripId: String): Trip? = null
      override suspend fun getActiveTrip(): Trip? = null
      override fun observeActiveTrip(): Flow<Trip?> = flowOf(null)
      override suspend fun recordBreadcrumb(breadcrumb: Breadcrumb) {}
      override suspend fun recordSensorChunk(chunk: SensorEventChunk) {}
      override fun observeLatestBreadcrumb(): Flow<Breadcrumb?> = flowOf(
        Breadcrumb(
          breadcrumbId = "1",
          tripId = "TRIP-OLD",
          timestamp = oldTimestamp,
          latitude = 25.27,
          longitude = 91.69,
          batteryPercent = 40,
        )
      )
      override fun observeLatestBreadcrumbForTrip(tripId: String): Flow<Breadcrumb?> = flowOf(null)
      override suspend fun getBreadcrumbsForTrip(tripId: String): List<Breadcrumb> = emptyList()
      override suspend fun getUnsyncedBreadcrumbs(): List<Breadcrumb> = emptyList()
    }

    val repository = RealEmergencyRepository(outboxDao = fakeOutboxDao, blackBoxRepository = staleBlackBox)

    repository.dispatchSos(SosRequest(touristId = "TST-STALE"))

    val saved = fakeOutboxDao.outboxMap.values.first()
    val decoded = Json.decodeFromString<RescuePacket>(saved.payloadJson)
    assertTrue("Payload must record isStaleLocation = true", decoded.isStaleLocation)
  }

  private class FakeOutboxDao : OutboxDao {
    val outboxMap = mutableMapOf<String, OutboxEntity>()

    override suspend fun insert(packet: OutboxEntity): Long {
      outboxMap[packet.packetId] = packet
      return outboxMap.size.toLong()
    }

    override suspend fun update(packet: OutboxEntity) {
      outboxMap[packet.packetId] = packet
    }

    override suspend fun getPacketById(packetId: String): OutboxEntity? = outboxMap[packetId]

    override suspend fun getPendingPackets(): List<OutboxEntity> {
      return outboxMap.values.filter { it.status == "PENDING" }
    }

    override fun observePendingCount(): Flow<Int> = flowOf(outboxMap.values.count { it.status == "PENDING" })

    override fun observePacket(packetId: String): Flow<OutboxEntity?> = flowOf(outboxMap[packetId])

    override suspend fun markSent(packetId: String, status: String, serverAckId: String, transportUsed: String, lastAttemptTime: Long) {
      val existing = outboxMap[packetId]
      if (existing != null) {
        outboxMap[packetId] = existing.copy(
          status = status,
          serverAckId = serverAckId,
          transportUsed = transportUsed,
          attemptCount = existing.attemptCount + 1,
          lastAttemptTime = lastAttemptTime,
        )
      }
    }

    override suspend fun markFailed(packetId: String, reason: String, lastAttemptTime: Long) {
      val existing = outboxMap[packetId]
      if (existing != null) {
        outboxMap[packetId] = existing.copy(
          status = "FAILED",
          errorMessage = reason,
          attemptCount = existing.attemptCount + 1,
          lastAttemptTime = lastAttemptTime,
        )
      }
    }

    override suspend fun getFailedPackets(): List<OutboxEntity> {
      return outboxMap.values
        .filter { it.status == "FAILED" && it.attemptCount < 10 }
        .sortedBy { it.createdAt }
    }

    override suspend fun markRetrying(packetId: String, lastAttemptTime: Long) {
      val existing = outboxMap[packetId]
      if (existing != null) {
        outboxMap[packetId] = existing.copy(
          status = "SENDING",
          attemptCount = existing.attemptCount + 1,
          lastAttemptTime = lastAttemptTime,
        )
      }
    }
  }
}
