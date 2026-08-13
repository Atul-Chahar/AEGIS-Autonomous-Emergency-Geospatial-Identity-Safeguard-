package com.example.aegis.transport

import android.util.Base64
import com.example.aegis.domain.model.SosRequest
import java.util.Locale

/**
 * Zero-cost compact SOS payload, matching the backend SMS decoder
 * (`aegis-backend/src/server.js`: `SOS:ID|LAT|LON|BAT`, Base64-encoded).
 */
object SosPayloadCodec {

  fun encodeCompact(request: SosRequest): String {
    val lat = request.latitude?.let { formatCoord(it) } ?: "0"
    val lon = request.longitude?.let { formatCoord(it) } ?: "0"
    val battery = request.batteryPct?.toString() ?: "0"
    return "SOS:${request.touristId}|$lat|$lon|$battery"
  }

  /** Base64 form sent over SMS (backend decodes before parsing). */
  fun encodeSmsPayload(request: SosRequest): String =
    Base64.encodeToString(encodeCompact(request).toByteArray(Charsets.US_ASCII), Base64.NO_WRAP)

  private fun formatCoord(value: Double): String = String.format(Locale.US, "%.3f", value)
}
