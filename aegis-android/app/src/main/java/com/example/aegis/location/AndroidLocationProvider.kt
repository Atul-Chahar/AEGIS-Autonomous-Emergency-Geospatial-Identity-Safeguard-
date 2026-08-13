package com.example.aegis.location

import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** Real location provider backed by Google Play Services FusedLocationProvider. */
class AndroidLocationProvider(private val context: Context) : LocationProvider {

  private val fusedLocationClient =
    LocationServices.getFusedLocationProviderClient(context.applicationContext)

  override suspend fun currentLocation(): LocationResult {
    if (!LocationPermissions.isGranted(context)) {
      return LocationResult.Unavailable("Location permission not granted")
    }
    return suspendCancellableCoroutine { continuation ->
      val cancellationTokenSource = CancellationTokenSource()
      val task =
        fusedLocationClient.getCurrentLocation(
          Priority.PRIORITY_HIGH_ACCURACY,
          cancellationTokenSource.token,
        )
      task.addOnSuccessListener { location ->
        if (continuation.isActive) {
          if (location != null) {
            continuation.resume(
              LocationResult.Success(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = location.accuracy,
                timestampEpochMillis = location.time,
                speedMps = if (location.hasSpeed()) location.speed else null,
                bearingDegrees = if (location.hasBearing()) location.bearing else null,
                altitudeMeters = if (location.hasAltitude()) location.altitude else null,
              ),
            )
          } else {
            continuation.resume(LocationResult.Unavailable("No location fix available yet"))
          }
        }
      }
      task.addOnFailureListener { error ->
        if (continuation.isActive) {
          continuation.resume(
            LocationResult.Unavailable(error.message ?: "Location request failed"),
          )
        }
      }
      continuation.invokeOnCancellation { cancellationTokenSource.cancel() }
    }
  }
}
