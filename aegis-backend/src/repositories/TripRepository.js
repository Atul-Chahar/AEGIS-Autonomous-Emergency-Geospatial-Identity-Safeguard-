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
    return [];
  }

  async getTripBreadcrumbs(tripId) {
    if (db.isPostgresConnected) {
      const res = await db.query('SELECT * FROM breadcrumbs WHERE trip_id = $1 ORDER BY timestamp ASC;', [tripId]);
      if (res.rows.length > 0) return res.rows;
    }
    const breadcrumbs = db.getStore().breadcrumbs || [];
    const matched = breadcrumbs.filter(b => b.tripId === tripId);
    return matched;
  }
}

module.exports = new TripRepository();
