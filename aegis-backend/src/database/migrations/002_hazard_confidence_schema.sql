-- AEGIS PostgreSQL / PostGIS Migration 002: Hazard Confidence Schema
ALTER TABLE hazard_reports ADD COLUMN IF NOT EXISTS confidence_score DOUBLE PRECISION DEFAULT 1.0;
ALTER TABLE hazard_reports ADD COLUMN IF NOT EXISTS verification_status VARCHAR(32) DEFAULT 'UNVERIFIED';
ALTER TABLE hazard_reports ADD COLUMN IF NOT EXISTS evidence_ref TEXT;
ALTER TABLE hazard_reports ADD COLUMN IF NOT EXISTS reporter_role VARCHAR(32) DEFAULT 'TOURIST';
ALTER TABLE hazard_reports ADD COLUMN IF NOT EXISTS audit_trail_json JSONB;

-- Hazard Events (Audit Trail for hazard confidence changes & route/geofence updates)
CREATE TABLE IF NOT EXISTS hazard_events (
  id BIGSERIAL PRIMARY KEY,
  hazard_id VARCHAR(64) REFERENCES hazard_reports(id) ON DELETE CASCADE,
  event_type VARCHAR(64) NOT NULL, -- CONFIDENCE_UPDATED, GEOFENCE_RISK_UPDATED, ROUTE_CLOSED
  confidence_score DOUBLE PRECISION,
  verification_status VARCHAR(32),
  reason TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);
