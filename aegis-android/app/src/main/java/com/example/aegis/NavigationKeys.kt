package com.example.aegis

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Home : NavKey

@Serializable data object Zones : NavKey

@Serializable data object Map : NavKey

@Serializable data object TouristId : NavKey

@Serializable data object Activity : NavKey

@Serializable data object TripSetup : NavKey

@Serializable data object SafetyCenter : NavKey

@Serializable data object JourneyBlackBox : NavKey

@Serializable data object IncidentCheck : NavKey

@Serializable data class ZoneDetail(val zoneId: String) : NavKey
