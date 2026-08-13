-- AEGIS PostgreSQL / PostGIS Database Migration 001
-- Enable PostGIS extension if available
CREATE EXTENSION IF NOT EXISTS postgis;

-- 1. Pseudonymous Tourist Identities (ZERO RAW PII STORED!)
CREATE TABLE IF NOT EXISTS tourists (
  tourist_id VARCHAR(64) PRIMARY KEY,
  id_hash VARCHAR(128) UNIQUE NOT NULL,
  itinerary_hash VARCHAR(128),
  valid_from TIMESTAMPTZ,
  valid_to TIMESTAMPTZ,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  qr_payload TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Trips
CREATE TABLE IF NOT EXISTS trips (
  id VARCHAR(64) PRIMARY KEY,
  tourist_id VARCHAR(64) REFERENCES tourists(tourist_id) ON DELETE CASCADE,
  started_at TIMESTAMPTZ DEFAULT NOW(),
  ended_at TIMESTAMPTZ,
  status VARCHAR(32) DEFAULT 'ACTIVE',
  planned_route_id VARCHAR(64)
);

-- 3. Breadcrumbs
CREATE TABLE IF NOT EXISTS breadcrumbs (
  id BIGSERIAL PRIMARY KEY,
  trip_id VARCHAR(64),
  tourist_id VARCHAR(64) REFERENCES tourists(tourist_id) ON DELETE CASCADE,
  lat DOUBLE PRECISION NOT NULL,
  lon DOUBLE PRECISION NOT NULL,
  location GEOMETRY(Point, 4326),
  horizontal_accuracy FLOAT,
  battery_percent INT,
  timestamp TIMESTAMPTZ DEFAULT NOW()
);

-- 4. Incidents (with unique packet_id for idempotency)
CREATE TABLE IF NOT EXISTS incidents (
  id VARCHAR(64) PRIMARY KEY,
  packet_id VARCHAR(128) UNIQUE,
  tourist_id VARCHAR(64) REFERENCES tourists(tourist_id) ON DELETE CASCADE,
  id_hash VARCHAR(128),
  lat DOUBLE PRECISION NOT NULL,
  lon DOUBLE PRECISION NOT NULL,
  location GEOMETRY(Point, 4326),
  battery_pct INT,
  channel VARCHAR(32) DEFAULT 'HTTPS',
  status VARCHAR(32) DEFAULT 'OPEN',
  risk_score INT DEFAULT 100,
  timestamp TIMESTAMPTZ DEFAULT NOW()
);

-- 5. Incident Events (Audit Trail)
CREATE TABLE IF NOT EXISTS incident_events (
  id BIGSERIAL PRIMARY KEY,
  incident_id VARCHAR(64) REFERENCES incidents(id) ON DELETE CASCADE,
  event_type VARCHAR(64) NOT NULL,
  payload JSONB,
  timestamp TIMESTAMPTZ DEFAULT NOW()
);

-- 6. Check-Ins
CREATE TABLE IF NOT EXISTS check_ins (
  id BIGSERIAL PRIMARY KEY,
  tourist_id VARCHAR(64) REFERENCES tourists(tourist_id) ON DELETE CASCADE,
  zone_id VARCHAR(64),
  lat DOUBLE PRECISION,
  lon DOUBLE PRECISION,
  location GEOMETRY(Point, 4326),
  note TEXT,
  timestamp TIMESTAMPTZ DEFAULT NOW()
);

-- 7. Hazard Reports
CREATE TABLE IF NOT EXISTS hazard_reports (
  id VARCHAR(64) PRIMARY KEY,
  reporter_id VARCHAR(64),
  hazard_type VARCHAR(64) NOT NULL,
  lat DOUBLE PRECISION NOT NULL,
  lon DOUBLE PRECISION NOT NULL,
  location GEOMETRY(Point, 4326),
  description TEXT,
  status VARCHAR(32) DEFAULT 'UNVERIFIED',
  timestamp TIMESTAMPTZ DEFAULT NOW()
);

-- 8. Safety Zones
CREATE TABLE IF NOT EXISTS safety_zones (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  risk_level VARCHAR(32) NOT NULL,
  color VARCHAR(32),
  boundary GEOMETRY(Polygon, 4326),
  coordinates_json JSONB
);

-- 9. Responder Units
CREATE TABLE IF NOT EXISTS responder_units (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  type VARCHAR(32) NOT NULL,
  lat DOUBLE PRECISION NOT NULL,
  lon DOUBLE PRECISION NOT NULL,
  location GEOMETRY(Point, 4326),
  status VARCHAR(32) DEFAULT 'AVAILABLE'
);

-- 10. Responder Capabilities
CREATE TABLE IF NOT EXISTS responder_capabilities (
  id BIGSERIAL PRIMARY KEY,
  responder_id VARCHAR(64) REFERENCES responder_units(id) ON DELETE CASCADE,
  capability VARCHAR(64) NOT NULL
);

-- 11. Relay Packet Receipts
CREATE TABLE IF NOT EXISTS relay_packet_receipts (
  packet_id VARCHAR(128) PRIMARY KEY,
  origin_tourist_id VARCHAR(64),
  relayed_by_tourist_id VARCHAR(64),
  received_at TIMESTAMPTZ DEFAULT NOW()
);
