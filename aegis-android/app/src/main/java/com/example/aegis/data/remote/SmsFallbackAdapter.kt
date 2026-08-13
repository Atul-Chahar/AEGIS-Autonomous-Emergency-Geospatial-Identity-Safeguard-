package com.example.aegis.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.aegis.domain.model.RescuePacket

class SmsFallbackAdapter {

  /** Formats a compact SOS payload string for SMS encoding. */
  fun formatSmsPayload(packet: RescuePacket): String {
    val lat = packet.latitude?.let { String.format(java.util.Locale.US, "%.3f", it) } ?: "0.000"
    val lon = packet.longitude?.let { String.format(java.util.Locale.US, "%.3f", it) } ?: "0.000"
    val bat = packet.batteryPercent ?: 0
    return "SOS:${packet.touristId}|$lat|$lon|${bat}%"
  }

  /**
   * Creates an explicit user-confirmed Android SMS handoff Intent (`smsto:`).
   * Safe for non-privileged apps — requires user interaction before transmitting.
   */
  fun createSmsIntent(
    recipientNumber: String = "112",
    packet: RescuePacket,
  ): Intent {
    val body = formatSmsPayload(packet)
    val uri = Uri.parse("smsto:$recipientNumber")
    return Intent(Intent.ACTION_SENDTO, uri).apply {
      putExtra("sms_body", body)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
  }

  fun launchSmsHandoff(context: Context, packet: RescuePacket, recipientNumber: String = "112"): Boolean {
    return try {
      val intent = createSmsIntent(recipientNumber, packet)
      context.startActivity(intent)
      true
    } catch (e: Exception) {
      false
    }
  }
}
