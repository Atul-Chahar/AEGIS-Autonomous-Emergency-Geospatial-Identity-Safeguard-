package com.example.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aegis.data.local.entity.SensorEventChunkEntity

@Dao
interface SensorEventChunkDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChunk(chunk: SensorEventChunkEntity)

  @Query("SELECT * FROM sensor_event_chunks WHERE tripId = :tripId ORDER BY eventTimestamp ASC")
  suspend fun getChunksForTrip(tripId: String): List<SensorEventChunkEntity>
}
