package com.example.aegis.sensors

/** A detected physical activity with confidence (feeds the inactivity monitor). */
data class DetectedActivity(
  val type: Int,
  val confidence: Int,
) {
  val isMoving: Boolean
    get() = type == TYPE_WALKING || type == TYPE_RUNNING || type == TYPE_ON_BICYCLE || type == TYPE_IN_VEHICLE

  companion object {
    const val TYPE_WALKING = 7
    const val TYPE_RUNNING = 8
    const val TYPE_ON_BICYCLE = 1
    const val TYPE_IN_VEHICLE = 0
  }
}
