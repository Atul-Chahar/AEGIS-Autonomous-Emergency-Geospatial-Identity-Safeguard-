const turf = require('@turf/turf');

module.exports = {
  makePointWkt(lat, lon) {
    return `SRID=4326;POINT(${lon} ${lat})`;
  },

  calculateDistanceKm(lat1, lon1, lat2, lon2) {
    const from = turf.point([parseFloat(lon1), parseFloat(lat1)]);
    const to = turf.point([parseFloat(lon2), parseFloat(lat2)]);
    return turf.distance(from, to, { units: 'kilometers' });
  },

  isPointInPolygon(lat, lon, polygonCoordinates) {
    const pt = turf.point([parseFloat(lon), parseFloat(lat)]);
    const poly = turf.polygon([polygonCoordinates]);
    return turf.booleanPointInPolygon(pt, poly);
  }
};
