/**
 * DEVELOPMENT SEED FIXTURES - CHERRAPUNJI (SOHRA) REGION FOCUS
 * Realistic geospatial dataset locked to Cherrapunji sector (25.275°N, 91.730°E).
 * Uses realistic, organic multi-vertex geographic boundaries (river contours, mountain gorges, cliff edges).
 */
const now = new Date();

module.exports = {
  devResponders: [
    {
      id: 'RES-01',
      name: 'Cherrapunji S&R Base Station 01',
      type: 'RESCUE',
      lat: 25.280,
      lon: 91.735,
      status: 'AVAILABLE',
      vehicle: '4WD_MOUNTAIN_RESCUE',
      medicalCapability: true,
      ropeMountainCapability: true,
      waterRescueCapability: true,
      teamSize: 4,
      primarySegmentId: 'SEG-SOHRA-NORTH'
    },
    {
      id: 'RES-02',
      name: 'Nongriat Valley Rapid Medical Unit',
      type: 'RESCUE',
      lat: 25.252,
      lon: 91.678,
      status: 'AVAILABLE',
      vehicle: 'FOOT_PATROL_MEDICAL',
      medicalCapability: true,
      ropeMountainCapability: true,
      waterRescueCapability: false,
      teamSize: 3,
      primarySegmentId: 'SEG-NONGRIAT-TRAIL'
    },
    {
      id: 'POL-04',
      name: 'Sohra Tourist Police Patrol Unit 4',
      type: 'POLICE',
      lat: 25.270,
      lon: 91.725,
      status: 'AVAILABLE',
      vehicle: 'RESCUE_TRUCK',
      medicalCapability: true,
      ropeMountainCapability: false,
      waterRescueCapability: false,
      teamSize: 2,
      primarySegmentId: 'SEG-MAIN-CORRIDOR'
    }
  ],

  devRouteSegments: [
    {
      segmentId: 'SEG-SOHRA-NORTH',
      responderId: 'RES-01',
      fromNode: 'Cherrapunji Base',
      toNode: 'Nohkalikai Canyon Target',
      distanceKm: 2.4,
      expectedTravelTimeMins: 6.0,
      surfaceType: 'PAVED',
      slopeCost: 1.0,
      isBlocked: false,
      hazard: null
    },
    {
      segmentId: 'SEG-NONGRIAT-TRAIL',
      responderId: 'RES-02',
      fromNode: 'Nongriat Station',
      toNode: 'Seven Sisters Black Zone',
      distanceKm: 3.8,
      expectedTravelTimeMins: 16.0,
      surfaceType: 'UNPAVED_ROCK',
      slopeCost: 1.4,
      isBlocked: true,
      hazard: 'Cliffside Rockfall & Restricted Zone Barrier'
    }
  ],

  devGeofences: [
    {
      id: 'GF-01',
      name: 'Nongriat Living Root Bridge Safe Eco-Corridor',
      riskLevel: 'SAFE',
      color: '#059669',
      coordinates: [
        [25.2420, 91.6620],
        [25.2465, 91.6660],
        [25.2510, 91.6685],
        [25.2555, 91.6720],
        [25.2590, 91.6780],
        [25.2615, 91.6840],
        [25.2580, 91.6880],
        [25.2530, 91.6860],
        [25.2485, 91.6810],
        [25.2440, 91.6740],
        [25.2395, 91.6680],
        [25.2420, 91.6620]
      ]
    },
    {
      id: 'GF-02',
      name: 'Mawsmai Ridge & Cloud Escarpment (Yellow Zone)',
      riskLevel: 'CAUTION',
      color: '#D97706',
      coordinates: [
        [25.2330, 91.7120],
        [25.2380, 91.7080],
        [25.2445, 91.7110],
        [25.2490, 91.7180],
        [25.2540, 91.7270],
        [25.2565, 91.7380],
        [25.2535, 91.7470],
        [25.2470, 91.7510],
        [25.2410, 91.7460],
        [25.2365, 91.7370],
        [25.2320, 91.7250],
        [25.2330, 91.7120]
      ]
    },
    {
      id: 'GF-03',
      name: 'Nohkalikai Falls Canyon & Flash-Flood Basin (Red Zone)',
      riskLevel: 'HIGH_RISK',
      color: '#E11D48',
      coordinates: [
        [25.2660, 91.6760],
        [25.2710, 91.6720],
        [25.2770, 91.6710],
        [25.2835, 91.6740],
        [25.2880, 91.6810],
        [25.2895, 91.6910],
        [25.2870, 91.7010],
        [25.2815, 91.7070],
        [25.2750, 91.7060],
        [25.2700, 91.6980],
        [25.2670, 91.6890],
        [25.2645, 91.6820],
        [25.2660, 91.6760]
      ]
    },
    {
      id: 'GF-04',
      name: 'Seven Sisters Cliff Edge & Bio-Reserve (Black Zone / Restricted)',
      riskLevel: 'RESTRICTED',
      color: '#18181B',
      coordinates: [
        [25.2130, 91.6680],
        [25.2185, 91.6640],
        [25.2260, 91.6670],
        [25.2325, 91.6750],
        [25.2360, 91.6860],
        [25.2375, 91.6990],
        [25.2340, 91.7120],
        [25.2280, 91.7180],
        [25.2210, 91.7160],
        [25.2150, 91.7080],
        [25.2110, 91.6950],
        [25.2095, 91.6810],
        [25.2130, 91.6680]
      ]
    }
  ],

  devHazards: [
    {
      id: 'HAZ-01',
      reporterId: 'SDRF-OBSERVER-01',
      reporterRole: 'AUTHORITY',
      hazardType: 'LANDSLIDE_COLLAPSE',
      lat: 25.274,
      lon: 91.688,
      description: 'Major rockfall and road blockage along Nohkalikai canyon descent. Impassable for standard vehicles.',
      status: 'VERIFIED_DANGER',
      confidenceScore: 98,
      verificationStatus: 'AUTHORITY_CONFIRMED',
      timestamp: new Date(now.getTime() - 15 * 60000).toISOString()
    },
    {
      id: 'HAZ-02',
      reporterId: 'TOURIST-REP-44',
      reporterRole: 'TOURIST',
      hazardType: 'CLIFF_EROSION',
      lat: 25.225,
      lon: 91.695,
      description: 'High vertical cliff instability near Seven Sisters boundary. Zero-entry perimeter active.',
      status: 'ACTIVE_WARNING',
      confidenceScore: 92,
      verificationStatus: 'LIKELY',
      timestamp: new Date(now.getTime() - 30 * 60000).toISOString()
    }
  ],

  devTourists: [
    {
      touristId: 'TST-KHL-2026',
      idHash: '0x8f2d5a1b3c7e9f0a2b4c6d8e0f1a3b5c7d9e1f3a5b7c9d1e3f5a7b9c1d3e5f7a',
      status: 'ACTIVE',
      validFrom: new Date(now.getTime() - 86400000).toISOString(),
      validTo: new Date(now.getTime() + 6 * 86400000).toISOString()
    },
    {
      touristId: 'TST-SHL-4041',
      idHash: '0x1c3e5a7b9d0f2a4c6e8b0d2f4a6c8e0b2d4f6a8c0e2b4d6f8a0c2e4b6d8f0a2c',
      status: 'ACTIVE',
      validFrom: new Date(now.getTime() - 86400000).toISOString(),
      validTo: new Date(now.getTime() + 6 * 86400000).toISOString()
    },
    {
      touristId: 'TST-DWK-9082',
      idHash: '0x4a8f9b2c1d3e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a',
      status: 'ACTIVE',
      validFrom: new Date(now.getTime() - 86400000).toISOString(),
      validTo: new Date(now.getTime() + 6 * 86400000).toISOString()
    },
    {
      touristId: 'TST-MWS-7711',
      idHash: '0x9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b',
      status: 'ACTIVE',
      validFrom: new Date(now.getTime() - 86400000).toISOString(),
      validTo: new Date(now.getTime() + 6 * 86400000).toISOString()
    }
  ],

  devTrips: [
    {
      id: 'TRIP-101',
      touristId: 'TST-KHL-2026',
      idHash: '0x8f2d5a1b3c7e9f0a2b4c6d8e0f1a3b5c7d9e1f3a5b7c9d1e3f5a7b9c1d3e5f7a',
      plannedRouteId: 'Nongriat Double Decker Living Root Trail',
      currentZoneId: 'Nongriat Safe Corridor',
      status: 'ACTIVE',
      lat: 25.252,
      lon: 91.675,
      accuracyMeters: 4,
      batteryPercent: 88,
      riskScore: 12,
      source: 'GPS_LIVE',
      startedAt: new Date(now.getTime() - 2 * 3600000).toISOString(),
      updatedAt: new Date(now.getTime() - 60000).toISOString()
    },
    {
      id: 'TRIP-102',
      touristId: 'TST-SHL-4041',
      idHash: '0x1c3e5a7b9d0f2a4c6e8b0d2f4a6c8e0b2d4f6a8c0e2b4d6f8a0c2e4b6d8f0a2c',
      plannedRouteId: 'Mawsmai Ridge Trek',
      currentZoneId: 'Mawsmai Caution Yellow Zone',
      status: 'ACTIVE',
      lat: 25.244,
      lon: 91.726,
      accuracyMeters: 14,
      batteryPercent: 48,
      riskScore: 48,
      source: 'BLE_MESH_CACHED',
      startedAt: new Date(now.getTime() - 4 * 3600000).toISOString(),
      updatedAt: new Date(now.getTime() - 20 * 60000).toISOString()
    },
    {
      id: 'TRIP-103',
      touristId: 'TST-DWK-9082',
      idHash: '0x4a8f9b2c1d3e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a',
      plannedRouteId: 'Nohkalikai Canyon Descent',
      currentZoneId: 'Nohkalikai Red Zone',
      status: 'ACTIVE',
      lat: 25.275,
      lon: 91.685,
      accuracyMeters: 6,
      batteryPercent: 18,
      riskScore: 95,
      source: 'EMERGENCY_BLE_BEACON',
      startedAt: new Date(now.getTime() - 3 * 3600000).toISOString(),
      updatedAt: new Date(now.getTime() - 4 * 60000).toISOString()
    },
    {
      id: 'TRIP-104',
      touristId: 'TST-MWS-7711',
      idHash: '0x9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b',
      plannedRouteId: 'Cherrapunji Town & Eco Park Hub',
      currentZoneId: 'Sohra Safe Corridor',
      status: 'ACTIVE',
      lat: 25.280,
      lon: 91.730,
      accuracyMeters: 5,
      batteryPercent: 94,
      riskScore: 8,
      source: 'GPS_LIVE',
      startedAt: new Date(now.getTime() - 1 * 3600000).toISOString(),
      updatedAt: new Date(now.getTime() - 90000).toISOString()
    }
  ],

  devBreadcrumbs: {
    'TRIP-101': [
      { id: 'B-101-1', tripId: 'TRIP-101', lat: 25.246, lon: 91.668, accuracyMeters: 6, batteryPercent: 95, timestamp: new Date(now.getTime() - 45 * 60000).toISOString() },
      { id: 'B-101-2', tripId: 'TRIP-101', lat: 25.249, lon: 91.671, accuracyMeters: 5, batteryPercent: 92, timestamp: new Date(now.getTime() - 30 * 60000).toISOString() },
      { id: 'B-101-3', tripId: 'TRIP-101', lat: 25.252, lon: 91.675, accuracyMeters: 4, batteryPercent: 88, timestamp: new Date(now.getTime() - 1 * 60000).toISOString() }
    ],
    'TRIP-102': [
      { id: 'B-102-1', tripId: 'TRIP-102', lat: 25.238, lon: 91.715, accuracyMeters: 10, batteryPercent: 62, timestamp: new Date(now.getTime() - 90 * 60000).toISOString() },
      { id: 'B-102-2', tripId: 'TRIP-102', lat: 25.244, lon: 91.726, accuracyMeters: 14, batteryPercent: 48, timestamp: new Date(now.getTime() - 20 * 60000).toISOString() }
    ],
    'TRIP-103': [
      { id: 'B-103-1', tripId: 'TRIP-103', lat: 25.268, lon: 91.695, accuracyMeters: 5, batteryPercent: 60, timestamp: new Date(now.getTime() - 60 * 60000).toISOString() },
      { id: 'B-103-2', tripId: 'TRIP-103', lat: 25.272, lon: 91.690, accuracyMeters: 6, batteryPercent: 35, timestamp: new Date(now.getTime() - 30 * 60000).toISOString() },
      { id: 'B-103-3', tripId: 'TRIP-103', lat: 25.275, lon: 91.685, accuracyMeters: 6, batteryPercent: 18, timestamp: new Date(now.getTime() - 4 * 60000).toISOString() }
    ],
    'TRIP-104': [
      { id: 'B-104-1', tripId: 'TRIP-104', lat: 25.275, lon: 91.725, accuracyMeters: 6, batteryPercent: 98, timestamp: new Date(now.getTime() - 30 * 60000).toISOString() },
      { id: 'B-104-2', tripId: 'TRIP-104', lat: 25.280, lon: 91.730, accuracyMeters: 5, batteryPercent: 94, timestamp: new Date(now.getTime() - 1.5 * 60000).toISOString() }
    ]
  },

  devIncidents: [
    {
      id: 'INC-SOS-01',
      tripId: 'TRIP-103',
      touristId: 'TST-DWK-9082',
      idHash: '0x4a8f9b2c1d3e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a',
      lat: 25.275,
      lon: 91.685,
      batteryPct: 18,
      channel: 'BLE_MESH_RELAY',
      status: 'OPEN',
      riskScore: 95,
      timestamp: new Date(now.getTime() - 4 * 60000).toISOString()
    }
  ]
};
