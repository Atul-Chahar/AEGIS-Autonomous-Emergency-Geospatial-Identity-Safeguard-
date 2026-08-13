const db = require('../database/pool');

module.exports = {
  getHealth(req, res) {
    res.json({
      status: 'OK',
      timestamp: new Date().toISOString(),
      service: 'AEGIS API Gateway',
      version: '2.0.0',
      database: db.isPostgresConnected ? 'PostgreSQL/PostGIS' : 'In-Memory Spatial Pool (Fallback)',
      environment: process.env.NODE_ENV || 'development'
    });
  }
};
