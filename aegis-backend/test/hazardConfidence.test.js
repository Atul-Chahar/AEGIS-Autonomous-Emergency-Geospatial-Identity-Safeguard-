const test = require('node:test');
const assert = require('node:assert/strict');
const confidenceEngine = require('../src/geospatial/HazardConfidenceEngine');
const hazardRepository = require('../src/repositories/HazardRepository');

test('Hazard Confidence Engine & Sybil-Resistant Evaluation Tests', async (t) => {
  t.beforeEach(async () => {
    await hazardRepository.clearAll();
  });

  await t.test('same reporter submitting three times is Sybil-protected and remains UNVERIFIED', async () => {
    const reporterId = 'REPORTER-SYBIL-001';
    const lat = 25.2600;
    const lon = 91.7000;

    // Report 1
    const res1 = await hazardRepository.createHazardReport({
      reporterId,
      reporterRole: 'TOURIST',
      hazardType: 'LANDSLIDE',
      lat,
      lon,
      description: 'First report'
    });
    assert.equal(res1.verificationStatus, 'UNVERIFIED');
    assert.equal(res1.confidenceScore, 1.0);

    // Report 2 (Same reporter)
    const res2 = await hazardRepository.createHazardReport({
      reporterId,
      reporterRole: 'TOURIST',
      hazardType: 'LANDSLIDE',
      lat: lat + 0.0001,
      lon: lon + 0.0001,
      description: 'Second report from same user'
    });
    assert.equal(res2.verificationStatus, 'UNVERIFIED');
    assert.equal(res2.confidenceScore, 1.0); // Sybil check: still 1 unique reporter

    // Report 3 (Same reporter)
    const res3 = await hazardRepository.createHazardReport({
      reporterId,
      reporterRole: 'TOURIST',
      hazardType: 'LANDSLIDE',
      lat: lat + 0.0002,
      lon: lon + 0.0002,
      description: 'Third report from same user'
    });
    assert.equal(res3.verificationStatus, 'UNVERIFIED');
    assert.equal(res3.confidenceScore, 1.0); // Still 1 unique reporter, not verified!
  });

  await t.test('three distinct reporters increases confidence to POSSIBLE / LIKELY', async () => {
    const lat = 25.2600;
    const lon = 91.7000;

    const res1 = await hazardRepository.createHazardReport({
      reporterId: 'TOURIST-A',
      reporterRole: 'TOURIST',
      hazardType: 'LANDSLIDE',
      lat, lon
    });
    assert.equal(res1.verificationStatus, 'UNVERIFIED'); // score 1.0

    const res2 = await hazardRepository.createHazardReport({
      reporterId: 'TOURIST-B',
      reporterRole: 'TOURIST',
      hazardType: 'LANDSLIDE',
      lat: lat + 0.0001, lon: lon + 0.0001
    });
    assert.equal(res2.verificationStatus, 'POSSIBLE'); // score 2.0

    const res3 = await hazardRepository.createHazardReport({
      reporterId: 'TOURIST-C',
      reporterRole: 'TOURIST',
      hazardType: 'LANDSLIDE',
      lat: lat + 0.0002, lon: lon + 0.0002
    });
    assert.equal(res3.verificationStatus, 'POSSIBLE'); // score 3.0
    assert.equal(res3.confidenceScore, 3.0);
  });

  await t.test('reports older than two hours do NOT contribute to confidence', async () => {
    const lat = 25.2600;
    const lon = 91.7000;
    const threeHoursAgo = new Date(Date.now() - (3 * 60 * 60 * 1000)).toISOString();

    // Old report
    await hazardRepository.createHazardReport({
      reporterId: 'TOURIST-OLD-1',
      reporterRole: 'TOURIST',
      hazardType: 'LANDSLIDE',
      lat, lon,
      timestamp: threeHoursAgo
    });

    // New report
    const resNew = await hazardRepository.createHazardReport({
      reporterId: 'TOURIST-NEW-1',
      reporterRole: 'TOURIST',
      hazardType: 'LANDSLIDE',
      lat, lon
    });

    // Old report discarded (> 2 hours), only new report counted
    assert.equal(resNew.confidenceScore, 1.0);
    assert.equal(resNew.verificationStatus, 'UNVERIFIED');
  });

  await t.test('reports farther than 500 m do NOT contribute to confidence', async () => {
    const lat1 = 25.2600;
    const lon1 = 91.7000;
    // 2 km away (~0.018 degrees lat diff)
    const lat2 = 25.2800;
    const lon2 = 91.7000;

    await hazardRepository.createHazardReport({
      reporterId: 'TOURIST-LOCATION-1',
      reporterRole: 'TOURIST',
      hazardType: 'LANDSLIDE',
      lat: lat1, lon: lon1
    });

    const resFar = await hazardRepository.createHazardReport({
      reporterId: 'TOURIST-LOCATION-2',
      reporterRole: 'TOURIST',
      hazardType: 'LANDSLIDE',
      lat: lat2, lon: lon2
    });

    // Far report is excluded (> 500m), score remains 1.0
    assert.equal(resFar.confidenceScore, 1.0);
    assert.equal(resFar.verificationStatus, 'UNVERIFIED');
  });

  await t.test('conflicting hazard types do NOT accumulate confidence', async () => {
    const lat = 25.2600;
    const lon = 91.7000;

    await hazardRepository.createHazardReport({
      reporterId: 'TOURIST-1',
      reporterRole: 'TOURIST',
      hazardType: 'LANDSLIDE',
      lat, lon
    });

    // Conflicting hazard type: WILDFIRE vs LANDSLIDE
    const resFire = await hazardRepository.createHazardReport({
      reporterId: 'TOURIST-2',
      reporterRole: 'TOURIST',
      hazardType: 'WILDFIRE',
      lat, lon
    });

    // Does not accumulate score for LANDSLIDE
    assert.equal(resFire.confidenceScore, 1.0);
    assert.equal(resFire.verificationStatus, 'UNVERIFIED');
  });

  await t.test('authority confirmation immediately transitions to AUTHORITY_CONFIRMED', async () => {
    const lat = 25.2600;
    const lon = 91.7000;

    const resAuth = await hazardRepository.createHazardReport({
      reporterId: 'DISPATCH-CHIEF-01',
      reporterRole: 'AUTHORITY',
      hazardType: 'LANDSLIDE',
      lat, lon,
      description: 'Official rescue team confirmation'
    });

    assert.equal(resAuth.verificationStatus, 'AUTHORITY_CONFIRMED');
    assert.ok(resAuth.confidenceScore >= 10.0);
    assert.equal(resAuth.routeClosed, true);
    assert.equal(resAuth.geofenceUpdated, true);
  });
});
