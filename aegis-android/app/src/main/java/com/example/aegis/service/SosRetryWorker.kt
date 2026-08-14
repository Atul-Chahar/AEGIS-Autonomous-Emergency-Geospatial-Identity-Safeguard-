package com.example.aegis.service

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.aegis.AegisApplication
import com.example.aegis.data.local.entity.OutboxEntity
import com.example.aegis.data.remote.dto.SosRequestDto
import com.example.aegis.domain.model.RescuePacket
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json

/**
 * WorkManager worker that retries all PENDING/SENDING/FAILED outbox SOS packets
 * when network connectivity is restored. Re-dispatches each stored packet
 * through the real OkHttp [AegisApi]; on a server ack the outbox row is marked
 * SENT, otherwise it stays retryable.
 *
 * This guarantees that offline SOS alerts are eventually delivered once the
 * device regains connectivity (offline-first architecture).
 */
class SosRetryWorker(
  appContext: Context,
  workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

  companion object {
    private const val UNIQUE_WORK_NAME = "aegis_sos_retry"

    /**
     * Enqueue a one-time retry job that will execute when network is available.
     * Safe to call multiple times — uses KEEP policy to avoid duplicates.
     */
    fun enqueueRetry(context: Context) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val retryRequest = OneTimeWorkRequestBuilder<SosRetryWorker>()
        .setConstraints(constraints)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .build()

      WorkManager.getInstance(context).enqueueUniqueWork(
        UNIQUE_WORK_NAME,
        ExistingWorkPolicy.KEEP, // Don't replace if already enqueued
        retryRequest,
      )
    }
  }

  override suspend fun doWork(): Result {
    val container = (applicationContext as AegisApplication).container
    val outboxDao = container.database.outboxDao()
    val api = container.aegisApi

    val pendingPackets = outboxDao.getPendingPackets()
    val failedPackets = outboxDao.getFailedPackets()
    val allRetryable = (pendingPackets + failedPackets).distinctBy { it.packetId }
    if (allRetryable.isEmpty()) return Result.success()

    var allSucceeded = true

    for (entry in allRetryable) {
      try {
        outboxDao.markRetrying(entry.packetId)

        val packet = parseRescuePacket(entry)
        val dto = SosRequestDto(
          packetId = packet.packetId,
          touristId = packet.touristId,
          lat = packet.latitude,
          lon = packet.longitude,
          batteryPct = packet.batteryPercent,
          channel = "HTTPS",
          rawSmsPayload = null,
        )

        val response = api.submitSos(dto)
        if (response.success && response.incidentId != null) {
          outboxDao.markSent(
            packetId = entry.packetId,
            status = "SENT",
            serverAckId = response.incidentId,
            transportUsed = "HTTPS",
          )
        } else {
          outboxDao.markFailed(entry.packetId, response.message ?: "No server ack")
          allSucceeded = false
        }
      } catch (e: Exception) {
        outboxDao.markFailed(entry.packetId, e.message ?: "Retry failed")
        allSucceeded = false
      }
    }

    return if (allSucceeded) Result.success() else Result.retry()
  }

  private fun parseRescuePacket(entry: OutboxEntity): RescuePacket =
    Json { ignoreUnknownKeys = true }.decodeFromString(
      RescuePacket.serializer(),
      entry.payloadJson,
    )
}
