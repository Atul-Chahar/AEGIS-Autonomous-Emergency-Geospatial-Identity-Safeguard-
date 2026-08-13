const { Pool } = require('pg');
const env = require('../config/env');
const devFixtures = require('./seeds/dev_fixtures');

class AegisDbPool {
  constructor() {
    this.isPostgresConnected = false;
    this.pgPool = null;

    // In-Memory Database Fallback for offline testing / zero-dependency runs
    this.memoryStore = {
      tourists: new Map(),
      trips: new Map(),
      breadcrumbs: [],
      incidents: [],
      incidentEvents: [],
      checkIns: [],
      hazardReports: [],
      safetyZones: [...devFixtures.devGeofences],
      responderUnits: [...devFixtures.devResponders],
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
        console.warn('⚠️ PostgreSQL connection unestablished; utilizing in-memory spatial pool fallback.');
        this.isPostgresConnected = false;
      }
    }
  }

  async query(text, params) {
    if (this.isPostgresConnected && this.pgPool) {
      return this.pgPool.query(text, params);
    }
    // Return mock query response structure for fallback
    return { rows: [], rowCount: 0 };
  }

  getStore() {
    return this.memoryStore;
  }
}

const poolInstance = new AegisDbPool();
poolInstance.initialize().catch(() => {});

module.exports = poolInstance;
