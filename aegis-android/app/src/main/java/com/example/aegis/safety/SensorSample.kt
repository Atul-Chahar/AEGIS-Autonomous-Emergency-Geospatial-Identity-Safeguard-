package com.example.aegis.safety

data class Vector3(val x: Float, val y: Float, val z: Float) {
  fun magnitude(): Float = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
}

data class SensorSample(
  val timestamp: Long,
  val linearAcceleration: Vector3? = null,
  val accelerometer: Vector3? = null,
  val gyroscope: Vector3? = null,
  val rotationVector: FloatArray? = null,
  val stepCountDelta: Int = 0,
  val gpsSpeedMps: Float? = null,
  val gpsBearingDegrees: Float? = null,
  val gpsAccuracyMeters: Float? = null,
  val activityMode: ActivityMode = ActivityMode.UNKNOWN,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as SensorSample

    if (timestamp != other.timestamp) return false
    if (linearAcceleration != other.linearAcceleration) return false
    if (accelerometer != other.accelerometer) return false
    if (gyroscope != other.gyroscope) return false
    if (rotationVector != null) {
      if (other.rotationVector == null) return false
      if (!rotationVector.contentEquals(other.rotationVector)) return false
    } else if (other.rotationVector != null) return false
    if (stepCountDelta != other.stepCountDelta) return false
    if (gpsSpeedMps != other.gpsSpeedMps) return false
    if (gpsBearingDegrees != other.gpsBearingDegrees) return false
    if (gpsAccuracyMeters != other.gpsAccuracyMeters) return false
    if (activityMode != other.activityMode) return false

    return true
  }

  override fun hashCode(): Int {
    var result = timestamp.hashCode()
    result = 31 * result + (linearAcceleration?.hashCode() ?: 0)
    result = 31 * result + (accelerometer?.hashCode() ?: 0)
    result = 31 * result + (gyroscope?.hashCode() ?: 0)
    result = 31 * result + (rotationVector?.contentHashCode() ?: 0)
    result = 31 * result + stepCountDelta
    result = 31 * result + (gpsSpeedMps?.hashCode() ?: 0)
    result = 31 * result + (gpsBearingDegrees?.hashCode() ?: 0)
    result = 31 * result + (gpsAccuracyMeters?.hashCode() ?: 0)
    result = 31 * result + activityMode.hashCode()
    return result
  }
}
