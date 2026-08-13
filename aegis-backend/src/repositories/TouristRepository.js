const db = require('../database/pool');

class TouristRepository {
  async saveIdentity(tourist) {
    // 1. PostgreSQL write if connected
    if (db.isPostgresConnected) {
      const text = `
        INSERT INTO tourists (tourist_id, id_hash, itinerary_hash, valid_from, valid_to, status, qr_payload)
        VALUES ($1, $2, $3, $4, $5, $6, $7)
        ON CONFLICT (tourist_id) DO UPDATE SET
          id_hash = EXCLUDED.id_hash,
          itinerary_hash = EXCLUDED.itinerary_hash,
          status = EXCLUDED.status
        RETURNING *;
      `;
      const values = [
        tourist.touristId,
        tourist.idHash,
        tourist.itineraryHash || null,
        tourist.validFrom || new Date().toISOString(),
        tourist.validTo || new Date(Date.now() + 7 * 86400000).toISOString(),
        tourist.status || 'ACTIVE',
        tourist.qrPayload || null
      ];
      const res = await db.query(text, values);
      return res.rows[0];
    }

    // 2. Fallback memory store
    const store = db.getStore().tourists;
    const record = {
      touristId: tourist.touristId,
      idHash: tourist.idHash,
      itineraryHash: tourist.itineraryHash || null,
      validFrom: tourist.validFrom || new Date().toISOString(),
      validTo: tourist.validTo || new Date(Date.now() + 7 * 86400000).toISOString(),
      status: tourist.status || 'ACTIVE',
      qrPayload: tourist.qrPayload || null,
      createdAt: new Date().toISOString()
    };
    store.set(tourist.touristId, record);
    return record;
  }

  async findByIdHash(idHash) {
    if (db.isPostgresConnected) {
      const res = await db.query('SELECT * FROM tourists WHERE id_hash = $1;', [idHash]);
      return res.rows[0] || null;
    }
    const store = db.getStore().tourists;
    for (const record of store.values()) {
      if (record.idHash === idHash) return record;
    }
    return null;
  }

  async findByTouristId(touristId) {
    if (db.isPostgresConnected) {
      const res = await db.query('SELECT * FROM tourists WHERE tourist_id = $1;', [touristId]);
      return res.rows[0] || null;
    }
    return db.getStore().tourists.get(touristId) || null;
  }

  async getAllTourists() {
    if (db.isPostgresConnected) {
      const res = await db.query('SELECT * FROM tourists ORDER BY created_at DESC;');
      return res.rows;
    }
    return Array.from(db.getStore().tourists.values());
  }
}

module.exports = new TouristRepository();
