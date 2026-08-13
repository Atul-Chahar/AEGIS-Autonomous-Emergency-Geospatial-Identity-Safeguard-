const path = require('path');

module.exports = {
  port: process.env.PORT || 5000,
  nodeEnv: process.env.NODE_ENV || 'development',
  databaseUrl: process.env.DATABASE_URL || process.env.PG_URL || null,
  pgHost: process.env.PGHOST || 'localhost',
  pgPort: parseInt(process.env.PGPORT) || 5432,
  pgDatabase: process.env.PGDATABASE || 'aegis_db',
  pgUser: process.env.PGUSER || 'postgres',
  pgPassword: process.env.PGPASSWORD || 'postgres',
  jwtSecret: process.env.JWT_SECRET || 'aegis-secret-key-sepolia-amoy-2026',
  rateLimitWindowMs: 15 * 60 * 1000, // 15 minutes
  rateLimitMax: 100, // 100 requests per 15 min per IP
};
