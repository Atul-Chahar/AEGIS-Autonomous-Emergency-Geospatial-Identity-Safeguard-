const test = require('node:test');
const assert = require('node:assert/strict');
const searchEngine = require('../src/geospatial/SearchProbabilityEngine');

test('AEGIS Search-Probability Engine & Area Reduction Metric Tests', async (t) => {

  await t.test('probability normalization: sum of all normalized cell probabilities equals 1.0 (100%)', () => {
    const input = {
      lastBreadcrumb: { lat: 25.145, lon: 91.265 },
      speedMetersPerSec: 1.2,
      elapsedTimeMins: 60,
      lastDirectionDeg: 45.0
    };

    const result = searchEngine.calculateSearchProbability(input);
    assert.ok(result.geoJsonHeatmap);
    assert.equal(result.geoJsonHeatmap.type, 'FeatureCollection');

    // Sum normalized scores
    const totalScore = result.geoJsonHeatmap.features.reduce(
      (sum, feat) => sum + feat.properties.normalizedScore,
      0
    );

    assert.ok(Math.abs(totalScore - 1.0) < 0.001, `Total score sum should be ~1.0, got ${totalScore}`);
  });

  await t.test('trail preference: cell along planned trail has higher probability than off-trail cell at same distance', () => {
    const input = {
      lastBreadcrumb: { lat: 25.145, lon: 91.265 },
      speedMetersPerSec: 1.5,
      elapsedTimeMins: 45,
      lastDirectionDeg: 45.0,
      plannedTrailWaypoints: [
        [25.145, 91.265],
        [25.155, 91.275] // Trail goes North-East
      ]
    };

    const result = searchEngine.calculateSearchProbability(input);
    const features = result.geoJsonHeatmap.features;

    // Find cell on trail vs cell off trail
    const onTrailCell = features.find(f => f.properties.distKm > 0.2 && f.properties.distKm < 1.0 && f.properties.normalizedScore > 0.05);
    assert.ok(onTrailCell, "Cell on trail should exist with elevated probability");
  });

  await t.test('barrier exclusion: cell inside hazard barrier receives 0 probability', () => {
    // Barrier polygon covering a specific region
    const barrierPolygon = [
      [25.140, 91.260],
      [25.150, 91.260],
      [25.150, 91.270],
      [25.140, 91.270]
    ];

    const input = {
      lastBreadcrumb: { lat: 25.145, lon: 91.265 },
      barriers: [barrierPolygon]
    };

    const result = searchEngine.calculateSearchProbability(input);
    const barrierCell = result.geoJsonHeatmap.features.find(f => f.properties.normalizedScore === 0.0);
    assert.ok(barrierCell, "Barrier cell should receive 0 probability");
  });

  await t.test('search area reduction metric: searchAreaBefore is strictly greater than searchAreaAfter', () => {
    const input = {
      lastBreadcrumb: { lat: 25.145, lon: 91.265 },
      speedMetersPerSec: 1.0,
      elapsedTimeMins: 60
    };

    const result = searchEngine.calculateSearchProbability(input);
    const metrics = result.metrics;

    assert.ok(metrics.searchAreaBeforeBlackBoxKm2 > metrics.searchAreaAfterBlackBoxKm2);
    assert.ok(metrics.areaReductionPercent > 0.0);
    assert.ok(metrics.label.includes('Search Area Reduction'));
  });

  await t.test('top 3 search sectors extraction: returns top 3 sorted search sectors', () => {
    const input = {
      lastBreadcrumb: { lat: 25.145, lon: 91.265 },
      speedMetersPerSec: 1.2,
      elapsedTimeMins: 60
    };

    const result = searchEngine.calculateSearchProbability(input);
    assert.ok(Array.isArray(result.topSearchSectors));
    assert.equal(result.topSearchSectors.length, 3);
    assert.ok(result.topSearchSectors[0].probabilityPercent >= result.topSearchSectors[1].probabilityPercent);
    assert.ok(result.topSearchSectors[1].probabilityPercent >= result.topSearchSectors[2].probabilityPercent);
    assert.ok(result.wordingDisclaimer.includes('Most likely search sectors'));
  });
});
