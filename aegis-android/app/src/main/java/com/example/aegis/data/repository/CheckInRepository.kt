package com.example.aegis.data.repository

import kotlinx.coroutines.flow.Flow

/** Records "I'm safe" / periodic check-in events locally (offline-first). */
interface CheckInRepository {
  suspend fun recordCheckIn(latitude: Double?, longitude: Double?): Long

  fun observeCheckInCount(): Flow<Int>
}
