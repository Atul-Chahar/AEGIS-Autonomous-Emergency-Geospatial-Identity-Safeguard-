package com.example.aegis.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.aegis.AegisApplication
import com.example.aegis.data.repository.BlackBoxRepository
import com.example.aegis.domain.model.Breadcrumb
import com.example.aegis.domain.model.SensorEventChunk
import com.example.aegis.location.AndroidLocationProvider
import com.example.aegis.location.LocationProvider
import com.example.aegis.location.LocationResult
import com.example.aegis.sensors.AndroidActivityRecognitionProvider
import com.example.aegis.sensors.BatteryInfoProvider
import com.example.aegis.sensors.SensorRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

class TripTrackingService : Service() {

  companion object {
    const val CHANNEL_ID = "aegis_trip_tracking_channel"
    const val NOTIFICATION_ID = 4001
    const val ACTION_START_TRIP = "com.example.aegis.action.START_TRIP"
    const val ACTION_STOP_TRIP = "com.example.aegis.action.STOP_TRIP"
    const val EXTRA_TOURIST_ID = "extra_tourist_id"
    const val EXTRA_PLANNED_ROUTE_ID = "extra_planned_route_id"

    fun start(context: Context, touristId: String, plannedRouteId: String? = null) {
      val intent = Intent(context, TripTrackingService::class.java).apply {
        action = ACTION_START_TRIP
        putExtra(EXTRA_TOURIST_ID, touristId)
        putExtra(EXTRA_PLANNED_ROUTE_ID, plannedRouteId)
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    }

    fun stop(context: Context) {
      val intent = Intent(context, TripTrackingService::class.java).apply {
        action = ACTION_STOP_TRIP
      }
      context.startService(intent)
    }
  }

  private val serviceJob = Job()
  private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

  private lateinit var repository: BlackBoxRepository
  private lateinit var locationProvider: LocationProvider
  private lateinit var batteryInfoProvider: BatteryInfoProvider
  private lateinit var activityRecognitionProvider: AndroidActivityRecognitionProvider
  private var sensorRecorder: SensorRecorder? = null

  private var locationJob: Job? = null
  private var currentTripId: String? = null
  private var currentActivityMode: String = "STATIONARY"

  override fun onCreate() {
    super.onCreate()
    val appContainer = (application as AegisApplication).appContainer
    repository = appContainer.blackBoxRepository
    locationProvider = AndroidLocationProvider(this)
    batteryInfoProvider = BatteryInfoProvider(this)
    activityRecognitionProvider = AndroidActivityRecognitionProvider(this)

    sensorRecorder = SensorRecorder(this) { eventType, confidence, rawDataJson ->
      val tripId = currentTripId ?: return@SensorRecorder
      serviceScope.launch {
        repository.recordSensorChunk(
          SensorEventChunk(
            chunkId = UUID.randomUUID().toString(),
            tripId = tripId,
            eventType = eventType,
            eventTimestamp = System.currentTimeMillis(),
            activityMode = currentActivityMode,
            confidence = confidence,
            encryptedPayload = rawDataJson,
          )
        )
      }
    }

    createNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val action = intent?.action

    if (action == ACTION_STOP_TRIP) {
      stopTrackingAndSelf()
      return START_NOT_STICKY
    }

    val notification = buildForegroundNotification("AEGIS Safe Passage Active", "Recording route breadcrumbs & offline safety metrics.")
    startForegroundWithNotification(notification)

    serviceScope.launch {
      val touristId = intent?.getStringExtra(EXTRA_TOURIST_ID) ?: "TOURIST-DEFAULT"
      val plannedRouteId = intent?.getStringExtra(EXTRA_PLANNED_ROUTE_ID)

      val trip = repository.startTrip(touristId, plannedRouteId)
      currentTripId = trip.tripId
      startTracking(trip.tripId)
    }

    return START_STICKY
  }

  private fun startTracking(tripId: String) {
    sensorRecorder?.start()

    // Activity Recognition listener
    serviceScope.launch {
      activityRecognitionProvider.observeDetectedActivity().catch {}.collect { activity ->
        currentActivityMode = activity.type.name
      }
    }

    // Location updates listener
    locationJob?.cancel()
    locationJob = serviceScope.launch {
      locationProvider.observeLocation(intervalMillis = 5000L).catch {}.collect { locResult ->
        val batteryPct = batteryInfoProvider.getBatteryPercent()

        val breadcrumb = when (locResult) {
          is LocationResult.Fix -> Breadcrumb(
            breadcrumbId = UUID.randomUUID().toString(),
            tripId = tripId,
            timestamp = locResult.timestampEpochMillis,
            latitude = locResult.latitude,
            longitude = locResult.longitude,
            horizontalAccuracyMeters = locResult.accuracyMeters,
            altitudeMeters = locResult.altitudeMeters,
            speedMps = locResult.speedMps,
            bearingDegrees = locResult.bearingDegrees,
            batteryPercent = batteryPct,
            activityMode = currentActivityMode,
            locationSource = "FUSED",
            isEstimated = false,
            syncState = "PENDING",
          )
          is LocationResult.Unavailable -> null
        }

        breadcrumb?.let { repository.recordBreadcrumb(it) }
      }
    }
  }

  private fun stopTrackingAndSelf() {
    locationJob?.cancel()
    sensorRecorder?.stop()
    val tripId = currentTripId
    serviceScope.launch {
      if (tripId != null) {
        repository.endTrip(tripId)
      }
      stopForeground(STOP_FOREGROUND_REMOVE)
      stopSelf()
    }
  }

  private fun startForegroundWithNotification(notification: Notification) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(
        NOTIFICATION_ID,
        notification,
        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
      )
    } else {
      startForeground(NOTIFICATION_ID, notification)
    }
  }

  private fun buildForegroundNotification(title: String, text: String): Notification {
    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle(title)
      .setContentText(text)
      .setSmallIcon(android.R.drawable.ic_menu_compass)
      .setOngoing(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .build()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "AEGIS Safe Passage Tracking",
        NotificationManager.IMPORTANCE_LOW,
      ).apply {
        description = "Shows notification while AEGIS route tracking is active"
      }
      val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onDestroy() {
    sensorRecorder?.stop()
    serviceJob.cancel()
    super.onDestroy()
  }
}
