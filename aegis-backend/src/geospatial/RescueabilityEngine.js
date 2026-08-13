const postgis = require('./postgisHelper');

class RescueabilityEngine {
  /**
   * Evaluates responder rescueability based on graph route traversal, feasible ETA, capability matching, and hazard blockages.
   *
   * @param {Object} incident - Incident details { lat, lon, requiredCapabilities: ['ROPE', 'MEDICAL'] }
   * @param {Array} responders - List of responder units
   * @param {Array} routeSegments - Graph route segments { from, to, distanceKm, surfaceType, slopeCost, isBlocked, hazard }
   * @returns {Object} { geographicallyNearest, operationallyRecommended, sortedResponders, divergenceExplanation }
   */
  evaluateRescueability(incident, responders, routeSegments = []) {
    if (!responders || responders.length === 0) {
      return { geographicallyNearest: null, operationallyRecommended: null, sortedResponders: [], divergenceExplanation: "No responder units available." };
    }

    const incLat = parseFloat(incident.lat);
    const incLon = parseFloat(incident.lon);
    const requiredCaps = incident.requiredCapabilities || ['MEDICAL'];

    // 1. Calculate Geographically Nearest Responder (Straight-line distance)
    const geoSorted = [...responders].map(r => ({
      ...r,
      geoDistanceKm: postgis.calculateDistanceKm(incLat, incLon, r.lat, r.lon)
    })).sort((a, b) => a.geoDistanceKm - b.geoDistanceKm);

    const geographicallyNearest = geoSorted[0];

    // 2. Evaluate Feasible Route Traversal ETA & Capability Match for each responder
    const evaluatedResponders = responders.map(r => {
      const geoDist = postgis.calculateDistanceKm(incLat, incLon, r.lat, r.lon);
      
      // Check Route Segment Blockages & Surface Travel Time
      const routeCheck = this.calculateFeasibleRouteETA(r, incLat, incLon, routeSegments);
      
      // Check Capability Match Score
      const capMatch = this.calculateCapabilityMatch(r, requiredCaps);

      let isBlocked = routeCheck.isBlocked;
      let feasibleETAMins = routeCheck.feasibleETAMins;

      // Rescueability Composite Score: Higher is better
      let rescueabilityScore = 0;
      if (!isBlocked && capMatch.hasRequiredCaps && r.status !== 'OFFLINE') {
        rescueabilityScore = (capMatch.score * 100) / (feasibleETAMins + 1.0);
      }

      return {
        ...r,
        geoDistanceKm: parseFloat(geoDist.toFixed(2)),
        feasibleETAMins: isBlocked ? Infinity : parseFloat(feasibleETAMins.toFixed(1)),
        isBlocked,
        blockageReason: routeCheck.blockageReason,
        capabilityMatchScore: capMatch.score,
        hasRequiredCaps: capMatch.hasRequiredCaps,
        matchedCaps: capMatch.matchedCaps,
        missingCaps: capMatch.missingCaps,
        rescueabilityScore: parseFloat(rescueabilityScore.toFixed(2))
      };
    });

    // Sort by Operational Rescueability Score (descending)
    const operationallySorted = [...evaluatedResponders].sort((a, b) => {
      if (a.isBlocked && !b.isBlocked) return 1;
      if (!a.isBlocked && b.isBlocked) return -1;
      return b.rescueabilityScore - a.rescueabilityScore;
    });

    const operationallyRecommended = operationallySorted[0];

    // 3. Generate Divergence Explanation if Geographically Nearest differs from Operationally Recommended
    let divergenceExplanation = "Geographically nearest responder matches operational recommendation.";
    if (geographicallyNearest.id !== operationallyRecommended.id) {
      const nearestEval = evaluatedResponders.find(r => r.id === geographicallyNearest.id);
      let reason = `Responder ${geographicallyNearest.name} is geographically closer (${nearestEval.geoDistanceKm} km), `;

      if (nearestEval.isBlocked) {
        reason += `but its route is IMPASSABLE (${nearestEval.blockageReason}). `;
      } else if (!nearestEval.hasRequiredCaps) {
        reason += `but lacks required capabilities (${nearestEval.missingCaps.join(', ')}). `;
      } else {
        reason += `but has a longer feasible travel ETA (${nearestEval.feasibleETAMins} mins). `;
      }

      reason += `Responder ${operationallyRecommended.name} (${operationallyRecommended.geoDistanceKm} km) is operationally recommended with an open route, Feasible ETA of ${operationallyRecommended.feasibleETAMins} mins, and 100% capability match.`;
      divergenceExplanation = reason;
    }

    return {
      geographicallyNearest: evaluatedResponders.find(r => r.id === geographicallyNearest.id),
      operationallyRecommended,
      sortedResponders: operationallySorted,
      divergenceExplanation
    };
  }

  calculateFeasibleRouteETA(responder, targetLat, targetLon, routeSegments) {
    // Default base speed depending on vehicle type
    let baseSpeedKmH = 35.0; // Default emergency vehicle speed
    if (responder.vehicle === 'FOOT_PATROL') baseSpeedKmH = 5.0;
    if (responder.vehicle === '4WD_AMBULANCE') baseSpeedKmH = 40.0;
    if (responder.vehicle === 'HELICOPTER') baseSpeedKmH = 150.0;
    if (responder.vehicle === 'RESCUE_TRUCK') baseSpeedKmH = 35.0;

    // Check if responder's primary route segment is blocked
    let relevantSegment = routeSegments.find(s => s.responderId === responder.id || s.segmentId === responder.primarySegmentId);
    if (!relevantSegment && routeSegments.length > 0) {
      relevantSegment = routeSegments[0];
    }
    if (relevantSegment && (relevantSegment.isBlocked || relevantSegment.blockedStatus)) {
      return {
        isBlocked: true,
        feasibleETAMins: Infinity,
        blockageReason: relevantSegment.hazard || relevantSegment.currentHazard || 'Bridge/Route Blocked by Landslide'
      };
    }

    const distKm = postgis.calculateDistanceKm(responder.lat, responder.lon, targetLat, targetLon);
    let slopeFactor = relevantSegment?.slopeCost || 1.0;
    let surfaceFactor = 1.0;

    if (relevantSegment?.surfaceType === 'GRAVEL') surfaceFactor = 1.3;
    if (relevantSegment?.surfaceType === 'DIRT_TRAIL') surfaceFactor = 1.6;
    if (relevantSegment?.surfaceType === 'MOUNTAIN_PATH') surfaceFactor = 2.2;

    const effectiveSpeed = baseSpeedKmH / (slopeFactor * surfaceFactor);
    const travelTimeMins = Math.max(2, (distKm / effectiveSpeed) * 60.0);

    return {
      isBlocked: false,
      feasibleETAMins: travelTimeMins,
      blockageReason: null
    };
  }

  calculateCapabilityMatch(responder, requiredCapabilities) {
    let matchedCount = 0;
    const matchedCaps = [];
    const missingCaps = [];

    const capsMap = {
      MEDICAL: responder.medicalCapability || responder.capabilities?.medical,
      ROPE: responder.ropeMountainCapability || responder.capabilities?.ropeMountain,
      WATER: responder.waterRescueCapability || responder.capabilities?.waterRescue,
      TEAM: (responder.teamSize || 1) >= 2
    };

    for (const req of requiredCapabilities) {
      if (capsMap[req]) {
        matchedCount++;
        matchedCaps.push(req);
      } else {
        missingCaps.push(req);
      }
    }

    const hasRequiredCaps = missingCaps.length === 0;
    const score = requiredCapabilities.length > 0 ? matchedCount / requiredCapabilities.length : 1.0;

    return { score, hasRequiredCaps, matchedCaps, missingCaps };
  }
}

module.exports = new RescueabilityEngine();
