package com.example.aegis.domain.usecase

import com.example.aegis.data.repository.SafetyZoneRepository
import com.example.aegis.domain.model.SafetyZone
import kotlinx.coroutines.flow.Flow

class ObserveSafetyZonesUseCase(private val repository: SafetyZoneRepository) {
  operator fun invoke(): Flow<List<SafetyZone>> = repository.observeZones()
}
