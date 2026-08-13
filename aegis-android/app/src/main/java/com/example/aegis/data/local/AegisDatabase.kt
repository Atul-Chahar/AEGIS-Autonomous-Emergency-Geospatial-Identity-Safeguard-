package com.example.aegis.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.aegis.data.local.dao.BreadcrumbDao
import com.example.aegis.data.local.dao.CheckInDao
import com.example.aegis.data.local.dao.OutboxDao
import com.example.aegis.data.local.dao.SensorEventChunkDao
import com.example.aegis.data.local.dao.TripDao
import com.example.aegis.data.local.dao.ZoneDao
import com.example.aegis.data.local.entity.BreadcrumbEntity
import com.example.aegis.data.local.entity.CheckInEntity
import com.example.aegis.data.local.entity.OutboxEntity
import com.example.aegis.data.local.entity.SensorEventChunkEntity
import com.example.aegis.data.local.entity.TripEntity
import com.example.aegis.data.local.entity.ZoneEntity

@Database(
  entities = [
    ZoneEntity::class,
    CheckInEntity::class,
    TripEntity::class,
    BreadcrumbEntity::class,
    SensorEventChunkEntity::class,
    OutboxEntity::class,
  ],
  version = 3,
  exportSchema = false,
)
abstract class AegisDatabase : RoomDatabase() {
  abstract fun zoneDao(): ZoneDao
  abstract fun checkInDao(): CheckInDao
  abstract fun tripDao(): TripDao
  abstract fun breadcrumbDao(): BreadcrumbDao
  abstract fun sensorEventChunkDao(): SensorEventChunkDao
  abstract fun outboxDao(): OutboxDao

  companion object {
    @Volatile private var instance: AegisDatabase? = null

    fun getInstance(context: Context): AegisDatabase =
      instance ?: synchronized(this) {
        instance
          ?: Room.databaseBuilder(context.applicationContext, AegisDatabase::class.java, "aegis.db")
            .fallbackToDestructiveMigration()
            .build()
            .also { instance = it }
      }
  }
}
