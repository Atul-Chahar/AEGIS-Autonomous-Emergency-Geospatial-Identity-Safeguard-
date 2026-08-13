package com.example.aegis.sensors

import kotlinx.coroutines.flow.Flow

/** Source of periodic physical-activity updates (Google Play Activity Recognition). */
interface ActivityRecognitionProvider {
  fun observeActivity(): Flow<DetectedActivity>
}
