package com.example.aegis.data.remote

import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

/** Central place for the HTTP/WebSocket client and JSON codec. */
object NetworkModule {

  val json: Json =
    Json {
      ignoreUnknownKeys = true
      encodeDefaults = true
    }

  val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(10, TimeUnit.SECONDS)
      .readTimeout(15, TimeUnit.SECONDS)
      .writeTimeout(15, TimeUnit.SECONDS)
      .pingInterval(20, TimeUnit.SECONDS) // keep WebSocket connections alive
      .build()
  }
}
