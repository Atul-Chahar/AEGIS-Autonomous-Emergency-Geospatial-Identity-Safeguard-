const postgis = require('./postgisHelper');

const MAX_REPORT_AGE_MS = 2 * 60 * 60 * 1000; // 2 hours (120 minutes)
const MAX_SPATIAL_DISTANCE_KM = 0.5; // 500 meters

class HazardConfidenceEngine {
  /**
   * Evaluates a candidate hazard report against existing active reports.
   *
   * @param {Object} candidateReport - { id, reporterId, reporterRole, hazardType, lat, lon, evidenceRef, hasWeatherEvidence, timestamp }
   * @param {Array} existingReports - List of all existing hazard report records
   * @returns {Object} { confidenceScore, verificationStatus, reason, effectiveReporters, auditTrail }
   */
  evaluateConfidence(candidateReport, existingReports = []) {
    const candidateTime = candidateReport.timestamp ? new Date(candidateReport.timestamp).getTime() : Date.now();
    const candidateLat = parseFloat(candidateReport.lat);
    const candidateLon = parseFloat(candidateReport.lon);
    const candidateType = (candidateReport.hazardType || 'UNKNOWN').toUpperCase();

    // 1. Filter existing reports:
    // a) Age <= 2 hours
    // b) Distance <= 500m
    // c) Compatible hazard type
    const validClusterReports = [];
    const reporterMap = new Map(); // Sybil protection: 1 effective report per distinct reporterId

    // Include candidate report in evaluation
    const allReports = [...existingReports, candidateReport];

    for (const report of allReports) {
      const reportTime = report.timestamp ? new Date(report.timestamp).getTime() : Date.now();
      const ageMs = candidateTime - reportTime;

      // Rule 1: Maximum age filter (2 hours)
      if (ageMs > MAX_REPORT_AGE_MS || ageMs < -60000) {
        continue;
      }

      // Rule 2: Spatial proximity filter (500 meters)
      const reportLat = parseFloat(report.lat);
      const reportLon = parseFloat(report.lon);
      const distKm = postgis.calculateDistanceKm(candidateLat, candidateLon, reportLat, reportLon);
      if (distKm > MAX_SPATIAL_DISTANCE_KM) {
        continue;
      }

      // Rule 3: Hazard-type similarity filter
      const reportType = (report.hazardType || 'UNKNOWN').toUpperCase();
      if (!this.areHazardTypesCompatible(candidateType, reportType)) {
        continue;
      }

      // Rule 4: Sybil / Reporter Deduplication (Only 1 effective report per reporterId)
      const rId = report.reporterId || 'ANONYMOUS';
      if (!reporterMap.has(rId) || this.getRoleWeight(report.reporterRole) > this.getRoleWeight(reporterMap.get(rId).reporterRole)) {
        reporterMap.set(rId, report);
      }
    }

    // 2. Compute Confidence Score from unique reporters + evidence bonuses
    let confidenceScore = 0.0;
    const effectiveReporters = [];
    let hasAuthority = false;

    for (const [rId, report] of reporterMap.entries()) {
      const role = report.reporterRole || 'TOURIST';
      const baseWeight = this.getRoleWeight(role);
      confidenceScore += baseWeight;
      effectiveReporters.push({ reporterId: rId, role, weight: baseWeight });

      if (role === 'AUTHORITY' || role === 'DISPATCHER') {
        hasAuthority = true;
      }

      // Evidence bonus (photo/sensor hash)
      if (report.evidenceRef) {
        confidenceScore += 0.5;
      }
      // External weather evidence bonus
      if (report.hasWeatherEvidence) {
        confidenceScore += 1.0;
      }
    }

    // 3. Map Confidence Score to Verification Status:
    // - UNVERIFIED: < 2.0
    // - POSSIBLE: 2.0 <= score < 5.0
    // - LIKELY: 5.0 <= score < 10.0
    // - AUTHORITY_CONFIRMED: score >= 10.0 or hasAuthority
    let verificationStatus = 'UNVERIFIED';
    if (hasAuthority || confidenceScore >= 10.0) {
      verificationStatus = 'AUTHORITY_CONFIRMED';
    } else if (confidenceScore >= 5.0) {
      verificationStatus = 'LIKELY';
    } else if (confidenceScore >= 2.0) {
      verificationStatus = 'POSSIBLE';
    } else {
      verificationStatus = 'UNVERIFIED';
    }

    const uniqueCount = reporterMap.size;
    const reason = `Evaluated score ${confidenceScore.toFixed(1)} from ${uniqueCount} distinct reporter(s) within 500m & 2h window. Status: ${verificationStatus}.`;

    return {
      confidenceScore: parseFloat(confidenceScore.toFixed(2)),
      verificationStatus,
      reason,
      effectiveReportersCount: uniqueCount,
      effectiveReporters,
      auditTrail: {
        evaluatedAt: new Date().toISOString(),
        candidateType,
        uniqueReporterCount: uniqueCount,
        hasAuthority,
        reason
      }
    };
  }

  getRoleWeight(role) {
    switch ((role || '').toUpperCase()) {
      case 'AUTHORITY':
      case 'DISPATCHER':
      case 'GOVERNMENT':
        return 10.0;
      case 'VERIFIED_GUIDE':
      case 'LOCAL_GUIDE':
      case 'RANGER':
        return 2.5;
      case 'TOURIST':
      case 'REGULAR_USER':
      default:
        return 1.0;
    }
  }

  areHazardTypesCompatible(typeA, typeB) {
    if (typeA === typeB) return true;
    const landslides = ['LANDSLIDE', 'ROCKFALL', 'MUDPOWER', 'DEBRIS_FLOW'];
    if (landslides.includes(typeA) && landslides.includes(typeB)) return true;
    const water = ['FLOOD', 'FLASH_FLOOD', 'RIVER_OVERFLOW'];
    if (water.includes(typeA) && water.includes(typeB)) return true;
    return false;
  }
}

module.exports = new HazardConfidenceEngine();
