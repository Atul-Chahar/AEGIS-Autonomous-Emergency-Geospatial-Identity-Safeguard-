package com.example.aegis.data.repository

import com.example.aegis.domain.model.RescuePost
import com.example.aegis.domain.model.SafetyZone
import kotlinx.coroutines.flow.Flow

/**
 * Source of safety zones. Implemented today by the demo/preview repository;
 * a Room-backed implementation (see [com.example.aegis.data.local]) lands next.
 */
interface SafetyZoneRepository {
  /** Zones ordered with the featured/active zone first. */
  fun observeZones(): Flow<List<SafetyZone>>

  suspend fun getZoneById(zoneId: String): SafetyZone?

  fun getRescuePost(): RescuePost
}
