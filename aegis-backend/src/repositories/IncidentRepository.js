const db = require('../database/pool');
const postgis = require('../geospatial/postgisHelper');

class IncidentRepository {
  async findByPacketId(packetId) {
    if (!packetId) return null;
    if (db.isPostgresConnected) {
      const res = await db.query('SELECT * FROM incidents WHERE packet_id = $1;', [packetId]);
      return res.rows[0] || null;
    }
    const list = db.getStore().incidents;
    return list.find(inc => inc.packetId === packetId) || null;
  }

  async saveIncident(incident) {
    // 1. Idempotency Check
    if (incident.packetId) {
      const existing = await this.findByPacketId(incident.packetId);
      if (existing) {
        return { incident: existing, isDuplicate: true };
      }
    }

    const id = incident.id || `INC-${Date.now()}-${Math.floor(Math.random() * 1000)}`;

    if (db.isPostgresConnected) {
      const text = `
        INSERT INTO incidents (id, packet_id, tourist_id, id_hash, lat, lon, location, battery_pct, channel, status, risk_score)
        VALUES ($1, $2, $3, $4, $5, $6, ST_SetSRID(ST_MakePoint($6, $5), 4326), $7, $8, $9, $10)
        RETURNING *;
      `;
      const values = [
        id,
        incident.packetId || null,
        incident.touristId || 'UNKNOWN',
        incident.idHash || null,
        parseFloat(incident.lat),
        parseFloat(incident.lon),
        incident.batteryPct || 100,
        incident.channel || 'HTTPS',
        incident.status || 'OPEN',
        incident.riskScore || 100
      ];
      const res = await db.query(text, values);
      const saved = res.rows[0];

      // Audit event log
      await this.logAuditEvent(id, 'INCIDENT_CREATED', { packetId: incident.packetId, channel: incident.channel });

      return { incident: saved, isDuplicate: false };
    }

    // Fallback in-memory
    const record = {
      id,
      packetId: incident.packetId || null,
      touristId: incident.touristId || 'UNKNOWN',
      idHash: incident.idHash || null,
      lat: parseFloat(incident.lat),
      lon: parseFloat(incident.lon),
      batteryPct: incident.batteryPct || 100,
      channel: incident.channel || 'HTTPS',
      status: incident.status || 'OPEN',
      riskScore: incident.riskScore || 100,
      timestamp: new Date().toISOString()
    };
    db.getStore().incidents.push(record);
    db.getStore().incidentEvents.push({
      id: Date.now(),
      incidentId: id,
      eventType: 'INCIDENT_CREATED',
      payload: { packetId: incident.packetId, channel: incident.channel },
      timestamp: new Date().toISOString()
    });

    return { incident: record, isDuplicate: false };
  }

  async logAuditEvent(incidentId, eventType, payload = {}) {
    if (db.isPostgresConnected) {
      await db.query(
        'INSERT INTO incident_events (incident_id, event_type, payload) VALUES ($1, $2, $3);',
        [incidentId, eventType, JSON.stringify(payload)]
      );
    } else {
      db.getStore().incidentEvents.push({
        id: Date.now(),
        incidentId,
        eventType,
        payload,
        timestamp: new Date().toISOString()
      });
    }
  }

  async updateIncidentStatus(incidentId, newStatus) {
    const validStatuses = ['OPEN', 'ACKNOWLEDGED', 'TEAM_DISPATCHED', 'SEARCHING', 'LOCATED', 'RESOLVED'];
    if (!validStatuses.includes(newStatus)) {
      throw new Error(`INVALID_STATUS: Allowed statuses are ${validStatuses.join(', ')}`);
    }

    let updated = null;
    if (db.isPostgresConnected) {
      const res = await db.query('UPDATE incidents SET status = $1 WHERE id = $2 RETURNING *;', [newStatus, incidentId]);
      updated = res.rows[0];
    } else {
      const incident = db.getStore().incidents.find(i => i.id === incidentId);
      if (incident) {
        incident.status = newStatus;
        updated = incident;
      }
    }

    if (updated) {
      await this.logAuditEvent(incidentId, 'STATUS_CHANGED', { newStatus });
    }

    return updated;
  }

  async getAllIncidents() {
    if (db.isPostgresConnected) {
      const res = await db.query('SELECT * FROM incidents ORDER BY timestamp DESC;');
      return res.rows;
    }
    return db.getStore().incidents;
  }
}

module.exports = new IncidentRepository();
