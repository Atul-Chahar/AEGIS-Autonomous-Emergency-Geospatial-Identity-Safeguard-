package com.example.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.aegis.data.local.entity.OutboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(packet: OutboxEntity): Long

  @Update
  suspend fun update(packet: OutboxEntity)

  @Query("SELECT * FROM outbox WHERE packetId = :packetId")
  suspend fun getPacketById(packetId: String): OutboxEntity?

  @Query("SELECT * FROM outbox WHERE status = 'PENDING' ORDER BY createdAt ASC")
  suspend fun getPendingPackets(): List<OutboxEntity>

  @Query("SELECT COUNT(*) FROM outbox WHERE status = 'PENDING'")
  fun observePendingCount(): Flow<Int>

  @Query("SELECT * FROM outbox WHERE packetId = :packetId")
  fun observePacket(packetId: String): Flow<OutboxEntity?>

  @Query("UPDATE outbox SET status = :status, serverAckId = :serverAckId, transportUsed = :transportUsed, attemptCount = attemptCount + 1, lastAttemptTime = :lastAttemptTime WHERE packetId = :packetId")
  suspend fun markSent(packetId: String, status: String = "SENT", serverAckId: String, transportUsed: String, lastAttemptTime: Long = System.currentTimeMillis())

  @Query("UPDATE outbox SET status = 'FAILED', errorMessage = :reason, attemptCount = attemptCount + 1, lastAttemptTime = :lastAttemptTime WHERE packetId = :packetId")
  suspend fun markFailed(packetId: String, reason: String, lastAttemptTime: Long = System.currentTimeMillis())

  @Query("SELECT * FROM outbox WHERE status = 'FAILED' AND attemptCount < 10 ORDER BY createdAt ASC")
  suspend fun getFailedPackets(): List<OutboxEntity>

  @Query("UPDATE outbox SET status = 'SENDING', attemptCount = attemptCount + 1, lastAttemptTime = :lastAttemptTime WHERE packetId = :packetId")
  suspend fun markRetrying(packetId: String, lastAttemptTime: Long = System.currentTimeMillis())
}
