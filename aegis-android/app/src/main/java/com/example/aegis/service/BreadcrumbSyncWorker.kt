package com.example.aegis.service

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.aegis.AegisApplication
import com.example.aegis.data.remote.dto.BreadcrumbSyncRequest
import com.example.aegis.data.remote.dto.TripSyncRequest
import java.util.concurrent.TimeUnit

/**
 * Periodic sync of the offline BlackBox to the AEGIS gateway.
 *
 * Offline-first: breadcrumbs are recorded in Room first; this worker pushes
 * them (plus the active trip) to the backend whenever connectivity is
 * available, then marks them SYNCED. Room stays the source of truth, so
 * network failure only delays delivery — it never loses data.
 */
class BreadcrumbSyncWorker(
  appContext: Context,
  workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

  companion object {
    private const val UNIQUE_WORK_NAME = "aegis_periodic_breadcrumb_sync"
    private const val SYNC_INTERVAL_MINUTES = 2L

    fun schedule(context: Context) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val request =
        PeriodicWorkRequestBuilder<BreadcrumbSyncWorker>(SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES)
          .setConstraints(constraints)
          .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
          .build()

      WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        UNIQUE_WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        request,
      )
    }

    /**
     * One-time immediate sync, enqueued when a trip starts so the gateway
     * (and authorities on the dashboard) sees the journey promptly instead of
     * waiting for the next periodic window. Deduped per-run by work name.
     */
    fun syncNow(context: Context) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val request =
        OneTimeWorkRequestBuilder<BreadcrumbSyncWorker>()
          .setConstraints(constraints)
          .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
          .build()

      WorkManager.getInstance(context)
        .enqueueUniqueWork(
          "aegis_immediate_breadcrumb_sync",
          ExistingWorkPolicy.REPLACE,
          request,
        )
    }
  }

  override suspend fun doWork(): Result {
    val container = (applicationContext as AegisApplication).container
    val blackBoxRepository = container.blackBoxRepository
    val api = container.aegisApi

    val activeTrip = blackBoxRepository.getActiveTrip() ?: return Result.success()

    return try {
      // 1. Ensure the trip exists on the gateway (idempotent upsert).
      api.startTrip(
        TripSyncRequest(
          tripId = activeTrip.tripId,
          touristId = activeTrip.touristId,
          plannedRouteId = activeTrip.plannedRouteId,
          status = "ACTIVE",
          startedAt = activeTrip.startedAt,
        ),
      )

      // 2. Push all locally pending breadcrumbs for this trip.
      val unsynced = blackBoxRepository.getUnsyncedBreadcrumbs()
      if (unsynced.isEmpty()) return Result.success()

      unsynced.forEach { breadcrumb ->
        api.submitBreadcrumb(
          BreadcrumbSyncRequest(
            breadcrumbId = breadcrumb.breadcrumbId,
            tripId = breadcrumb.tripId,
            touristId = activeTrip.touristId,
            lat = breadcrumb.latitude,
            lon = breadcrumb.longitude,
            accuracyMeters = breadcrumb.horizontalAccuracyMeters,
            batteryPercent = breadcrumb.batteryPercent,
            activityMode = breadcrumb.activityMode,
            timestamp = breadcrumb.timestamp,
          ),
        )
      }

      // 3. Mark them synced only after the gateway acknowledged.
      blackBoxRepository.markBreadcrumbsSynced(unsynced.map { it.breadcrumbId })
      Result.success()
    } catch (e: Exception) {
      // Network/transport failure — keep everything PENDING in Room and retry later.
      Result.retry()
    }
  }
}

/** Schedules the periodic BlackBox sync worker (idempotent). */
object BreadcrumbSyncScheduler {
  fun schedule(context: Context) {
    BreadcrumbSyncWorker.schedule(context)
  }
}
