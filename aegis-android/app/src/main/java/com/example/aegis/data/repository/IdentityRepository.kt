package com.example.aegis.data.repository

import com.example.aegis.domain.model.TouristIdentity
import kotlinx.coroutines.flow.Flow

/** Source of the tourist identity voucher. */
interface IdentityRepository {
  fun observeIdentity(): Flow<TouristIdentity>
}
