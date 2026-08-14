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
   * breadcrumbs plus POST_NOTIFICATIONS (Android 13+) so the foreground
   * tracking notification is actually visible.
   */
  val requiredForTrip: Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
      )
    } else {
      required
    }

  fun isGranted(context: Context, permissions: Array<String> = requiredForTrip): Boolean =
    permissions.all {
      ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}
