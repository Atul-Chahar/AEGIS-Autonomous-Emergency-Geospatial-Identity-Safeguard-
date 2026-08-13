package com.example.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outbox")
data class OutboxEntity(
  @PrimaryKey val packetId: String,
  val eventType: String,
  val payloadJson: String,
  val status: String, // PENDING, SENDING, SENT, FAILED
  val attemptCount: Int = 0,
  val lastAttemptTime: Long? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val serverAckId: String? = null,
  val transportUsed: String? = null,
  val errorMessage: String? = null,
)
