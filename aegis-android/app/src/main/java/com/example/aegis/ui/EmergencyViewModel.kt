package com.example.aegis.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aegis.data.repository.BlackBoxRepository
import com.example.aegis.data.repository.IdentityRepository
import com.example.aegis.domain.model.SosDispatchResult
import com.example.aegis.domain.model.SosRequest
import com.example.aegis.domain.usecase.DispatchSosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Locale

data class EmergencyUiState(
  val overlayVisible: Boolean = false,
  val dispatching: Boolean = false,
  val dispatchResult: SosDispatchResult? = null,
  val payloadPreview: String? = null,
  val statusMessage: String? = null,
  val error: String? = null,
)

/**
 * Hosts the emergency SOS overlay for every screen.
 * Uses real latest breadcrumbs and battery percentages from BlackBox.
 * Displays honest delivery states — never fake success.
 */
class EmergencyViewModel(
  private val dispatchSos: DispatchSosUseCase,
  private val identityRepository: IdentityRepository,
  private val blackBoxRepository: BlackBoxRepository? = null,
) : ViewModel() {

  private val _uiState = MutableStateFlow(EmergencyUiState())
  val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

  fun openOverlay() {
    _uiState.value = EmergencyUiState(overlayVisible = true)
    viewModelScope.launch {
      val identity = identityRepository.observeIdentity().first()
      val latestBreadcrumb = blackBoxRepository?.observeLatestBreadcrumb()?.firstOrNull()

      val latStr = latestBreadcrumb?.latitude?.let { String.format(Locale.US, "%.4f", it) } ?: "No Fix"
      val lonStr = latestBreadcrumb?.longitude?.let { String.format(Locale.US, "%.4f", it) } ?: "No Fix"
      val batStr = latestBreadcrumb?.batteryPercent?.let { "$it%" } ?: "—"

      _uiState.value = _uiState.value.copy(
        payloadPreview = "SOS:${identity.touristId} | lat:$latStr | lon:$lonStr | battery:$batStr",
        statusMessage = "Ready to dispatch via Outbox",
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
      val latestBreadcrumb = blackBoxRepository?.observeLatestBreadcrumb()?.firstOrNull()

      val effectiveLat = latitude ?: latestBreadcrumb?.latitude
      val effectiveLon = longitude ?: latestBreadcrumb?.longitude
      val effectiveBat = latestBreadcrumb?.batteryPercent

      val request = SosRequest(
        touristId = identity.touristId,
        zoneId = zoneId,
        latitude = effectiveLat,
        longitude = effectiveLon,
        batteryPct = effectiveBat,
        timestampEpochMillis = System.currentTimeMillis(),
      )

      _uiState.value = _uiState.value.copy(
        dispatching = true,
        dispatchResult = null,
        statusMessage = "Writing to Outbox & Sending…",
        error = null,
      )

      val result = dispatchSos(request)
      val statusMsg = when (result) {
        is SosDispatchResult.Sent -> "Delivered via ${result.transport} (Ack: ${result.ackId})"
        is SosDispatchResult.PendingSmsFallback -> "Saved to Outbox · Waiting for connectivity — SMS handoff ready"
        is SosDispatchResult.Failed -> "Failed: ${result.reason}"
        is SosDispatchResult.NotAvailable -> "Not Available: ${result.reason}"
        SosDispatchResult.Dispatched -> "Dispatched via Outbox"
      }

      _uiState.value = _uiState.value.copy(
        dispatching = false,
        dispatchResult = result,
        statusMessage = statusMsg,
        error = (result as? SosDispatchResult.NotAvailable)?.reason ?: (result as? SosDispatchResult.Failed)?.reason,
      )
    }
  }
}
