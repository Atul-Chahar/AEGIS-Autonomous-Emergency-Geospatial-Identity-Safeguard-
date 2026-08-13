const db = require('../database/pool');
const postgis = require('../geospatial/postgisHelper');

class HazardRepository {
  async createHazardReport(report) {
    const id = `HAZ-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
    const lat = parseFloat(report.lat);
    const lon = parseFloat(report.lon);

    // 1. PostGIS or memory spatial clustering check: find reports within 500m (0.5 km)
    let nearbyCount = 0;
    if (db.isPostgresConnected) {
      const clusterRes = await db.query(
        `SELECT COUNT(*) FROM hazard_reports 
         WHERE hazard_type = $1 
         AND ST_DWithin(location, ST_SetSRID(ST_MakePoint($2, $3), 4326), 0.005);`,
        [report.hazardType, lon, lat]
      );
      nearbyCount = parseInt(clusterRes.rows[0].count) || 0;
    } else {
      const hazards = db.getStore().hazardReports;
      nearbyCount = hazards.filter(h => {
        if (h.hazardType !== report.hazardType) return false;
        const distKm = postgis.calculateDistanceKm(lat, lon, h.lat, h.lon);
        return distKm <= 0.5; // 500m
      }).length;
    }

    const initialStatus = (nearbyCount >= 2) ? 'VERIFIED' : 'UNVERIFIED';

    if (db.isPostgresConnected) {
      const text = `
        INSERT INTO hazard_reports (id, reporter_id, hazard_type, lat, lon, location, description, status)
        VALUES ($1, $2, $3, $4, $5, ST_SetSRID(ST_MakePoint($5, $4), 4326), $6, $7)
        RETURNING *;
      `;
      const values = [
        id,
        report.reporterId || 'ANONYMOUS',
        report.hazardType,
        lat,
        lon,
        report.description || '',
        initialStatus
      ];
      const res = await db.query(text, values);
      return { hazard: res.rows[0], nearbyCount, isVerified: initialStatus === 'VERIFIED' };
    }

    const record = {
      id,
      reporterId: report.reporterId || 'ANONYMOUS',
      hazardType: report.hazardType,
      lat,
      lon,
      description: report.description || '',
      status: initialStatus,
      timestamp: new Date().toISOString()
    };
    db.getStore().hazardReports.push(record);
    return { hazard: record, nearbyCount, isVerified: initialStatus === 'VERIFIED' };
  }

  async getAllHazards() {
    if (db.isPostgresConnected) {
      const res = await db.query('SELECT * FROM hazard_reports ORDER BY timestamp DESC;');
      return res.rows;
    }
    return db.getStore().hazardReports;
  }
}

module.exports = new HazardRepository();
