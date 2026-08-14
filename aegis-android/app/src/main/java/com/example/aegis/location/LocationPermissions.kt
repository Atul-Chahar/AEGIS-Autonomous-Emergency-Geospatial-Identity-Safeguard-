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
   * Permissions needed to start a tracked journey: location for the BlackBox
   * breadcrumbs, POST_NOTIFICATIONS (Android 13+) for the foreground tracking
   * notification, and Bluetooth (Android 12+) for the offline peer relay.
   */
  val requiredForTrip: Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
      )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
      )
    } else {
      required
    }

  fun isGranted(context: Context, permissions: Array<String> = requiredForTrip): Boolean =
    permissions.all {
      ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}
