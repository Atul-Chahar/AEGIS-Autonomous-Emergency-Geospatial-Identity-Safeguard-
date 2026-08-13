package com.example.aegis.sensors

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Real Activity Recognition provider (Google Play Services). Emits the most
 * probable detected activity every [intervalMillis]. Requires the
 * ACTIVITY_RECOGNITION runtime permission — the monitor only starts observing
 * when the user starts the relevant feature.
 */
class AndroidActivityRecognitionProvider(
  context: Context,
  private val intervalMillis: Long = 60_000L,
) : ActivityRecognitionProvider {

  private val appContext = context.applicationContext
  private val client = ActivityRecognition.getClient(appContext)

  override fun observeActivity(): Flow<DetectedActivity> = callbackFlow {
    val receiver =
      object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
          val result = ActivityRecognitionResult.extractResult(intent) ?: return
          val mostProbable = result.mostProbableActivity ?: return
          trySend(DetectedActivity(mostProbable.type, mostProbable.confidence))
        }
      }

    if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACTIVITY_RECOGNITION) !=
      PackageManager.PERMISSION_GRANTED
    ) {
      close(SecurityException("ACTIVITY_RECOGNITION permission not granted"))
      awaitClose { }
      return@callbackFlow
    }

    val filter = IntentFilter(ACTION_ACTIVITY_RESULT)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    } else {
      @Suppress("UnspecifiedRegisterReceiverFlag")
      appContext.registerReceiver(receiver, filter)
    }

    val pendingIntent =
      PendingIntent.getBroadcast(
        appContext,
        0,
        Intent(ACTION_ACTIVITY_RESULT).setPackage(appContext.packageName),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
      )

    client.requestActivityUpdates(intervalMillis, pendingIntent).addOnFailureListener { error ->
      close(error)
    }

    awaitClose {
      client.removeActivityUpdates(pendingIntent)
      runCatching { appContext.unregisterReceiver(receiver) }
    }
  }

  private companion object {
    const val ACTION_ACTIVITY_RESULT = "com.example.aegis.ACTIVITY_RECOGNITION_RESULT"
  }
}
