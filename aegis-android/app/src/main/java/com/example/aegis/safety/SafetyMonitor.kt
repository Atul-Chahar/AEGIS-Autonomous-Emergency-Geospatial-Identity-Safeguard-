package com.example.aegis.safety

import kotlinx.coroutines.flow.Flow

/** Current evaluated safety state for the active zone. */
data class SafetyState(
  val zoneId: String?,
  val band: RiskBand,
  val lastEvaluatedAtEpochMillis: Long,
)

/**
 * Contract for the on-device safety monitor. The real implementation
 * (location + activity + risk formula) ships in the next stage together with
 * the sensor wiring — nothing here fakes a live status.
 */
interface SafetyMonitor {
  fun observeSafetyState(): Flow<SafetyState>
}
