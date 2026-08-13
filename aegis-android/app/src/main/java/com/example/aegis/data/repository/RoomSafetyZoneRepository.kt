package com.example.aegis.data.repository

import com.example.aegis.data.local.dao.ZoneDao
import com.example.aegis.data.local.entity.ZoneEntity
import com.example.aegis.domain.model.RescuePost
import com.example.aegis.domain.model.SafetyZone
import com.example.aegis.domain.model.ZoneStatus
import com.example.aegis.safety.OfflineGeofenceEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSafetyZoneRepository(
  private val zoneDao: ZoneDao,
  private val geofenceEngine: OfflineGeofenceEngine = OfflineGeofenceEngine(),
) : SafetyZoneRepository {

  override fun observeZones(): Flow<List<SafetyZone>> {
    return zoneDao.observeZones().map { entities ->
      if (entities.isEmpty()) {
        defaultDynamicZones
      } else {
        entities.map { it.toDomain() }
      }
    }
  }

  override suspend fun getZoneById(zoneId: String): SafetyZone? {
    val entity = zoneDao.getZoneById(zoneId)
    if (entity != null) return entity.toDomain()
    return defaultDynamicZones.firstOrNull { it.id == zoneId }
  }

  override fun getRescuePost(): RescuePost {
    return RescuePost(
      name = "Sohra Emergency & Rescue Station",
      location = "Mawsmai, Sohra, Meghalaya",
      distance = "2.4 km",
      eta = "14 min",
      rating = "4.9",
    )
  }

  fun evaluateLocationGeofence(latitude: Double, longitude: Double): SafetyZone? {
    val classification = geofenceEngine.classifyLocation(latitude, longitude)
    val polygonId = classification.matchedPolygonId ?: return null
    val baseZone = defaultDynamicZones.firstOrNull { it.id == polygonId } ?: return null
    return baseZone.copy(
      status = classification.status,
      riskScore = classification.riskScore,
    )
  }

  private fun ZoneEntity.toDomain() = SafetyZone(
    id = id,
    name = name,
    tagline = tagline,
    description = description,
    region = region,
    status = try { ZoneStatus.valueOf(status) } catch (e: Exception) { ZoneStatus.SAFE },
    riskScore = riskScore,
    dates = dates,
    duration = duration,
    elevation = elevation,
    peers = peers,
  )

  companion object {
    val defaultDynamicZones = listOf(
      SafetyZone(
        id = "cherrapunji",
        name = "Cherrapunji Ridge",
        tagline = "Geofenced high-altitude rainfall ridge.",
        description = "Live offline geofence zone evaluated locally via point-in-polygon ray-casting.",
        region = "🇮🇳 MEGHALAYA",
        status = ZoneStatus.CAUTION,
        riskScore = 62,
        dates = "12 – 20 AUG",
        duration = "7 days",
        elevation = "1,965 m",
        peers = 2,
      ),
      SafetyZone(
        id = "roots",
        name = "Living Root Bridges",
        tagline = "Protected eco-corridor.",
        description = "Live offline safe geofence sanctuary.",
        region = "🇮🇳 MEGHALAYA",
        status = ZoneStatus.SAFE,
        riskScore = 18,
        dates = "12 – 20 AUG",
        duration = "3 days",
        elevation = "740 m",
        peers = 4,
      ),
      SafetyZone(
        id = "nohkalikai",
        name = "Nohkalikai Cliff",
        tagline = "High-risk plunge cliff boundary.",
        description = "Live offline high-risk hazard geofence.",
        region = "🇮🇳 MEGHALAYA",
        status = ZoneStatus.HIGH_RISK,
        riskScore = 78,
        dates = "12 – 20 AUG",
        duration = "2 days",
        elevation = "1,120 m",
        peers = 0,
      ),
    )
  }
}
