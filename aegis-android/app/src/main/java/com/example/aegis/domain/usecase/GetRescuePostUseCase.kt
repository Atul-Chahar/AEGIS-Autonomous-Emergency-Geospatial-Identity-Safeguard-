package com.example.aegis.domain.usecase

import com.example.aegis.data.repository.SafetyZoneRepository
import com.example.aegis.domain.model.RescuePost

class GetRescuePostUseCase(private val repository: SafetyZoneRepository) {
  operator fun invoke(): RescuePost = repository.getRescuePost()
}
