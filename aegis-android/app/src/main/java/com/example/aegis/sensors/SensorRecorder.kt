package com.example.aegis.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class SensorRecorder(
  private val context: Context,
  private val onHighImpactDetected: (eventType: String, confidence: Float, rawDataJson: String) -> Unit,
) : SensorEventListener {

  private var sensorManager: SensorManager? = null
  private var accelerometer: Sensor? = null

  fun start() {
    try {
      sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
      accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
      accelerometer?.let {
        sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
      }
    } catch (e: Exception) {
      // Sensor unavailable
    }
  }

  fun stop() {
    try {
      sensorManager?.unregisterListener(this)
    } catch (e: Exception) {
      // Ignore cleanup error
    }
  }

  override fun onSensorChanged(event: SensorEvent?) {
    if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

    val x = event.values[0]
    val y = event.values[1]
    val z = event.values[2]

    val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
    // Normal gravity is ~9.8 m/s^2. High impact (> 25 m/s^2)
    if (magnitude > 25.0f) {
      val rawJson = """{"x":$x,"y":$y,"z":$z,"magnitude":$magnitude}"""
      onHighImpactDetected("IMPACT_DETECTED", (magnitude / 50.0f).coerceAtMost(1.0f), rawJson)
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
