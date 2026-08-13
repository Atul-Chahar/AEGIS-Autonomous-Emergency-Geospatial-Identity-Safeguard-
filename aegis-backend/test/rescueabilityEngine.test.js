const test = require('node:test');
const assert = require('node:assert/strict');
const rescueEngine = require('../src/geospatial/RescueabilityEngine');

test('AEGIS RescueabilityEngine & Terrain-Aware Routing Tests', async (t) => {

  await t.test('Required Demonstration: Responder A (3 km, blocked bridge) vs Responder B (7 km, open route + rope gear) -> Recommends Responder B', () => {
    const incident = {
      lat: 25.145,
      lon: 91.265,
      requiredCapabilities: ['MEDICAL', 'ROPE']
    };

    const responders = [
      {
        id: 'RES-A',
        name: 'Responder A (Cherrapunji Station)',
        lat: 25.148,
        lon: 91.270, // ~3.0 km
        status: 'AVAILABLE',
        vehicle: 'FOOT_PATROL',
        medicalCapability: true,
        ropeMountainCapability: false,
        primarySegmentId: 'SEG-DAWKI-BRIDGE'
      },
      {
        id: 'RES-B',
        name: 'Responder B (Shillong Mountain S&R)',
        lat: 25.185,
        lon: 91.315, // ~7.0 km
        status: 'AVAILABLE',
        vehicle: '4WD_AMBULANCE',
        medicalCapability: true,
        ropeMountainCapability: true,
        primarySegmentId: 'SEG-SHILLONG-HWY'
      }
    ];

    const routeSegments = [
      {
        segmentId: 'SEG-DAWKI-BRIDGE',
        isBlocked: true,
        hazard: 'Dawki Bridge Landslide Collapse'
      },
      {
        segmentId: 'SEG-SHILLONG-HWY',
        isBlocked: false,
        surfaceType: 'PAVED',
        slopeCost: 1.0
      }
    ];

    const result = rescueEngine.evaluateRescueability(incident, responders, routeSegments);

    // 1. Geographically nearest must be Responder A
    assert.equal(result.geographicallyNearest.id, 'RES-A');
    assert.ok(result.geographicallyNearest.geoDistanceKm < 5.0);

    // 2. Operationally recommended MUST be Responder B
    assert.equal(result.operationallyRecommended.id, 'RES-B');
    assert.equal(result.operationallyRecommended.isBlocked, false);
    assert.ok(result.operationallyRecommended.feasibleETAMins < 30.0);

    // 3. Divergence explanation must explicitly detail the difference
    assert.ok(result.divergenceExplanation.includes('Responder A'));
    assert.ok(result.divergenceExplanation.includes('IMPASSABLE'));
    assert.ok(result.divergenceExplanation.includes('Responder B'));
  });

  await t.test('capability filter: responder missing required capability for water incident is penalized', () => {
    const incident = {
      lat: 25.145,
      lon: 91.265,
      requiredCapabilities: ['WATER']
    };

    const responders = [
      {
        id: 'UNIT-FOOT',
        name: 'Foot Unit No Boat',
        lat: 25.148,
        lon: 91.270,
        status: 'AVAILABLE',
        waterRescueCapability: false
      },
      {
        id: 'UNIT-BOAT',
        name: 'River Boat Unit',
        lat: 25.185,
        lon: 91.315,
        status: 'AVAILABLE',
        waterRescueCapability: true
      }
    ];

    const result = rescueEngine.evaluateRescueability(incident, responders, []);
    assert.equal(result.operationallyRecommended.id, 'UNIT-BOAT');
  });

  await t.test('terrain surface speed cost: mountain path increases travel time ETA', () => {
    const responder = {
      id: 'RES-TEST',
      lat: 25.145,
      lon: 91.265,
      vehicle: 'FOOT_PATROL'
    };

    const pavedRoute = [{ segmentId: 'SEG-1', surfaceType: 'PAVED', slopeCost: 1.0 }];
    const mountainRoute = [{ segmentId: 'SEG-1', surfaceType: 'MOUNTAIN_PATH', slopeCost: 2.0 }];

    const pavedETA = rescueEngine.calculateFeasibleRouteETA(responder, 25.185, 91.305, pavedRoute);
    const mountainETA = rescueEngine.calculateFeasibleRouteETA(responder, 25.185, 91.305, mountainRoute);

    assert.ok(mountainETA.feasibleETAMins > pavedETA.feasibleETAMins * 2.0);
  });
});
