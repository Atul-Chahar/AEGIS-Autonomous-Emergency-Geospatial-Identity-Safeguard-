package com.example.aegis.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.aegis.data.local.AegisDatabase
import com.example.aegis.data.local.security.BlackBoxEncryptor
import com.example.aegis.domain.model.Breadcrumb
import com.example.aegis.domain.model.SensorEventChunk
import com.example.aegis.domain.model.TripStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class BlackBoxRepositoryTest {

  private lateinit var database: AegisDatabase
  private lateinit var repository: BlackBoxRepository
  private lateinit var encryptor: BlackBoxEncryptor

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    database = Room.inMemoryDatabaseBuilder(context, AegisDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    encryptor = BlackBoxEncryptor(isTestMode = true)
    repository = RoomBlackBoxRepository(
      tripDao = database.tripDao(),
      breadcrumbDao = database.breadcrumbDao(),
      sensorEventChunkDao = database.sensorEventChunkDao(),
      encryptor = encryptor,
    )
  }

  @After
  fun teardown() {
    database.close()
  }

  @Test
  fun `starting trip creates active trip in repository`() = runTest {
    val trip = repository.startTrip(touristId = "TST-9901", plannedRouteId = "CHERRA-01")

    assertNotNull(trip.tripId)
    assertEquals("TST-9901", trip.touristId)
    assertEquals("CHERRA-01", trip.plannedRouteId)
    assertEquals(TripStatus.ACTIVE, trip.status)

    val active = repository.getActiveTrip()
    assertEquals(trip.tripId, active?.tripId)
  }

  @Test
  fun `inserting breadcrumb persists and updates latest breadcrumb`() = runTest {
    val trip = repository.startTrip(touristId = "TST-9901")
    val breadcrumb = Breadcrumb(
      breadcrumbId = UUID.randomUUID().toString(),
      tripId = trip.tripId,
      timestamp = 1752000000000L,
      latitude = 25.2742,
      longitude = 91.6964,
      horizontalAccuracyMeters = 5.0f,
      batteryPercent = 88,
      locationSource = "FUSED",
    )

    repository.recordBreadcrumb(breadcrumb)

    val latest = repository.observeLatestBreadcrumb().first()
    assertNotNull(latest)
    assertEquals(breadcrumb.breadcrumbId, latest?.breadcrumbId)
    assertEquals(25.2742, latest?.latitude ?: 0.0, 0.0001)
    assertEquals(91.6964, latest?.longitude ?: 0.0, 0.0001)
    assertEquals(88, latest?.batteryPercent)
  }

  @Test
  fun `reading latest breadcrumb returns most recent breadcrumb`() = runTest {
    val trip = repository.startTrip(touristId = "TST-9901")
    val b1 = Breadcrumb(
      breadcrumbId = "b1",
      tripId = trip.tripId,
      timestamp = 1000L,
      latitude = 25.0,
      longitude = 91.0,
    )
    val b2 = Breadcrumb(
      breadcrumbId = "b2",
      tripId = trip.tripId,
      timestamp = 2000L,
      latitude = 25.1,
      longitude = 91.1,
    )

    repository.recordBreadcrumb(b1)
    repository.recordBreadcrumb(b2)

    val latest = repository.observeLatestBreadcrumb().first()
    assertEquals("b2", latest?.breadcrumbId)
    assertEquals(25.1, latest?.latitude ?: 0.0, 0.0001)
  }

  @Test
  fun `app restart persistence recovers active trip and unsynced breadcrumbs`() = runTest {
    // Phase 1: Start trip & insert breadcrumbs
    val trip = repository.startTrip(touristId = "TST-9901")
    val b1 = Breadcrumb(
      breadcrumbId = "persistent-b1",
      tripId = trip.tripId,
      timestamp = 1500L,
      latitude = 25.2,
      longitude = 91.2,
      syncState = "PENDING",
    )
    repository.recordBreadcrumb(b1)

    // Phase 2: Simulate app restart by instantiating a new repository instance over the same DB
    val newRepository = RoomBlackBoxRepository(
      tripDao = database.tripDao(),
      breadcrumbDao = database.breadcrumbDao(),
      sensorEventChunkDao = database.sensorEventChunkDao(),
      encryptor = encryptor,
    )

    val recoveredTrip = newRepository.getActiveTrip()
    assertNotNull(recoveredTrip)
    assertEquals(trip.tripId, recoveredTrip?.tripId)

    val unsynced = newRepository.getUnsyncedBreadcrumbs()
    assertEquals(1, unsynced.size)
    assertEquals("persistent-b1", unsynced.first().breadcrumbId)
  }

  @Test
  fun `ending trip updates trip status to completed`() = runTest {
    val trip = repository.startTrip(touristId = "TST-9901")
    val endedTrip = repository.endTrip(trip.tripId)

    assertNotNull(endedTrip)
    assertEquals(TripStatus.COMPLETED, endedTrip?.status)
    assertNotNull(endedTrip?.endedAt)

    val activeAfterEnd = repository.getActiveTrip()
    assertNull(activeAfterEnd)
  }

  @Test
  fun `no-location condition returns null latest breadcrumb`() = runTest {
    val latest = repository.observeLatestBreadcrumb().first()
    assertNull(latest)
  }

  @Test
  fun `sensor event chunk payload is encrypted in repository`() = runTest {
    val trip = repository.startTrip(touristId = "TST-9901")
    val chunk = SensorEventChunk(
      chunkId = "chunk-1",
      tripId = trip.tripId,
      eventType = "IMPACT_DETECTED",
      eventTimestamp = System.currentTimeMillis(),
      confidence = 0.95f,
      encryptedPayload = """{"x":10.5,"y":-2.1,"z":24.0}""",
    )

    repository.recordSensorChunk(chunk)

    val storedChunks = database.sensorEventChunkDao().getChunksForTrip(trip.tripId)
    assertEquals(1, storedChunks.size)
    val storedEntity = storedChunks.first()

    // Verify stored payload is encrypted and not raw JSON
    assertEquals(false, storedEntity.encryptedPayload.contains("10.5"))

    // Verify decrypting recovers raw payload
    val decrypted = encryptor.decrypt(storedEntity.encryptedPayload)
    assertEquals("""{"x":10.5,"y":-2.1,"z":24.0}""", decrypted)
  }
}
