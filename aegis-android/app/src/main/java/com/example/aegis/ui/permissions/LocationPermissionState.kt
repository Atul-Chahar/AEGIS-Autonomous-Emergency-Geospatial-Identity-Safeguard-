package com.example.aegis.ui.permissions

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.aegis.location.LocationPermissions

/** Holds the current location-permission state and a request() action. */
class LocationPermissionState(
  val isGranted: Boolean,
  val request: () -> Unit,
)

/**
 * Location runtime-permission helper. Call [LocationPermissionState.request]
 * only when the user starts a location-dependent feature (e.g. Start Route) —
 * never at app launch.
 */
@Composable
fun rememberLocationPermissionState(): LocationPermissionState {
  val context: Context = LocalContext.current
  var isGranted by remember { mutableStateOf(LocationPermissions.isGranted(context)) }

  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
      isGranted = it.values.all { granted -> granted }
    }

  return LocationPermissionState(isGranted = isGranted, request = { launcher.launch(LocationPermissions.required) })
}
