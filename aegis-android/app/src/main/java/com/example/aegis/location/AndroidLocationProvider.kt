package com.example.aegis.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Real location provider. Primary source is Google Play Services
 * FusedLocationProvider; when Play Services is unavailable (low-cost
 * devices, AOSP builds) it falls back to the platform LocationManager
 * (GPS + network providers) and actively requests a fresh single fix —
 * never fabricates a position and never reports a stale one as live.
 */
class AndroidLocationProvider(private val context: Context) : LocationProvider {

  private val fusedLocationClient =
    LocationServices.getFusedLocationProviderClient(context.applicationContext)

  private val locationManager =
    context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

  override suspend fun currentLocation(): LocationResult {
    if (!LocationPermissions.isGranted(context)) {
      return LocationResult.Unavailable("Location permission not granted")
    }
    // Primary: Fused provider (best accuracy, needs Play Services).
    val fused = tryFusedLocation()
    if (fused is LocationResult.Success) return fused

    // Fallback: platform LocationManager — works without Play Services.
    val platform = platformLocation()
    if (platform is LocationResult.Success) return platform

    // Report the most informative reason honestly.
    return if (fused is LocationResult.Unavailable) fused else platform
  }

  private suspend fun tryFusedLocation(): LocationResult {
    // On devices without Google Play Services the fused task can hang forever
    // (no success, no failure). Time it out so the platform fallback runs.
    val result = withTimeoutOrNull(2_500L) {
      suspendCancellableCoroutine { continuation ->
        val cancellationTokenSource = CancellationTokenSource()
        val task =
          fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token,
          )
        task.addOnSuccessListener { location ->
          if (continuation.isActive) {
            continuation.resume(
              if (location != null) location.toSuccess("FUSED") else
                LocationResult.Unavailable("No fused location fix available yet"),
            )
          }
        }
        task.addOnFailureListener { error ->
          if (continuation.isActive) {
            continuation.resume(LocationResult.Unavailable(error.message ?: "Fused location request failed"))
          }
        }
        continuation.invokeOnCancellation { cancellationTokenSource.cancel() }
      }
    }
    return result ?: LocationResult.Unavailable("Fused location request timed out")
  }

  private suspend fun platformLocation(): LocationResult {
    val hasFine = isFineGranted()
    val hasCoarse = isCoarseGranted()
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    for (provider in providers) {
      val needsFine = provider == LocationManager.GPS_PROVIDER
      if (needsFine && !hasFine) continue
      if (!needsFine && !hasCoarse) continue

      try {
        // Fast path: a fresh cached fix (never older than 30s).
        val cached = locationManager.getLastKnownLocation(provider)
        if (cached != null && isFresh(cached)) return cached.toSuccess(providerLabel(provider))

        // Active path: request a fresh single fix (works when the provider is
        // idle, e.g. an emulator with an injected GPS fix or a device whose
        // cache is empty). Times out so tracking keeps polling.
        val fresh = requestSingleFix(provider)
        if (fresh != null) return fresh.toSuccess(providerLabel(provider))
      } catch (_: SecurityException) {
        // Permission revoked mid-flight; try next provider.
      } catch (_: IllegalArgumentException) {
        // Provider not present on this device (e.g. no network provider on
        // AOSP images) — skip it instead of crashing the tracking service.
      }
    }
    return LocationResult.Unavailable("No platform location fix available yet")
  }

  private suspend fun requestSingleFix(provider: String): Location? {
    return withTimeoutOrNull(4_000L) {
      suspendCancellableCoroutine { continuation ->
        lateinit var listener: LocationListener
        listener = LocationListener { location ->
          if (continuation.isActive) {
            locationManager.removeUpdates(listener)
            continuation.resume(location)
          }
        }
        try {
          locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        } catch (e: SecurityException) {
          if (continuation.isActive) continuation.resume(null)
          return@suspendCancellableCoroutine
        }
        continuation.invokeOnCancellation { locationManager.removeUpdates(listener) }
      }
    }
  }

  private fun isFineGranted(): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
      PackageManager.PERMISSION_GRANTED

  private fun isCoarseGranted(): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
      PackageManager.PERMISSION_GRANTED

  /** A fix older than 30s is treated as stale — never report stale positions as live. */
  private fun isFresh(location: Location): Boolean =
    location.time > 0L && System.currentTimeMillis() - location.time <= 30_000L

  private fun providerLabel(provider: String): String =
    if (provider == LocationManager.GPS_PROVIDER) "GPS" else "NETWORK"

  private fun Location.toSuccess(source: String): LocationResult.Success =
    LocationResult.Success(
      latitude = latitude,
      longitude = longitude,
      accuracyMeters = if (hasAccuracy()) accuracy else Float.MAX_VALUE,
      timestampEpochMillis = time,
      speedMps = if (hasSpeed()) speed else null,
      bearingDegrees = if (hasBearing()) bearing else null,
      altitudeMeters = if (hasAltitude()) altitude else null,
      source = source,
    )
}
