package com.example.aegis.transport

import com.example.aegis.domain.model.SosRequest

/** Outcome of an actual transport attempt. */
sealed interface TransportResult {
  data object Sent : TransportResult
  data class Failed(val reason: String) : TransportResult
}

/**
 * Emergency transport channel (WebSocket / zero-cost SMS fallback).
 * Implementations arrive in the next stage — the interface is the contract the
 * emergency repository will route through.
 */
interface EmergencyTransport {
  suspend fun dispatch(request: SosRequest, payload: String): TransportResult
}
