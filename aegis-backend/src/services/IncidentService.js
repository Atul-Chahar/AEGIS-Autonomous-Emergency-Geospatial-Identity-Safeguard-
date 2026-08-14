const incidentRepository = require('../repositories/IncidentRepository');
const touristRepository = require('../repositories/TouristRepository');

class IncidentService {
  async processSosDispatch({ packetId, touristId, channel, rawSmsPayload, lat, lon, batteryPct }) {
    // Default fallback coordinates if missing in body
    const finalLat = lat !== undefined ? parseFloat(lat) : 25.141;
    const finalLon = lon !== undefined ? parseFloat(lon) : 91.261;

    // Link the canonical keccak256 commitment when the tourist has a
    // registered voucher — the dashboard then shows a real ID hash instead
    // of "No Hash Linked" for a known tourist.
    let idHash = null;
    if (touristId) {
      const tourist = touristRepository.findByTouristId
        ? await touristRepository.findByTouristId(touristId)
        : null;
      idHash = tourist?.idHash || tourist?.id_hash || null;
    }

    const { incident, isDuplicate } = await incidentRepository.saveIncident({
      packetId: packetId || null,
      touristId: touristId || 'TST-EMERGENCY',
      idHash,
      lat: finalLat,
      lon: finalLon,
      batteryPct: batteryPct !== undefined ? parseInt(batteryPct) : 85,
      channel: channel || 'HTTPS',
      status: 'OPEN',
      riskScore: 100
    });

    // Spread the full record so the live WS payload carries the geospatial
    // details the authority dashboard needs to pin the emergency on the map.
    return {
      ...incident,
      success: true,
      incidentId: incident.id,
      packetId: incident.packetId || packetId,
      isDuplicate,
      status: incident.status,
      timestamp: incident.timestamp
    };
  }

  async updateStatus(incidentId, status) {
    return incidentRepository.updateIncidentStatus(incidentId, status);
  }

  async listAllIncidents() {
    return incidentRepository.getAllIncidents();
  }
}

module.exports = new IncidentService();
