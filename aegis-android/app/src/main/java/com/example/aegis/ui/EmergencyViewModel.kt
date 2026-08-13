package com.example.aegis.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aegis.data.repository.IdentityRepository
import com.example.aegis.domain.model.SosDispatchResult
import com.example.aegis.domain.model.SosRequest
import com.example.aegis.domain.usecase.DispatchSosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class EmergencyUiState(
  val overlayVisible: Boolean = false,
  val dispatching: Boolean = false,
  val dispatchResult: SosDispatchResult? = null,
  val payloadPreview: String? = null,
  val error: String? = null,
)

/**
 * Hosts the emergency SOS overlay for every screen (the raised nav trigger and
 * the detail-screen button share this state). Dispatch goes through the real
 * repository contract — the result shown is whatever the transport actually
 * returned, never a fabricated success.
 */
class EmergencyViewModel(
  private val dispatchSos: DispatchSosUseCase,
  private val identityRepository: IdentityRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(EmergencyUiState())
  val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

  fun openOverlay() {
    _uiState.value = EmergencyUiState(overlayVisible = true)
    viewModelScope.launch {
      val identity = identityRepository.observeIdentity().first()
      _uiState.value =
        _uiState.value.copy(
          payloadPreview = "SOS:${identity.touristId} | lat — | lon — | battery —",
        )
    }
  }

  fun dismissOverlay() {
    _uiState.value = EmergencyUiState()
  }

  fun dispatch(zoneId: String? = null, latitude: Double? = null, longitude: Double? = null) {
    if (_uiState.value.dispatching) return
    viewModelScope.launch {
      val identity = identityRepository.observeIdentity().first()
      val request =
        SosRequest(
          touristId = identity.touristId,
          zoneId = zoneId,
          latitude = latitude,
          longitude = longitude,
          batteryPct = null,
          timestampEpochMillis = System.currentTimeMillis(),
        )
      _uiState.value =
        _uiState.value.copy(
          dispatching = true,
          dispatchResult = null,
          error = null,
          payloadPreview =
            "SOS:${identity.touristId} | zone:${zoneId ?: "—"} | " +
              "lat:${latitude?.toString() ?: "—"} | lon:${longitude?.toString() ?: "—"}",
        )
      val result = dispatchSos(request)
      _uiState.value =
        _uiState.value.copy(
          dispatching = false,
          dispatchResult = result,
          error = (result as? SosDispatchResult.NotAvailable)?.reason,
        )
    }
  }
}
