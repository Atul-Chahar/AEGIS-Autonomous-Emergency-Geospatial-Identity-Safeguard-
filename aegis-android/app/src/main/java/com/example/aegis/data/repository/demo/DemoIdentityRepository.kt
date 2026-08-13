package com.example.aegis.data.repository.demo

import com.example.aegis.data.repository.IdentityRepository
import com.example.aegis.domain.model.IdentityStatus
import com.example.aegis.domain.model.TouristIdentity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * DEMO / PREVIEW identity source — a local-only voucher. Blockchain proof,
 * checkpoint scanning and revocation are NOT implemented yet, so nothing here
 * claims they are.
 */
class DemoIdentityRepository : IdentityRepository {
  override fun observeIdentity(): Flow<TouristIdentity> = flow {
    emit(
      TouristIdentity(
        touristId = "TST-8F29X4",
        displayName = "Aryan",
        status = IdentityStatus.ACTIVE,
        validFrom = "12 Aug 2026",
        validTo = "20 Aug 2026",
      ),
    )
  }
}
