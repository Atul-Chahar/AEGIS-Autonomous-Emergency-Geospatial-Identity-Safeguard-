package com.example.aegis.domain.usecase

import com.example.aegis.data.repository.SafetyZoneRepository
import com.example.aegis.domain.model.SafetyZone

class GetZoneByIdUseCase(private val repository: SafetyZoneRepository) {
  suspend operator fun invoke(zoneId: String): SafetyZone? = repository.getZoneById(zoneId)
}
