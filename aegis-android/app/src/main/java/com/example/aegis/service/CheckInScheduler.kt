package com.example.aegis.service

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Schedules the periodic offline check-in worker (idempotent). */
object CheckInScheduler {
  private const val UNIQUE_WORK_NAME = "aegis-periodic-check-in"

  fun schedule(context: Context) {
    val request =
      PeriodicWorkRequestBuilder<CheckInWorker>(15, TimeUnit.MINUTES)
        .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
      UNIQUE_WORK_NAME,
      ExistingPeriodicWorkPolicy.KEEP,
      request,
    )
  }
}
