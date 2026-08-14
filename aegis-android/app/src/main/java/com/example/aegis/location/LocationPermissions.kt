package com.example.aegis.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/** Location runtime-permission contract. Permissions are requested only when a location feature starts. */
object LocationPermissions {
  val required: Array<String> =
    arrayOf(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION,
    )

  /**
   * Permissions needed to start a tracked journey:
   *  - location for the BlackBox breadcrumbs,
   *  - POST_NOTIFICATIONS (Android 13+) for the foreground tracking notification,
   *  - Bluetooth (Android 12+) for the offline peer relay (Nearby Connections),
   *  - NEARBY_WIFI_DEVICES (Android 13+) so the mesh can also relay over WiFi Direct,
   *  - ACTIVITY_RECOGNITION so TripTrackingService's inactivity monitor works
   *    (declared in the manifest but must be runtime-requested; on API < 29 it
   *    is auto-granted at install and requesting it is a harmless no-op).
   */
  val requiredForTrip: Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.NEARBY_WIFI_DEVICES,
        Manifest.permission.ACTIVITY_RECOGNITION,
      )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACTIVITY_RECOGNITION,
      )
    } else {
      required + arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
    }

  fun isGranted(context: Context, permissions: Array<String> = requiredForTrip): Boolean =
    permissions.all {
      ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}
