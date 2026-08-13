package com.example.aegis.data.repository.demo

import com.example.aegis.data.repository.EmergencyRepository
import com.example.aegis.domain.model.SosDispatchResult
import com.example.aegis.domain.model.SosRequest

/**
 * Placeholder emergency repository. Dispatch is NOT wired to a transport yet,
 * so it honestly returns [SosDispatchResult.NotAvailable] — never a fake
 * success. The WebSocket/SMS transport ships in a later stage.
 */
class DemoEmergencyRepository : EmergencyRepository {
  override suspend fun dispatchSos(request: SosRequest): SosDispatchResult =
    SosDispatchResult.NotAvailable(
      "Emergency transport is not connected yet — WebSocket + SMS fallback dispatch ships in a later stage.",
    )
}
