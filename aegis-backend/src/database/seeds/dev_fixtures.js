/**
 * DEVELOPMENT SEED FIXTURES
 * Clearly labeled initial development data for local testing & preview.
 * Do NOT treat development seed data as real production statistics.
 */
module.exports = {
  devResponders: [
    {
      id: 'RES-01',
      name: 'Responder A (Cherrapunji Station)',
      type: 'RESCUE',
      lat: 25.148,
      lon: 91.270,
      status: 'AVAILABLE',
      vehicle: 'FOOT_PATROL',
      medicalCapability: true,
      ropeMountainCapability: false,
      waterRescueCapability: false,
      teamSize: 2,
      primarySegmentId: 'SEG-DAWKI-BRIDGE'
    },
    {
      id: 'RES-02',
      name: 'Responder B (Shillong Mountain S&R)',
      type: 'RESCUE',
      lat: 25.185,
      lon: 91.315,
      status: 'AVAILABLE',
      vehicle: '4WD_AMBULANCE',
      medicalCapability: true,
      ropeMountainCapability: true,
      waterRescueCapability: true,
      teamSize: 5,
      primarySegmentId: 'SEG-SHILLONG-HWY'
    },
    {
      id: 'POL-04',
      name: 'District Highway Patrol Unit 4',
      type: 'POLICE',
      lat: 25.280,
      lon: 91.720,
      status: 'AVAILABLE',
      vehicle: 'RESCUE_TRUCK',
      medicalCapability: true,
      ropeMountainCapability: false,
      waterRescueCapability: false,
      teamSize: 3,
      primarySegmentId: 'SEG-MAIN-HWY'
    }
  ],
  devRouteSegments: [
    {
      segmentId: 'SEG-DAWKI-BRIDGE',
      responderId: 'RES-01',
      fromNode: 'Cherrapunji Station',
      toNode: 'Dawki Canyon Target',
      distanceKm: 3.0,
      expectedTravelTimeMins: 8.0,
      surfaceType: 'PAVED',
      slopeCost: 1.0,
      isBlocked: true,
      hazard: 'Dawki River Bridge BLOCKED by Landslide Collapse'
    },
    {
      segmentId: 'SEG-SHILLONG-HWY',
      responderId: 'RES-02',
      fromNode: 'Shillong Station',
      toNode: 'Dawki Canyon Target',
      distanceKm: 7.0,
      expectedTravelTimeMins: 14.0,
      surfaceType: 'PAVED',
      slopeCost: 1.1,
      isBlocked: false,
      hazard: null
    }
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
