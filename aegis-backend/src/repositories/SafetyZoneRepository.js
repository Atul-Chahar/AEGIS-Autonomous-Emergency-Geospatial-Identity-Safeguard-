const db = require('../database/pool');
const postgis = require('../geospatial/postgisHelper');

class SafetyZoneRepository {
  async getAllZones() {
    if (db.isPostgresConnected) {
      const res = await db.query('SELECT id, name, risk_level AS "riskLevel", color, coordinates_json FROM safety_zones;');
      if (res.rows.length > 0) return res.rows;
    }
    return db.getStore().safetyZones;
  }

  async classifyPoint(lat, lon) {
    const zones = await this.getAllZones();
    for (const zone of zones) {
      const coords = zone.coordinates || zone.coordinates_json;
      if (coords && postgis.isPointInPolygon(lat, lon, coords)) {
        return {
          matchedZoneId: zone.id,
          name: zone.name,
          riskLevel: zone.riskLevel,
          color: zone.color
        };
      }
    }
    return {
      matchedZoneId: null,
      name: 'Uncharted Region',
      riskLevel: 'UNKNOWN',
      color: '#6B7280'
    };
  }
}

module.exports = new SafetyZoneRepository();
