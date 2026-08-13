package com.example.aegis

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Home : NavKey

@Serializable data object Zones : NavKey

@Serializable data object TouristId : NavKey

/** Placeholder destination for the Activity nav slot (check-in log). */
@Serializable data object Activity : NavKey

@Serializable data class ZoneDetail(val zoneId: String) : NavKey
