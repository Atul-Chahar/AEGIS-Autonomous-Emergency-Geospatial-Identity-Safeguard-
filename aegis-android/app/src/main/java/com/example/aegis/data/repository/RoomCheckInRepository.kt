package com.example.aegis.data.repository

import com.example.aegis.data.local.dao.CheckInDao
import com.example.aegis.data.local.entity.CheckInEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Room-backed check-in repository — records events locally first (offline-first). */
class RoomCheckInRepository(
  private val checkInDao: CheckInDao,
  private val identityRepository: IdentityRepository,
) : CheckInRepository {

  override suspend fun recordCheckIn(latitude: Double?, longitude: Double?): Long {
    val touristId = identityRepository.observeIdentity().first().touristId
    return checkInDao.insert(
      CheckInEntity(
        touristId = touristId,
        latitude = latitude,
        longitude = longitude,
        timestampEpochMillis = System.currentTimeMillis(),
      ),
    )
  }

  override fun observeCheckInCount(): Flow<Int> = checkInDao.observeCount()

  override fun observeRecentCheckIns(): Flow<List<com.example.aegis.domain.model.CheckIn>> =
    checkInDao.observeRecent().map { entities ->
      entities.map {
        com.example.aegis.domain.model.CheckIn(
          id = it.id,
          touristId = it.touristId,
          latitude = it.latitude,
          longitude = it.longitude,
          timestampEpochMillis = it.timestampEpochMillis,
        )
      }
    }
}
