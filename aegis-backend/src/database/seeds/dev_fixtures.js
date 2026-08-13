/**
 * DEVELOPMENT SEED FIXTURES
 * Clearly labeled initial development data for local testing & preview.
 * Do NOT treat development seed data as real production statistics.
 */
module.exports = {
  devResponders: [
    { id: 'RES-01', name: 'Meghalaya S&R Unit 1 [DEV FIXTURE]', type: 'RESCUE', lat: 25.148, lon: 91.270, status: 'AVAILABLE', capabilities: ['RAPID_ASCENT', 'MEDICAL_STABILIZATION'] },
    { id: 'POL-04', name: 'Cherrapunji District Police [DEV FIXTURE]', type: 'POLICE', lat: 25.280, lon: 91.720, status: 'AVAILABLE', capabilities: ['GEO_PATROL', 'DISPATCH'] },
    { id: 'MED-02', name: 'Shillong Civil Medical Rapid [DEV FIXTURE]', type: 'MEDICAL', lat: 25.570, lon: 91.880, status: 'AVAILABLE', capabilities: ['TRAUMA_CARE', 'EVACUATION'] }
  ],
  devGeofences: [
    {
      id: 'GF-01',
      name: 'Shillong Urban Zone [DEV FIXTURE]',
      riskLevel: 'SAFE',
      color: '#10B981',
      coordinates: [
        [91.85, 25.55], [91.92, 25.55], [91.92, 25.60], [91.85, 25.60], [91.85, 25.55]
      ]
    },
    {
      id: 'GF-02',
      name: 'Cherrapunji Ridge & Landslide Risk [DEV FIXTURE]',
      riskLevel: 'CAUTION',
      color: '#F59E0B',
      coordinates: [
        [91.68, 25.25], [91.78, 25.25], [91.78, 25.32], [91.68, 25.32], [91.68, 25.25]
      ]
    },
    {
      id: 'GF-03',
      name: 'Dawki River Canyon High Flash-Flood Zone [DEV FIXTURE]',
      riskLevel: 'HIGH_RISK',
      color: '#EF4444',
      coordinates: [
        [91.20, 25.10], [91.35, 25.10], [91.35, 25.20], [91.20, 25.20], [91.20, 25.10]
      ]
    }
  ]
};
