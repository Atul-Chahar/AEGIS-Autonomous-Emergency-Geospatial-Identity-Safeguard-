package com.example.aegis.data.repository

import com.example.aegis.domain.model.Breadcrumb
import com.example.aegis.domain.model.SensorEventChunk
import com.example.aegis.domain.model.Trip
import kotlinx.coroutines.flow.Flow

interface BlackBoxRepository {
  suspend fun startTrip(touristId: String, plannedRouteId: String? = null): Trip
  suspend fun endTrip(tripId: String): Trip?
  suspend fun getActiveTrip(): Trip?
  fun observeActiveTrip(): Flow<Trip?>
  suspend fun recordBreadcrumb(breadcrumb: Breadcrumb)
  suspend fun recordSensorChunk(chunk: SensorEventChunk)
  fun observeLatestBreadcrumb(): Flow<Breadcrumb?>
  fun observeLatestBreadcrumbForTrip(tripId: String): Flow<Breadcrumb?>
  suspend fun getBreadcrumbsForTrip(tripId: String): List<Breadcrumb>
  suspend fun getUnsyncedBreadcrumbs(): List<Breadcrumb>
}
