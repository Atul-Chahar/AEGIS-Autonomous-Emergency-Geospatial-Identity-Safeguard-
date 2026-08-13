package com.example.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aegis.data.local.entity.CheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
  @Insert
  suspend fun insert(checkIn: CheckInEntity): Long

  @Query("SELECT COUNT(*) FROM check_ins")
  fun observeCount(): Flow<Int>

  @Query("SELECT * FROM check_ins ORDER BY timestampEpochMillis DESC LIMIT 20")
  fun observeRecent(): Flow<List<CheckInEntity>>
}
