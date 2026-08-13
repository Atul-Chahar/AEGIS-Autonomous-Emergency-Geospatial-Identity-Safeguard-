package com.example.aegis.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aegis.data.repository.BlackBoxRepository
import com.example.aegis.domain.model.Breadcrumb
import com.example.aegis.domain.model.SafetyZone
import com.example.aegis.domain.model.TouristIdentity
import com.example.aegis.domain.model.Trip
import com.example.aegis.domain.usecase.GetTouristIdentityUseCase
import com.example.aegis.domain.usecase.ObserveSafetyZonesUseCase
import com.example.aegis.service.TripTrackingService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

class HomeViewModel(
  observeZones: ObserveSafetyZonesUseCase,
  observeIdentity: GetTouristIdentityUseCase,
  private val blackBoxRepository: BlackBoxRepository,
) : ViewModel() {

  val zones: StateFlow<List<SafetyZone>> =
    observeZones().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  val featuredZone: StateFlow<SafetyZone?> =
    zones.map { it.firstOrNull() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  val identity: StateFlow<TouristIdentity?> =
    observeIdentity()
      .map<TouristIdentity, TouristIdentity?> { it }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  val activeTrip: StateFlow<Trip?> =
    blackBoxRepository.observeActiveTrip()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  val isTrackingActive: StateFlow<Boolean> =
    activeTrip.map { it != null }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

  val latestBreadcrumb: StateFlow<Breadcrumb?> =
    blackBoxRepository.observeLatestBreadcrumb()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  val locationText: StateFlow<String> =
    latestBreadcrumb.map { breadcrumb ->
      if (breadcrumb != null) {
        val latStr = String.format(Locale.US, "%.4f° N", breadcrumb.latitude)
        val lonStr = String.format(Locale.US, "%.4f° E", breadcrumb.longitude)
        "$latStr, $lonStr (${breadcrumb.batteryPercent}% batt)"
      } else {
        "Location unavailable"
      }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Location unavailable")

  val touristName: String
    get() = identity.value?.displayName ?: "Tourist"

  fun startRoute(context: Context, plannedRouteId: String? = null) {
    val touristId = identity.value?.touristId ?: "TST-DEFAULT"
    TripTrackingService.start(context, touristId, plannedRouteId)
  }

  fun stopRoute(context: Context) {
    TripTrackingService.stop(context)
  }
}
