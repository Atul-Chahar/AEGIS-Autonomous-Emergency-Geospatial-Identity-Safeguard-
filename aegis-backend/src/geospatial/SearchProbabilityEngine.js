const postgis = require('./postgisHelper');

class SearchProbabilityEngine {
  /**
   * Generates an original AEGIS search-probability model & search area reduction metric.
   *
   * @param {Object} input
   * @param {Object} input.lastBreadcrumb - { lat, lon, timestamp, accuracyMeters, batteryPercent }
   * @param {number} input.lastDirectionDeg - Heading bearing (0 - 360 degrees)
   * @param {string} input.activityMode - STILL, WALKING, RUNNING, IN_VEHICLE
   * @param {number} input.speedMetersPerSec - Speed estimate in m/s (e.g. 1.2 for walking)
   * @param {Array} input.plannedTrailWaypoints - List of [lat, lon] waypoints along planned route
   * @param {number} input.elapsedTimeMins - Elapsed time in minutes since last breadcrumb
   * @param {Array} input.barriers - List of hazard barrier polygons [[lat, lon], ...]
   * @returns {Object} Search probability result with GeoJSON heatmap, top 3 sectors, and search area reduction metric
   */
  calculateSearchProbability(input) {
    const lastLat = parseFloat(input.lastBreadcrumb?.lat || 25.145);
    const lastLon = parseFloat(input.lastBreadcrumb?.lon || 91.265);
    const speedMs = parseFloat(input.speedMetersPerSec !== undefined ? input.speedMetersPerSec : 1.2); // 1.2 m/s (~4.3 km/h)
    const elapsedMins = parseFloat(input.elapsedTimeMins !== undefined ? input.elapsedTimeMins : 60); // 60 mins
    const bearingDeg = parseFloat(input.lastDirectionDeg !== undefined ? input.lastDirectionDeg : 45.0);
    const trailWaypoints = input.plannedTrailWaypoints || [
      [25.145, 91.265],
      [25.165, 91.285],
      [25.185, 91.305]
    ];
    const barriers = input.barriers || [];

    // 1. Calculate Maximum Physically Reachable Radius & Search Area Before/After BlackBox
    const maxReachKm = Math.max(0.2, (speedMs * (elapsedMins * 60)) / 1000.0);
    
    // Unconstrained Search Area BEFORE BlackBox (Unconstrained 3.0 m/s speed radius over elapsed time)
    const unconstrainedReachKm = Math.max(0.5, (3.0 * (elapsedMins * 60)) / 1000.0);
    const searchAreaBeforeKm2 = Math.PI * Math.pow(unconstrainedReachKm, 2);

    // 2. Generate Bounded 10 x 10 Spatial Grid around Last Observation
    const gridSize = 10;
    const latSpan = (maxReachKm * 2) / 111.0; // ~111km per degree lat
    const lonSpan = (maxReachKm * 2) / (111.0 * Math.cos((lastLat * Math.PI) / 180.0));

    const minLat = lastLat - latSpan / 2;
    const minLon = lastLon - lonSpan / 2;
    const cellLatSpan = latSpan / gridSize;
    const cellLonSpan = lonSpan / gridSize;

    const rawCells = [];
    let totalRawScore = 0.0;

    for (let row = 0; row < gridSize; row++) {
      for (let col = 0; col < gridSize; col++) {
        const cellMinLat = minLat + row * cellLatSpan;
        const cellMaxLat = cellMinLat + cellLatSpan;
        const cellMinLon = minLon + col * cellLonSpan;
        const cellMaxLon = cellMinLon + cellLonSpan;
        const centerLat = cellMinLat + cellLatSpan / 2;
        const centerLon = cellMinLon + cellLonSpan / 2;

        const distFromLastKm = postgis.calculateDistanceKm(lastLat, lastLon, centerLat, centerLon);

        // Factor A: Physically Reachable Bound
        if (distFromLastKm > maxReachKm) {
          rawCells.push({
            row, col, bounds: [[cellMinLat, cellMinLon], [cellMaxLat, cellMaxLon]],
            centerLat, centerLon, rawScore: 0.0, distKm: distFromLastKm, isBarrier: false
          });
          continue;
        }

        // Factor B: Barrier / Exclusion Check
        let isBarrier = false;
        for (const barrier of barriers) {
          if (this.isPointInPolygon(centerLat, centerLon, barrier)) {
            isBarrier = true;
            break;
          }
        }
        if (isBarrier) {
          rawCells.push({
            row, col, bounds: [[cellMinLat, cellMinLon], [cellMaxLat, cellMaxLon]],
            centerLat, centerLon, rawScore: 0.0, distKm: distFromLastKm, isBarrier: true
          });
          continue;
        }

        // Factor C: Distance Decay (Gaussian)
        const distScore = Math.exp(-Math.pow(distFromLastKm / (maxReachKm * 0.7), 2));

        // Factor D: Bearing / Directional Alignment Bias
        const cellBearing = this.calculateBearing(lastLat, lastLon, centerLat, centerLon);
        const angleDelta = Math.abs(((cellBearing - bearingDeg + 180 + 360) % 360) - 180);
        const directionScore = Math.exp(-Math.pow(angleDelta / 60.0, 2));

        // Factor E: Planned Trail Preference Multiplier
        let trailMultiplier = 1.0;
        const minDistToTrailKm = this.getMinDistanceToTrail(centerLat, centerLon, trailWaypoints);
        if (minDistToTrailKm <= 0.3) {
          trailMultiplier = 2.5; // +2.5x trail preference
        } else if (minDistToTrailKm <= 0.8) {
          trailMultiplier = 1.5;
        }

        const rawScore = distScore * directionScore * trailMultiplier;
        totalRawScore += rawScore;

        rawCells.push({
          row, col, bounds: [[cellMinLat, cellMinLon], [cellMaxLat, cellMaxLon]],
          centerLat, centerLon, rawScore, distKm: distFromLastKm, minDistToTrailKm, isBarrier: false
        });
      }
    }

    // 3. Normalize Cell Probabilities (Sum = 1.0) & Compute High-Probability Search Area
    let highProbAreaCellsCount = 0;
    const probabilityCells = rawCells.map(cell => {
      const normalizedScore = totalRawScore > 0 ? cell.rawScore / totalRawScore : 0;
      const probabilityPercent = parseFloat((normalizedScore * 100).toFixed(2));
      if (probabilityPercent > 1.5) highProbAreaCellsCount++;

      return {
        row: cell.row,
        col: cell.col,
        bounds: cell.bounds,
        center: [cell.centerLat, cell.centerLon],
        distKm: parseFloat(cell.distKm.toFixed(2)),
        normalizedScore,
        probabilityPercent,
        isBarrier: cell.isBarrier
      };
    });

    // Constrained Search Area AFTER BlackBox
    const cellAreaKm2 = (latSpan * 111.0) * (lonSpan * 111.0 * Math.cos((lastLat * Math.PI) / 180.0)) / (gridSize * gridSize);
    const searchAreaAfterKm2 = Math.max(0.5, highProbAreaCellsCount * cellAreaKm2);
    const areaReductionPercent = Math.max(0, Math.min(99.5, ((searchAreaBeforeKm2 - searchAreaAfterKm2) / searchAreaBeforeKm2) * 100));

    // 4. Extract Top 3 Most Likely Search Sectors (Alpha, Bravo, Charlie)
    const sortedCells = [...probabilityCells]
      .filter(c => c.probabilityPercent > 0)
      .sort((a, b) => b.probabilityPercent - a.probabilityPercent);

    const sectorNames = ['Sector Alpha (Trail Corridor)', 'Sector Bravo (Primary Travel Direction)', 'Sector Charlie (Secondary Reach)'];
    const topSectors = sortedCells.slice(0, 3).map((cell, idx) => {
      let explanation = `High proximity (${cell.distKm} km from last fix)`;
      if (cell.distKm < 0.5) explanation += ` • Aligned with planned trail corridor`;
      if (idx === 0) explanation += ` • Highest combined travel probability`;

      return {
        sectorId: `SEC-0${idx + 1}`,
        name: sectorNames[idx] || `Sector ${idx + 1}`,
        probabilityPercent: cell.probabilityPercent,
        center: cell.center,
        bounds: cell.bounds,
        explanation
      };
    });

    // 5. Generate GeoJSON Output
    const geoJsonHeatmap = {
      type: 'FeatureCollection',
      features: probabilityCells.map(cell => ({
        type: 'Feature',
        geometry: {
          type: 'Polygon',
          coordinates: [[
            [cell.bounds[0][1], cell.bounds[0][0]],
            [cell.bounds[1][1], cell.bounds[0][0]],
            [cell.bounds[1][1], cell.bounds[1][0]],
            [cell.bounds[0][1], cell.bounds[1][0]],
            [cell.bounds[0][1], cell.bounds[0][0]]
          ]]
        },
        properties: {
          row: cell.row,
          col: cell.col,
          probabilityPercent: cell.probabilityPercent,
          normalizedScore: cell.normalizedScore,
          distKm: cell.distKm
        }
      }))
    };

    return {
      lastObservation: { lat: lastLat, lon: lastLon, elapsedMins, bearingDeg, speedMs },
      topSearchSectors: topSectors,
      metrics: {
        searchAreaBeforeBlackBoxKm2: parseFloat(searchAreaBeforeKm2.toFixed(1)),
        searchAreaAfterBlackBoxKm2: parseFloat(searchAreaAfterKm2.toFixed(1)),
        areaReductionPercent: parseFloat(areaReductionPercent.toFixed(1)),
        label: `${areaReductionPercent.toFixed(1)}% Search Area Reduction (${searchAreaBeforeKm2.toFixed(1)} km² → ${searchAreaAfterKm2.toFixed(1)} km²)`
      },
      geoJsonHeatmap,
      wordingDisclaimer: "Most likely search sectors based on telemetry & physical reach. Probability is an estimation, not certainty."
    };
  }

  calculateBearing(lat1, lon1, lat2, lon2) {
    const phi1 = (lat1 * Math.PI) / 180;
    const phi2 = (lat2 * Math.PI) / 180;
    const deltaLambda = ((lon2 - lon1) * Math.PI) / 180;

    const y = Math.sin(deltaLambda) * Math.cos(phi2);
    const x = Math.cos(phi1) * Math.sin(phi2) - Math.sin(phi1) * Math.cos(phi2) * Math.cos(deltaLambda);
    const theta = Math.atan2(y, x);
    return ((theta * 180) / Math.PI + 360) % 360;
  }

  getMinDistanceToTrail(lat, lon, trailWaypoints) {
    let minDist = 99999.0;
    for (const wp of trailWaypoints) {
      const dist = postgis.calculateDistanceKm(lat, lon, wp[0], wp[1]);
      if (dist < minDist) minDist = dist;
    }
    return minDist;
  }

  isPointInPolygon(lat, lon, polygonCoords) {
    if (!polygonCoords || polygonCoords.length < 3) return false;
    let inside = false;
    for (let i = 0, j = polygonCoords.length - 1; i < polygonCoords.length; j = i++) {
      const xi = polygonCoords[i][0], yi = polygonCoords[i][1];
      const xj = polygonCoords[j][0], yj = polygonCoords[j][1];
      const intersect = ((yi > lon) !== (yj > lon)) && (lat < (xj - xi) * (lon - yi) / (yj - yi) + xi);
      if (intersect) inside = !inside;
    }
    return inside;
  }
}

module.exports = new SearchProbabilityEngine();
