package com.example.aegis.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aegis.AegisApplication
import com.example.aegis.domain.model.TouristIdentity
import com.example.aegis.location.LocationResult
import kotlinx.coroutines.flow.first

/**
 * Periodic background check-in: records a local event in Room (offline-first).
 * Location is attached only when the user has granted the permission and a fix
 * is available — the worker never fabricates coordinates.
 */
class CheckInWorker(
  appContext: Context,
  params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

  override suspend fun doWork(): Result {
    val container = (applicationContext as AegisApplication).container
    val identity: TouristIdentity = container.identityRepository.observeIdentity().first()

    val location = container.locationProvider.currentLocation()
    val (latitude, longitude) =
      when (location) {
        is LocationResult.Success -> location.latitude to location.longitude
        is LocationResult.Unavailable -> null to null
      }

    container.checkInRepository.recordCheckIn(latitude, longitude)
    return Result.success()
  }
}
