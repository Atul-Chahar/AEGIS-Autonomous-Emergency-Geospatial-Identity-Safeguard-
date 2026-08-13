package com.example.aegis.domain.usecase

import com.example.aegis.data.repository.EmergencyRepository
import com.example.aegis.domain.model.SosDispatchResult
import com.example.aegis.domain.model.SosRequest

class DispatchSosUseCase(private val repository: EmergencyRepository) {
  suspend operator fun invoke(request: SosRequest): SosDispatchResult = repository.dispatchSos(request)
}
