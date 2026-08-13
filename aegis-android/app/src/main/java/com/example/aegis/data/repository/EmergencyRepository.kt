package com.example.aegis.data.repository

import com.example.aegis.domain.model.SosDispatchResult
import com.example.aegis.domain.model.SosRequest

/** Dispatches emergency SOS requests through the transport layer. */
interface EmergencyRepository {
  suspend fun dispatchSos(request: SosRequest): SosDispatchResult
}
