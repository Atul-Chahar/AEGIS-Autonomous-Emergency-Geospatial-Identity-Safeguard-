package com.example.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aegis.data.local.entity.ZoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ZoneDao {
  @Query("SELECT * FROM zones ORDER BY riskScore ASC")
  fun observeZones(): Flow<List<ZoneEntity>>

  @Query("SELECT * FROM zones WHERE id = :zoneId LIMIT 1")
  suspend fun getZoneById(zoneId: String): ZoneEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(zones: List<ZoneEntity>)

  @Query("DELETE FROM zones")
  suspend fun clearAll()
}
