package com.example.aegis.location

/** Source of the device's current GNSS location. */
interface LocationProvider {
  suspend fun currentLocation(): LocationResult
}
