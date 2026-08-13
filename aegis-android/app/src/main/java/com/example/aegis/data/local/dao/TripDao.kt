package com.example.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.aegis.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTrip(trip: TripEntity)

  @Update
  suspend fun updateTrip(trip: TripEntity)

  @Query("SELECT * FROM trips WHERE status = 'ACTIVE' ORDER BY startedAt DESC LIMIT 1")
  suspend fun getActiveTrip(): TripEntity?

  @Query("SELECT * FROM trips WHERE status = 'ACTIVE' ORDER BY startedAt DESC LIMIT 1")
  fun observeActiveTrip(): Flow<TripEntity?>

  @Query("SELECT * FROM trips WHERE tripId = :tripId")
  suspend fun getTripById(tripId: String): TripEntity?
}
