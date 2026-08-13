package com.example.aegis.safety

import com.example.aegis.data.repository.CheckInRepository
import com.example.aegis.domain.model.CheckIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Locale

enum class CheckInState {
  NORMAL,
  CHECK_REQUIRED,
  USER_PROMPTED,
  SAFE_CONFIRMED,
  NO_RESPONSE,
}

data class CheckInStatus(
  val state: CheckInState,
  val lastCheckInTimestamp: Long?,
  val promptMessage: String?,
  val isGuardianNotified: Boolean = false, // Honest state flag (false when stored locally in Room)
)

class SafetyCheckInManager(
  private val checkInRepository: CheckInRepository,
) {
  private val _status = MutableStateFlow(
    CheckInStatus(
      state = CheckInState.NORMAL,
      lastCheckInTimestamp = System.currentTimeMillis(),
      promptMessage = null,
      isGuardianNotified = false,
    )
  )
  val status: StateFlow<CheckInStatus> = _status

  fun triggerCheckInRequired(reason: String) {
    _status.value = CheckInStatus(
      state = CheckInState.CHECK_REQUIRED,
      lastCheckInTimestamp = _status.value.lastCheckInTimestamp,
      promptMessage = "Safety check required: $reason",
      isGuardianNotified = false,
    )
  }

  fun promptUser() {
    val current = _status.value
    if (current.state == CheckInState.CHECK_REQUIRED || current.state == CheckInState.NORMAL) {
      _status.value = CheckInStatus(
        state = CheckInState.USER_PROMPTED,
        lastCheckInTimestamp = current.lastCheckInTimestamp,
        promptMessage = current.promptMessage ?: "Please confirm your safety.",
        isGuardianNotified = false,
      )
    }
  }

  suspend fun confirmSafe(
    latitude: Double? = null,
    longitude: Double? = null,
    zoneId: String? = null,
    note: String = "User clicked I'm Safe",
  ): CheckIn {
    val rowId = checkInRepository.recordCheckIn(latitude, longitude)
    val now = System.currentTimeMillis()
    _status.value = CheckInStatus(
      state = CheckInState.SAFE_CONFIRMED,
      lastCheckInTimestamp = now,
      promptMessage = "Safety confirmed locally at ${SimpleDateFormat("HH:mm", Locale.US).format(now)}.",
      isGuardianNotified = false, // Honest: recorded locally in Room SQLite
    )
    return CheckIn(
      id = rowId,
      touristId = "TST-LOCAL",
      latitude = latitude,
      longitude = longitude,
      timestampEpochMillis = now,
    )
  }

  fun handleTimeoutNoResponse() {
    _status.value = CheckInStatus(
      state = CheckInState.NO_RESPONSE,
      lastCheckInTimestamp = _status.value.lastCheckInTimestamp,
      promptMessage = "No response to safety prompt. Risk escalation triggered.",
      isGuardianNotified = false,
    )
  }

  fun resetToNormal() {
    _status.value = CheckInStatus(
      state = CheckInState.NORMAL,
      lastCheckInTimestamp = System.currentTimeMillis(),
      promptMessage = null,
      isGuardianNotified = false,
    )
  }
}
