package com.example.aegis.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/** Location runtime-permission contract. Permissions are requested only when a location feature starts. */
object LocationPermissions {
  val required: Array<String> =
    arrayOf(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION,
    )

  fun isGranted(context: Context): Boolean =
    required.all {
      ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}
