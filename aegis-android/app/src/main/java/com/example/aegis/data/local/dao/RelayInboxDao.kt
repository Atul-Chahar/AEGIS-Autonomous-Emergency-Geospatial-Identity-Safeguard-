package com.example.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.aegis.data.local.entity.RelayInboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RelayInboxDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(packet: RelayInboxEntity): Long

  @Update
  suspend fun update(packet: RelayInboxEntity)

  @Query("SELECT * FROM relay_inbox WHERE packetId = :packetId")
  suspend fun getPacketById(packetId: String): RelayInboxEntity?

  @Query("SELECT * FROM relay_inbox WHERE status = 'STORED_PENDING_RELAY' ORDER BY CASE priority WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 ELSE 3 END ASC, receivedAtEpochMillis ASC")
  suspend fun getPendingRelayPackets(): List<RelayInboxEntity>

  @Query("SELECT COUNT(*) FROM relay_inbox WHERE status = 'STORED_PENDING_RELAY'")
  fun observePendingRelayCount(): Flow<Int>

  @Query("UPDATE relay_inbox SET status = 'RELAYED_TO_INTERNET', serverAckId = :serverAckId, relayedAtEpochMillis = :relayedAt WHERE packetId = :packetId")
  suspend fun markRelayed(packetId: String, serverAckId: String, relayedAt: Long = System.currentTimeMillis())
}
