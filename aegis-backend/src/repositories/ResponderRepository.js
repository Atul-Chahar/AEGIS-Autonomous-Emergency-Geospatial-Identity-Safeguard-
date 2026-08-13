const db = require('../database/pool');
const postgis = require('../geospatial/postgisHelper');
const devFixtures = require('../database/seeds/dev_fixtures');

class ResponderRepository {
  async getAllResponders() {
    if (db.isPostgresConnected) {
      const res = await db.query('SELECT * FROM responder_units ORDER BY name ASC;');
      if (res.rows.length > 0) return res.rows;
    }
    return db.getStore().responderUnits || devFixtures.devResponders;
  }

  async findNearestResponders(lat, lon) {
    const all = await this.getAllResponders();
    return all.map(r => {
      const distKm = postgis.calculateDistanceKm(lat, lon, r.lat, r.lon);
      const etaMins = Math.max(3, Math.round((distKm / 35.0) * 60)); // Average 35 km/h emergency speed
      return {
        id: r.id,
        name: r.name,
        type: r.type,
        lat: r.lat,
        lon: r.lon,
        status: r.status || 'AVAILABLE',
        distanceKm: distKm.toFixed(2),
        etaMins
      };
    }).sort((a, b) => parseFloat(a.distanceKm) - parseFloat(b.distanceKm));
  }
}

module.exports = new ResponderRepository();
