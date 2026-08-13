package com.example.aegis.data.repository

import com.example.aegis.data.local.dao.BreadcrumbDao
import com.example.aegis.data.local.dao.SensorEventChunkDao
import com.example.aegis.data.local.dao.TripDao
import com.example.aegis.data.local.entity.BreadcrumbEntity
import com.example.aegis.data.local.entity.SensorEventChunkEntity
import com.example.aegis.data.local.entity.TripEntity
import com.example.aegis.data.local.security.BlackBoxEncryptor
import com.example.aegis.domain.model.Breadcrumb
import com.example.aegis.domain.model.SensorEventChunk
import com.example.aegis.domain.model.Trip
import com.example.aegis.domain.model.TripStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomBlackBoxRepository(
  private val tripDao: TripDao,
  private val breadcrumbDao: BreadcrumbDao,
  private val sensorEventChunkDao: SensorEventChunkDao,
  private val encryptor: BlackBoxEncryptor = BlackBoxEncryptor(),
) : BlackBoxRepository {

  override suspend fun startTrip(touristId: String, plannedRouteId: String?): Trip {
    val existingActive = tripDao.getActiveTrip()
    if (existingActive != null) {
      return existingActive.toDomain()
    }

    val tripId = UUID.randomUUID().toString()
    val newTripEntity = TripEntity(
      tripId = tripId,
      touristId = touristId,
      startedAt = System.currentTimeMillis(),
      endedAt = null,
      plannedRouteId = plannedRouteId,
      status = TripStatus.ACTIVE.name,
    )
    tripDao.insertTrip(newTripEntity)
    return newTripEntity.toDomain()
  }

  override suspend fun endTrip(tripId: String): Trip? {
    val existing = tripDao.getTripById(tripId) ?: tripDao.getActiveTrip() ?: return null
    val updatedEntity = existing.copy(
      endedAt = System.currentTimeMillis(),
      status = TripStatus.COMPLETED.name,
    )
    tripDao.updateTrip(updatedEntity)
    return updatedEntity.toDomain()
  }

  override suspend fun getActiveTrip(): Trip? {
    return tripDao.getActiveTrip()?.toDomain()
  }

  override fun observeActiveTrip(): Flow<Trip?> {
    return tripDao.observeActiveTrip().map { it?.toDomain() }
  }

  override suspend fun recordBreadcrumb(breadcrumb: Breadcrumb) {
    breadcrumbDao.insertBreadcrumb(breadcrumb.toEntity())
  }

  override suspend fun recordSensorChunk(chunk: SensorEventChunk) {
    val encrypted = encryptor.encrypt(chunk.encryptedPayload)
    val entity = chunk.toEntity(encryptedPayload = encrypted)
    sensorEventChunkDao.insertChunk(entity)
  }

  override fun observeLatestBreadcrumb(): Flow<Breadcrumb?> {
    return breadcrumbDao.observeLatestBreadcrumbAny().map { it?.toDomain() }
  }

  override fun observeLatestBreadcrumbForTrip(tripId: String): Flow<Breadcrumb?> {
    return breadcrumbDao.observeLatestBreadcrumb(tripId).map { it?.toDomain() }
  }

  override suspend fun getBreadcrumbsForTrip(tripId: String): List<Breadcrumb> {
    return breadcrumbDao.getBreadcrumbsForTrip(tripId).map { it.toDomain() }
  }

  override suspend fun getUnsyncedBreadcrumbs(): List<Breadcrumb> {
    return breadcrumbDao.getUnsyncedBreadcrumbs().map { it.toDomain() }
  }

  private fun TripEntity.toDomain() = Trip(
    tripId = tripId,
    touristId = touristId,
    startedAt = startedAt,
    endedAt = endedAt,
    plannedRouteId = plannedRouteId,
    status = try {
      TripStatus.valueOf(status)
    } catch (e: Exception) {
      TripStatus.ACTIVE
    },
  )

  private fun Breadcrumb.toEntity() = BreadcrumbEntity(
    breadcrumbId = breadcrumbId,
    tripId = tripId,
    timestamp = timestamp,
    latitude = latitude,
    longitude = longitude,
    horizontalAccuracyMeters = horizontalAccuracyMeters,
    altitudeMeters = altitudeMeters,
    speedMps = speedMps,
    bearingDegrees = bearingDegrees,
    batteryPercent = batteryPercent,
    activityMode = activityMode,
    locationSource = locationSource,
    isEstimated = isEstimated,
    syncState = syncState,
  )

  private fun BreadcrumbEntity.toDomain() = Breadcrumb(
    breadcrumbId = breadcrumbId,
    tripId = tripId,
    timestamp = timestamp,
    latitude = latitude,
    longitude = longitude,
    horizontalAccuracyMeters = horizontalAccuracyMeters,
    altitudeMeters = altitudeMeters,
    speedMps = speedMps,
    bearingDegrees = bearingDegrees,
    batteryPercent = batteryPercent,
    activityMode = activityMode,
    locationSource = locationSource,
    isEstimated = isEstimated,
    syncState = syncState,
  )

  private fun SensorEventChunk.toEntity(encryptedPayload: String) = SensorEventChunkEntity(
    chunkId = chunkId,
    tripId = tripId,
    eventType = eventType,
    eventTimestamp = eventTimestamp,
    activityMode = activityMode,
    confidence = confidence,
    encryptedPayload = encryptedPayload,
    createdAt = createdAt,
  )
}
