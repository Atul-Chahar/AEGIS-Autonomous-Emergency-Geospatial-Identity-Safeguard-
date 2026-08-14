package com.example.aegis.data.repository.demo

import com.example.aegis.data.repository.BlackBoxRepository
import com.example.aegis.domain.model.Breadcrumb
import com.example.aegis.domain.model.SensorEventChunk
import com.example.aegis.domain.model.Trip
import com.example.aegis.domain.model.TripStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class DemoBlackBoxRepository : BlackBoxRepository {
  private val activeTripState = MutableStateFlow<Trip?>(null)
  private val latestBreadcrumbState = MutableStateFlow<Breadcrumb?>(null)
  private val breadcrumbsList = mutableListOf<Breadcrumb>()
  private val sensorChunksList = mutableListOf<SensorEventChunk>()

  override suspend fun startTrip(touristId: String, plannedRouteId: String?): Trip {
    val trip = Trip(
      tripId = UUID.randomUUID().toString(),
      touristId = touristId,
      startedAt = System.currentTimeMillis(),
      plannedRouteId = plannedRouteId,
      status = TripStatus.ACTIVE,
    )
    activeTripState.value = trip
    return trip
  }

  override suspend fun endTrip(tripId: String): Trip? {
    val current = activeTripState.value ?: return null
    val ended = current.copy(
      endedAt = System.currentTimeMillis(),
      status = TripStatus.COMPLETED,
    )
    activeTripState.value = null
    return ended
  }

  override suspend fun getActiveTrip(): Trip? = activeTripState.value

  override fun observeActiveTrip(): Flow<Trip?> = activeTripState

  override suspend fun recordBreadcrumb(breadcrumb: Breadcrumb) {
    breadcrumbsList.add(breadcrumb)
    latestBreadcrumbState.value = breadcrumb
  }

  override suspend fun recordSensorChunk(chunk: SensorEventChunk) {
    sensorChunksList.add(chunk)
  }

  override fun observeLatestBreadcrumb(): Flow<Breadcrumb?> = latestBreadcrumbState

  override fun observeLatestBreadcrumbForTrip(tripId: String): Flow<Breadcrumb?> =
    latestBreadcrumbState.map { if (it?.tripId == tripId) it else null }

  override suspend fun getBreadcrumbsForTrip(tripId: String): List<Breadcrumb> =
    breadcrumbsList.filter { it.tripId == tripId }

  override suspend fun getUnsyncedBreadcrumbs(): List<Breadcrumb> =
    breadcrumbsList.filter { it.syncState == "PENDING" }

  override suspend fun markBreadcrumbsSynced(breadcrumbIds: List<String>) {
    val ids = breadcrumbIds.toSet()
    for (i in breadcrumbsList.indices) {
      val breadcrumb = breadcrumbsList[i]
      if (breadcrumb.breadcrumbId in ids) {
        breadcrumbsList[i] = breadcrumb.copy(syncState = "SYNCED")
      }
    }
  }
}
