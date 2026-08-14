package com.example.aegis.data.repository

import kotlinx.coroutines.flow.Flow

/** Records "I'm safe" / periodic check-in events locally (offline-first). */
interface CheckInRepository {
  suspend fun recordCheckIn(latitude: Double?, longitude: Double?): Long

  fun observeCheckInCount(): Flow<Int>

  /** Recent locally-recorded check-ins (newest first) for the Activity timeline. */
  fun observeRecentCheckIns(): Flow<List<com.example.aegis.domain.model.CheckIn>>
}
