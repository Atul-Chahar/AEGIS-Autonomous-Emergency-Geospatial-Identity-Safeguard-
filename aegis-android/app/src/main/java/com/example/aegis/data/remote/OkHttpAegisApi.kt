package com.example.aegis.data.remote

import com.example.aegis.data.remote.dto.BreadcrumbSyncDto
import com.example.aegis.data.remote.dto.BreadcrumbSyncRequest
import com.example.aegis.data.remote.dto.GeofenceDto
import com.example.aegis.data.remote.dto.HealthDto
import com.example.aegis.data.remote.dto.IdentityRegisterRequest
import com.example.aegis.data.remote.dto.IdentityRegisterResponse
import com.example.aegis.data.remote.dto.IncidentDto
import com.example.aegis.data.remote.dto.SosRequestDto
import com.example.aegis.data.remote.dto.SosResponseDto
import com.example.aegis.data.remote.dto.TripSyncDto
import com.example.aegis.data.remote.dto.TripSyncRequest
import java.io.IOException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Real OkHttp transport for the AEGIS backend gateway
 * (`aegis-backend/src/server.js`). All calls are suspend functions that run
 * on the IO dispatcher; JSON is decoded with kotlinx.serialization.
 *
 * Base URL comes from [ApiConfig] (BuildConfig `AEGIS_BACKEND_BASE_URL`,
 * default `http://10.0.2.2:5000` for the emulator) — never hardcoded here.
 */
class OkHttpAegisApi(
  private val baseUrl: String = ApiConfig.backendBaseUrl,
  private val client: OkHttpClient = NetworkModule.okHttpClient,
  private val json: Json = NetworkModule.json,
) : AegisApi {

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  override suspend fun health(): HealthDto =
    get("$baseUrl/api/health", HealthDto.serializer())

  override suspend fun getGeofences(): List<GeofenceDto> =
    getList("$baseUrl/api/geofences", GeofenceDto.serializer())

  override suspend fun registerIdentity(request: IdentityRegisterRequest): IdentityRegisterResponse =
    post("$baseUrl/api/identity/register", request, IdentityRegisterRequest.serializer(), IdentityRegisterResponse.serializer())

  override suspend fun submitSos(request: SosRequestDto): SosResponseDto =
    post("$baseUrl/api/sos", request, SosRequestDto.serializer(), SosResponseDto.serializer())

  override suspend fun getIncidents(): List<IncidentDto> =
    getList("$baseUrl/api/incidents", IncidentDto.serializer())

  /** Registers the active trip on the gateway (BlackBox sync). */
  suspend fun startTrip(request: TripSyncRequest): TripSyncDto =
    post("$baseUrl/api/trips", request, TripSyncRequest.serializer(), TripSyncDto.serializer())

  /** Appends a breadcrumb to the trip trail on the gateway (BlackBox sync). */
  suspend fun submitBreadcrumb(request: BreadcrumbSyncRequest): BreadcrumbSyncDto =
    post("$baseUrl/api/breadcrumbs", request, BreadcrumbSyncRequest.serializer(), BreadcrumbSyncDto.serializer())

  private suspend fun <T> get(url: String, serializer: KSerializer<T>): T =
    withContext(Dispatchers.IO) {
      val request = Request.Builder().url(url).get().build()
      client.newCall(request).execute().use { response ->
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful) throw IOException("GET $url -> HTTP ${response.code}: $body")
        json.decodeFromString(serializer, body)
      }
    }

  private suspend fun <T> getList(url: String, serializer: KSerializer<T>): List<T> =
    withContext(Dispatchers.IO) {
      val request = Request.Builder().url(url).get().build()
      client.newCall(request).execute().use { response ->
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful) throw IOException("GET $url -> HTTP ${response.code}: $body")
        json.decodeFromString(ListSerializer(serializer), body)
      }
    }

  private suspend fun <T, R> post(
    url: String,
    body: T,
    bodySerializer: KSerializer<T>,
    responseSerializer: KSerializer<R>,
  ): R =
    withContext(Dispatchers.IO) {
      val payload = json.encodeToString(bodySerializer, body)
      val request =
        Request.Builder()
          .url(url)
          .post(payload.toRequestBody(jsonMediaType))
          .build()
      client.newCall(request).execute().use { response ->
        val responseBody = response.body?.string().orEmpty()
        if (!response.isSuccessful) throw IOException("POST $url -> HTTP ${response.code}: $responseBody")
        json.decodeFromString(responseSerializer, responseBody)
      }
    }
}
