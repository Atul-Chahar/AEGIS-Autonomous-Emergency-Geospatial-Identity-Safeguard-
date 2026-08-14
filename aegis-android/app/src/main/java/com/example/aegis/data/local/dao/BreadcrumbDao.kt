package com.example.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aegis.data.local.entity.BreadcrumbEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BreadcrumbDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBreadcrumb(breadcrumb: BreadcrumbEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBreadcrumbs(breadcrumbs: List<BreadcrumbEntity>)

  @Query("SELECT * FROM breadcrumbs WHERE tripId = :tripId ORDER BY timestamp DESC LIMIT 1")
  suspend fun getLatestBreadcrumb(tripId: String): BreadcrumbEntity?

  @Query("SELECT * FROM breadcrumbs WHERE tripId = :tripId ORDER BY timestamp DESC LIMIT 1")
  fun observeLatestBreadcrumb(tripId: String): Flow<BreadcrumbEntity?>

  @Query("SELECT * FROM breadcrumbs ORDER BY timestamp DESC LIMIT 1")
  fun observeLatestBreadcrumbAny(): Flow<BreadcrumbEntity?>

  @Query("SELECT * FROM breadcrumbs WHERE tripId = :tripId ORDER BY timestamp ASC")
  suspend fun getBreadcrumbsForTrip(tripId: String): List<BreadcrumbEntity>

  @Query("SELECT * FROM breadcrumbs WHERE syncState = 'PENDING' ORDER BY timestamp ASC")
  suspend fun getUnsyncedBreadcrumbs(): List<BreadcrumbEntity>

  @Query("UPDATE breadcrumbs SET syncState = 'SYNCED' WHERE breadcrumbId IN (:breadcrumbIds)")
  suspend fun markBreadcrumbsSynced(breadcrumbIds: List<String>)
}
