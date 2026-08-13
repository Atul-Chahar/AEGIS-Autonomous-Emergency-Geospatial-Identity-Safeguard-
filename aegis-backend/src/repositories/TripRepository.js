const db = require('../database/pool');

class TripRepository {
  async getActiveTrips() {
    if (db.isPostgresConnected) {
      const res = await db.query("SELECT * FROM trips WHERE status = 'ACTIVE' ORDER BY started_at DESC;");
      if (res.rows.length > 0) return res.rows;
    }
    const storeTrips = db.getStore().trips;
    if (storeTrips && storeTrips.size > 0) {
      return Array.from(storeTrips.values()).filter(t => t.status === 'ACTIVE');
    }
    // Default active trips fixture
    return [
      { id: 'TRIP-2026-MEGHALAYA', touristId: 'TST-8F29X4', startedAt: new Date().toISOString(), status: 'ACTIVE', plannedRouteId: 'cherrapunji-ridge' },
      { id: 'TRIP-2026-ROOTS', touristId: 'TST-3391A', startedAt: new Date().toISOString(), status: 'ACTIVE', plannedRouteId: 'living-roots' }
    ];
  }

  async getTripBreadcrumbs(tripId) {
    if (db.isPostgresConnected) {
      const res = await db.query('SELECT * FROM breadcrumbs WHERE trip_id = $1 ORDER BY timestamp ASC;', [tripId]);
      if (res.rows.length > 0) return res.rows;
    }
    const breadcrumbs = db.getStore().breadcrumbs || [];
    const matched = breadcrumbs.filter(b => b.tripId === tripId);
    if (matched.length > 0) return matched;

    // Default BlackBox trajectory fixtures for map polyline layer
    return [
      { id: 1, tripId, lat: 25.138, lon: 91.258, accuracyMeters: 5.0, batteryPercent: 95, timestamp: new Date(Date.now() - 3600000).toISOString() },
      { id: 2, tripId, lat: 25.141, lon: 91.261, accuracyMeters: 4.5, batteryPercent: 90, timestamp: new Date(Date.now() - 1800000).toISOString() },
      { id: 3, tripId, lat: 25.145, lon: 91.265, accuracyMeters: 6.0, batteryPercent: 85, timestamp: new Date().toISOString() }
    ];
  }
}

module.exports = new TripRepository();
