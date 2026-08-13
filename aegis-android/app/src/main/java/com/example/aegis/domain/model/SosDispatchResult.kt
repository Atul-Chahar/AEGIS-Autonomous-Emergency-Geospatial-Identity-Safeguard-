package com.example.aegis.domain.model

/**
 * Result of an SOS dispatch attempt. Honest delivery status — returned by
 * the transport implementation. Never displays fake checkmarks or fabricated success.
 */
sealed interface SosDispatchResult {
  data class Sent(
    val transport: String,
    val ackId: String,
    val timestamp: Long = System.currentTimeMillis(),
  ) : SosDispatchResult

  data class PendingSmsFallback(
    val packetId: String,
    val smsPayload: String,
    val reason: String = "Waiting for connectivity — SMS handoff ready",
  ) : SosDispatchResult

  data class Failed(val reason: String) : SosDispatchResult

  data class NotAvailable(val reason: String) : SosDispatchResult

  data object Dispatched : SosDispatchResult
}
