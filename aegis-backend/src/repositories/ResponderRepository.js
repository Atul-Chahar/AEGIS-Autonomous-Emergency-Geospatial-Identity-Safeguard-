const db = require('../database/pool');
const rescueEngine = require('../geospatial/RescueabilityEngine');

class ResponderRepository {
  async getAllResponders() {
    if (db.isPostgresConnected) {
      const res = await db.query('SELECT * FROM responder_units ORDER BY name ASC;');
      if (res.rows.length > 0) return res.rows;
    }
    return db.getStore().responderUnits || [];
  }

  async matchResponders(lat, lon, requiredCapabilities = ['MEDICAL', 'ROPE']) {
    const all = await this.getAllResponders();

    const evaluation = rescueEngine.evaluateRescueability(
      { lat, lon, requiredCapabilities },
      all,
      []
    );

    return evaluation;
  }
}

module.exports = new ResponderRepository();
