package com.example.aegis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "relay_inbox")
data class RelayInboxEntity(
  @PrimaryKey val packetId: String,
  val originTouristId: String,
  val payloadJson: String,
  val priority: String,
  val hopCount: Int,
  val ttl: Int,
  val receivedAtEpochMillis: Long = System.currentTimeMillis(),
  val status: String = "STORED_PENDING_RELAY", // STORED_PENDING_RELAY, RELAYED_TO_INTERNET, EXPIRED
  val relayedAtEpochMillis: Long? = null,
  val serverAckId: String? = null,
)
