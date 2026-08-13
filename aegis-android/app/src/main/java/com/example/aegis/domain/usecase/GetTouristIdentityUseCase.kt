package com.example.aegis.domain.usecase

import com.example.aegis.data.repository.IdentityRepository
import com.example.aegis.domain.model.TouristIdentity
import kotlinx.coroutines.flow.Flow

class GetTouristIdentityUseCase(private val repository: IdentityRepository) {
  operator fun invoke(): Flow<TouristIdentity> = repository.observeIdentity()
}
