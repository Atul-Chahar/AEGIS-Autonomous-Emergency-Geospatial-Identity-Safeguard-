package com.example.aegis.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.aegis.AegisApplication
import com.example.aegis.domain.usecase.DispatchSosUseCase
import com.example.aegis.domain.usecase.GetRescuePostUseCase
import com.example.aegis.domain.usecase.GetTouristIdentityUseCase
import com.example.aegis.domain.usecase.GetZoneByIdUseCase
import com.example.aegis.domain.usecase.ObserveSafetyZonesUseCase
import com.example.aegis.ui.home.HomeViewModel
import com.example.aegis.ui.id.TouristIdViewModel
import com.example.aegis.ui.zone.ZoneDetailViewModel
import com.example.aegis.ui.zones.ZonesViewModel

/** Builds screen ViewModels from the [AegisApplication] container (no DI framework needed). */
object AegisViewModelFactory : ViewModelProvider.Factory {

  private fun app(extras: CreationExtras): AegisApplication =
    extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AegisApplication

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
    val container = app(extras).container

    val viewModel: ViewModel =
      when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
          HomeViewModel(
            observeZones = ObserveSafetyZonesUseCase(container.safetyZoneRepository),
            observeIdentity = GetTouristIdentityUseCase(container.identityRepository),
            blackBoxRepository = container.blackBoxRepository,
            sanityChecker = container.locationSanityChecker,
            geofenceEngine = container.offlineGeofenceEngine,
            deviationEngine = container.routeDeviationEngine,
            checkInManager = container.safetyCheckInManager,
          )
        }

        modelClass.isAssignableFrom(ZonesViewModel::class.java) -> {
          ZonesViewModel(observeZones = ObserveSafetyZonesUseCase(container.safetyZoneRepository))
        }

        modelClass.isAssignableFrom(ZoneDetailViewModel::class.java) -> {
          // Zone id is passed via a factory function instead (see Navigation).
          error("ZoneDetailViewModel requires a zoneId — use zoneDetailViewModelFactory()")
        }

        modelClass.isAssignableFrom(TouristIdViewModel::class.java) -> {
          TouristIdViewModel(observeIdentity = GetTouristIdentityUseCase(container.identityRepository))
        }

        modelClass.isAssignableFrom(EmergencyViewModel::class.java) -> {
          EmergencyViewModel(
            dispatchSos = DispatchSosUseCase(container.emergencyRepository),
            identityRepository = container.identityRepository,
            blackBoxRepository = container.blackBoxRepository,
          )
        }

        else -> error("Unknown ViewModel class: ${modelClass.name}")
      }
    return viewModel as T
  }
}

/** Zone-detail factory: the destination carries the zone id. */
fun zoneDetailViewModelFactory(zoneId: String): ViewModelProvider.Factory =
  viewModelFactory {
    initializer {
      val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AegisApplication
      val container = app.container
      ZoneDetailViewModel(
        zoneId = zoneId,
        getZoneById = GetZoneByIdUseCase(container.safetyZoneRepository),
        observeZones = ObserveSafetyZonesUseCase(container.safetyZoneRepository),
        getRescuePost = GetRescuePostUseCase(container.safetyZoneRepository),
        checkInRepository = container.checkInRepository,
        checkInManager = container.safetyCheckInManager,
      )
    }
  }
