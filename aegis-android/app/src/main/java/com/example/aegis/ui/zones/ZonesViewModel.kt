package com.example.aegis.ui.zones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aegis.domain.model.SafetyZone
import com.example.aegis.domain.model.ZoneStatus
import com.example.aegis.domain.usecase.ObserveSafetyZonesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ZonesViewModel(
  observeZones: ObserveSafetyZonesUseCase,
) : ViewModel() {

  private val zones: StateFlow<List<SafetyZone>> =
    observeZones().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  private val _selectedFilter = MutableStateFlow<ZoneStatus?>(null)
  val selectedFilter: StateFlow<ZoneStatus?> = _selectedFilter.asStateFlow()

  val filteredZones: StateFlow<List<SafetyZone>> =
    combine(zones, selectedFilter) { all, filter ->
      if (filter == null) all else all.filter { it.status == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  /** The expanded "active" card — the featured zone (first of the list). */
  val activeZone: StateFlow<SafetyZone?> =
    zones.map { it.firstOrNull() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  fun setFilter(status: ZoneStatus?) {
    _selectedFilter.value = status
  }
}
