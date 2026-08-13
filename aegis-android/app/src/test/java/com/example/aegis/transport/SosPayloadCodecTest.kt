package com.example.aegis.transport

import com.example.aegis.domain.model.SosRequest
import org.junit.Assert.assertEquals
import org.junit.Test

class SosPayloadCodecTest {

  private fun request(
    touristId: String = "TST-8F29X4",
    latitude: Double? = null,
    longitude: Double? = null,
    batteryPct: Int? = null,
  ) =
    SosRequest(
      touristId = touristId,
      zoneId = null,
      latitude = latitude,
      longitude = longitude,
      batteryPct = batteryPct,
      timestampEpochMillis = 1_752_000_000_000L,
    )

  @Test
  fun `missing sensor data encodes as zeros`() {
    val payload = SosPayloadCodec.encodeCompact(request())
    assertEquals("SOS:TST-8F29X4|0|0|0", payload)
  }

  @Test
  fun `coordinates are truncated to three decimals`() {
    val payload = SosPayloadCodec.encodeCompact(request(latitude = 25.2742, longitude = 91.6964, batteryPct = 87))
    assertEquals("SOS:TST-8F29X4|25.274|91.696|87", payload)
  }

  @Test
  fun `tourist id with special characters survives encoding`() {
    val payload = SosPayloadCodec.encodeCompact(request(touristId = "TST-ABC-123"))
    assertEquals("SOS:TST-ABC-123|0|0|0", payload)
  }
}
