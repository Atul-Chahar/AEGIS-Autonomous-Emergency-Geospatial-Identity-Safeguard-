package com.example.aegis.data.remote

import com.example.aegis.BuildConfig

/**
 * Backend gateway configuration. The base URL is injected at build time
 * (`aegisBackendBaseUrl` Gradle property, default `http://10.0.2.2:5000` for
 * the emulator) — never hardcode localhost in application code.
 */
object ApiConfig {
  val backendBaseUrl: String = BuildConfig.AEGIS_BACKEND_BASE_URL

  val healthUrl: String = "$backendBaseUrl/api/health"
  val identityRegisterUrl: String = "$backendBaseUrl/api/identity/register"
  val geofencesUrl: String = "$backendBaseUrl/api/geofences"
  val sosUrl: String = "$backendBaseUrl/api/sos"
  val incidentsUrl: String = "$backendBaseUrl/api/incidents"

  /** WebSocket endpoint for live emergency broadcasts (backend `/ws`). */
  val eventsWebSocketUrl: String =
    backendBaseUrl.replaceFirst("http", "ws") + "/ws"
}
