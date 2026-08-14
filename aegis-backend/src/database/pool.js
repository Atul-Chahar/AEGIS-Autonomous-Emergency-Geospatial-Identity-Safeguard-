const { Pool } = require('pg');
const env = require('../config/env');
const fixtures = require('./seeds/dev_fixtures');

class AegisDbPool {
  constructor() {
    this.isPostgresConnected = false;
    this.pgPool = null;

    // In-Memory Database Fallback seeded with comprehensive Meghalaya fixtures
    const touristMap = new Map();
    (fixtures.devTourists || []).forEach(t => touristMap.set(t.touristId, t));

    const tripMap = new Map();
    (fixtures.devTrips || []).forEach(t => tripMap.set(t.id, t));

    const allBreadcrumbs = [];
    Object.values(fixtures.devBreadcrumbs || {}).forEach(trail => {
      if (Array.isArray(trail)) allBreadcrumbs.push(...trail);
    });

    this.memoryStore = {
      tourists: touristMap,
      trips: tripMap,
      breadcrumbs: allBreadcrumbs,
      incidents: [...(fixtures.devIncidents || [])],
      incidentEvents: [],
      checkIns: [],
      hazardReports: [...(fixtures.devHazards || [])],
      safetyZones: [...(fixtures.devGeofences || [])],
      responderUnits: [...(fixtures.devResponders || [])],
      responderCapabilities: [],
      relayReceipts: new Map(),
    };
  }

  async initialize() {
    if (env.databaseUrl || env.pgHost) {
      try {
        this.pgPool = new Pool({
          connectionString: env.databaseUrl,
          host: env.pgHost,
          port: env.pgPort,
          database: env.pgDatabase,
          user: env.pgUser,
          password: env.pgPassword,
          max: 10,
          idleTimeoutMillis: 30000,
          connectionTimeoutMillis: 2000,
        });

        const client = await this.pgPool.connect();
        client.release();
        this.isPostgresConnected = true;
        console.log('✅ Connected to PostgreSQL / PostGIS database');
      } catch (err) {
        console.warn('⚠️ PostgreSQL connection unestablished; utilizing in-memory spatial pool fallback with rich dev fixtures.');
        this.isPostgresConnected = false;
      }
    }
  }

  async query(text, params) {
    if (this.isPostgresConnected && this.pgPool) {
      return this.pgPool.query(text, params);
    }
    // Return empty query response structure when PostgreSQL is unavailable.
    return { rows: [], rowCount: 0 };
  }

  getStore() {
    return this.memoryStore;
  }
}

const poolInstance = new AegisDbPool();
poolInstance.initialize().catch(() => {});

module.exports = poolInstance;
