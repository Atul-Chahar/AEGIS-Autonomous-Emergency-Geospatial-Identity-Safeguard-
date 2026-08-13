package com.example.aegis.ui.zone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aegis.data.repository.CheckInRepository
import com.example.aegis.domain.model.RescuePost
import com.example.aegis.domain.model.SafetyZone
import com.example.aegis.domain.usecase.GetRescuePostUseCase
import com.example.aegis.domain.usecase.GetZoneByIdUseCase
import com.example.aegis.domain.usecase.ObserveSafetyZonesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ZoneDetailUiState(
  val zone: SafetyZone? = null,
  val rescuePost: RescuePost? = null,
  val checkInCount: Int = 0,
  val checkInNotice: String? = null,
)

class ZoneDetailViewModel(
  zoneId: String,
  getZoneById: GetZoneByIdUseCase,
  observeZones: ObserveSafetyZonesUseCase,
  getRescuePost: GetRescuePostUseCase,
  private val checkInRepository: CheckInRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ZoneDetailUiState())
  val uiState: StateFlow<ZoneDetailUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      val zone =
        observeZones()
          .map { zones -> zones.firstOrNull { it.id == zoneId } ?: getZoneById(zoneId) }
          .first()
      _uiState.value = _uiState.value.copy(zone = zone, rescuePost = getRescuePost())
    }
    viewModelScope.launch {
      checkInRepository.observeCheckInCount().collect { count ->
        _uiState.value = _uiState.value.copy(checkInCount = count)
      }
    }
  }

  /** Records a real local check-in (offline-first). Location attaches when wired. */
  fun checkIn() {
    viewModelScope.launch {
      checkInRepository.recordCheckIn(latitude = null, longitude = null)
      _uiState.value = _uiState.value.copy(checkInNotice = "Checked in locally")
    }
  }
}
