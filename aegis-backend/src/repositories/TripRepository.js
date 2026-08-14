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

  /**
   * Creates or upserts a trip from the Android BlackBox sync.
   * Accepts both `tripId` (Android/JS) and `id` (Postgres) keys.
   */
  async saveTrip(trip) {
    const id = trip.tripId || trip.id || `TRIP-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
    const startedAt = trip.startedAt || trip.started_at || Date.now();
    const startedIso = new Date(startedAt).toISOString();

    if (db.isPostgresConnected) {
      const text = `
        INSERT INTO trips (id, tourist_id, started_at, ended_at, status, planned_route_id)
        VALUES ($1, $2, $3, $4, $5, $6)
        ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status, planned_route_id = EXCLUDED.planned_route_id
        RETURNING *;
      `;
      const values = [
        id,
        trip.touristId || 'UNKNOWN',
        startedIso,
        trip.endedAt ? new Date(trip.endedAt).toISOString() : null,
        trip.status || 'ACTIVE',
        trip.plannedRouteId || null
      ];
      const res = await db.query(text, values);
      return this.normalizeTrip(res.rows[0]);
    }

    const record = {
      id,
      tripId: id,
      touristId: trip.touristId || 'UNKNOWN',
      startedAt: startedIso,
      started_at: startedIso,
      endedAt: trip.endedAt ? new Date(trip.endedAt).toISOString() : null,
      ended_at: trip.endedAt ? new Date(trip.endedAt).toISOString() : null,
      status: trip.status || 'ACTIVE',
      plannedRouteId: trip.plannedRouteId || null,
      planned_route_id: trip.plannedRouteId || null,
      updatedAt: startedIso,
      updated_at: startedIso
    };
    db.getStore().trips.set(id, record);
    return record;
  }

  /** Appends a breadcrumb (Android BlackBox sync). Idempotent on breadcrumbId. */
  async saveBreadcrumb(breadcrumb) {
    const id = breadcrumb.breadcrumbId || `BC-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
    const timestampIso = breadcrumb.timestamp ? new Date(breadcrumb.timestamp).toISOString() : new Date().toISOString();

    if (db.isPostgresConnected) {
      const text = `
        INSERT INTO breadcrumbs (trip_id, tourist_id, lat, lon, location, horizontal_accuracy, battery_percent, timestamp)
        VALUES ($1, $2, $3, $4, ST_SetSRID(ST_MakePoint($4, $3), 4326), $5, $6, $7)
        RETURNING *;
      `;
      const values = [
        breadcrumb.tripId,
        breadcrumb.touristId || 'UNKNOWN',
        parseFloat(breadcrumb.lat),
        parseFloat(breadcrumb.lon),
        breadcrumb.accuracyMeters != null ? parseFloat(breadcrumb.accuracyMeters) : null,
        breadcrumb.batteryPercent != null ? parseInt(breadcrumb.batteryPercent) : null,
        timestampIso
      ];
      const res = await db.query(text, values);
      return this.normalizeBreadcrumb(res.rows[0]);
    }

    const store = db.getStore();
    const existing = store.breadcrumbs.find(b => b.breadcrumbId === id);
    const record = {
      breadcrumbId: id,
      id,
      tripId: breadcrumb.tripId,
      touristId: breadcrumb.touristId || 'UNKNOWN',
      lat: parseFloat(breadcrumb.lat),
      lon: parseFloat(breadcrumb.lon),
      accuracyMeters: breadcrumb.accuracyMeters != null ? parseFloat(breadcrumb.accuracyMeters) : null,
      horizontal_accuracy: breadcrumb.accuracyMeters != null ? parseFloat(breadcrumb.accuracyMeters) : null,
      batteryPercent: breadcrumb.batteryPercent != null ? parseInt(breadcrumb.batteryPercent) : null,
      battery_pct: breadcrumb.batteryPercent != null ? parseInt(breadcrumb.batteryPercent) : null,
      activityMode: breadcrumb.activityMode || 'STILL',
      timestamp: timestampIso
    };
    if (existing) {
      Object.assign(existing, record);
      return existing;
    }
    store.breadcrumbs.push(record);
    return record;
  }

  async getTripBreadcrumbs(tripId) {
    if (db.isPostgresConnected) {
      const res = await db.query('SELECT * FROM breadcrumbs WHERE trip_id = $1 ORDER BY timestamp ASC;', [tripId]);
      if (res.rows.length > 0) return res.rows.map(row => this.normalizeBreadcrumb(row));
    }
    const breadcrumbs = db.getStore().breadcrumbs || [];
    const matched = breadcrumbs.filter(b => b.tripId === tripId);
    return matched.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
  }

  normalizeTrip(row) {
    if (!row) return row;
    return {
      ...row,
      id: row.id,
      tripId: row.id,
      touristId: row.tourist_id || row.touristId,
      startedAt: row.started_at || row.startedAt,
      plannedRouteId: row.planned_route_id || row.plannedRouteId,
      updatedAt: row.updated_at || row.started_at
    };
  }

  normalizeBreadcrumb(row) {
    if (!row) return row;
    return {
      ...row,
      breadcrumbId: row.breadcrumb_id || row.id,
      tripId: row.trip_id || row.tripId,
      touristId: row.tourist_id || row.touristId,
      accuracyMeters: row.horizontal_accuracy != null ? Number(row.horizontal_accuracy) : null,
      batteryPercent: row.battery_percent != null ? Number(row.battery_percent) : null
    };
  }
}

module.exports = new TripRepository();
