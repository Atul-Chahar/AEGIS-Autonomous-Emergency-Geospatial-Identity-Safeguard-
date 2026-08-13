const db = require('../database/pool');
const postgis = require('../geospatial/postgisHelper');
const confidenceEngine = require('../geospatial/HazardConfidenceEngine');

class HazardRepository {
  async createHazardReport(report) {
    const id = report.id || `HAZ-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
    const lat = parseFloat(report.lat);
    const lon = parseFloat(report.lon);
    const reporterId = report.reporterId || 'ANONYMOUS';
    const reporterRole = report.reporterRole || 'TOURIST';
    const timestamp = report.timestamp || new Date().toISOString();

    const candidateReport = {
      id,
      reporterId,
      reporterRole,
      hazardType: report.hazardType,
      lat,
      lon,
      description: report.description || '',
      evidenceRef: report.evidenceRef || null,
      hasWeatherEvidence: !!report.hasWeatherEvidence,
      timestamp
    };

    // 1. Query existing active hazard reports for confidence evaluation
    const existingActiveHazards = await this.getAllHazards();

    // 2. Evaluate Confidence via HazardConfidenceEngine
    const evalResult = confidenceEngine.evaluateConfidence(candidateReport, existingActiveHazards);

    const confidenceScore = evalResult.confidenceScore;
    const verificationStatus = evalResult.verificationStatus;
    const reason = evalResult.reason;

    // 3. Save Record
    let savedRecord = null;

    if (db.isPostgresConnected) {
      const text = `
        INSERT INTO hazard_reports (id, reporter_id, hazard_type, lat, lon, location, description, status, confidence_score, verification_status, evidence_ref, reporter_role, audit_trail_json)
        VALUES ($1, $2, $3, $4, $5, ST_SetSRID(ST_MakePoint($5, $4), 4326), $6, $7, $8, $9, $10, $11, $12)
        RETURNING *;
      `;
      const values = [
        id,
        reporterId,
        report.hazardType,
        lat,
        lon,
        report.description || '',
        verificationStatus,
        confidenceScore,
        verificationStatus,
        report.evidenceRef || null,
        reporterRole,
        JSON.stringify(evalResult.auditTrail)
      ];
      const res = await db.query(text, values);
      savedRecord = res.rows[0];
    } else {
      savedRecord = {
        id,
        reporterId,
        reporterRole,
        hazardType: report.hazardType,
        lat,
        lon,
        description: report.description || '',
        status: verificationStatus,
        confidenceScore,
        verificationStatus,
        evidenceRef: report.evidenceRef || null,
        auditTrail: evalResult.auditTrail,
        timestamp
      };
      db.getStore().hazardReports.push(savedRecord);
    }

    // 4. Log Audit Event for confidence evaluation
    await this.logHazardEvent(id, 'CONFIDENCE_UPDATED', confidenceScore, verificationStatus, reason);

    // 5. If status is LIKELY or AUTHORITY_CONFIRMED, log route / geofence audit event
    let routeClosed = false;
    let geofenceUpdated = false;

    if (verificationStatus === 'LIKELY' || verificationStatus === 'AUTHORITY_CONFIRMED') {
      routeClosed = true;
      geofenceUpdated = true;
      await this.logHazardEvent(id, 'ROUTE_CLOSED', confidenceScore, verificationStatus, `Route corridor closed due to ${verificationStatus} hazard (${report.hazardType})`);
      await this.logHazardEvent(id, 'GEOFENCE_RISK_UPDATED', confidenceScore, verificationStatus, `Geofence risk rating updated to HIGH_RISK due to ${verificationStatus} hazard (${report.hazardType})`);
    }

    return {
      hazard: savedRecord,
      confidenceScore,
      verificationStatus,
      reason,
      routeClosed,
      geofenceUpdated,
      isVerified: verificationStatus === 'AUTHORITY_CONFIRMED' || verificationStatus === 'LIKELY'
    };
  }

  async logHazardEvent(hazardId, eventType, confidenceScore, verificationStatus, reason) {
    if (db.isPostgresConnected) {
      await db.query(
        `INSERT INTO hazard_events (hazard_id, event_type, confidence_score, verification_status, reason)
         VALUES ($1, $2, $3, $4, $5);`,
        [hazardId, eventType, confidenceScore, verificationStatus, reason]
      );
    } else {
      if (!db.getStore().hazardEvents) {
        db.getStore().hazardEvents = [];
      }
      db.getStore().hazardEvents.push({
        id: Date.now(),
        hazardId,
        eventType,
        confidenceScore,
        verificationStatus,
        reason,
        createdAt: new Date().toISOString()
      });
    }
  }

  async getAllHazards() {
    if (db.isPostgresConnected) {
      const res = await db.query('SELECT * FROM hazard_reports ORDER BY timestamp DESC;');
      return res.rows;
    }
    return db.getStore().hazardReports;
  }

  async clearAll() {
    if (db.isPostgresConnected) {
      await db.query('DELETE FROM hazard_reports;');
      await db.query('DELETE FROM hazard_events;');
    } else {
      db.getStore().hazardReports = [];
      db.getStore().hazardEvents = [];
    }
  }
}

module.exports = new HazardRepository();
