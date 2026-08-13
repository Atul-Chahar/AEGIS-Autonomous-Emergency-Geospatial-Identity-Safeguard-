package com.example.aegis.domain.model

/**
 * Result of an SOS dispatch attempt. `Dispatched` is only returned by a real
 * transport implementation; until then callers get [NotAvailable] with the
 * real reason — never a fabricated success.
 */
sealed interface SosDispatchResult {
  data object Dispatched : SosDispatchResult

  data class NotAvailable(val reason: String) : SosDispatchResult
}
