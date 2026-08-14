/**
 * DEVELOPMENT REFERENCE FIXTURES - CHERRAPUNJI (SOHRA) REGION FOCUS
 *
 * ⚠️ REFERENCE / CONFIGURATION DATA ONLY.
 * This file must NEVER seed live telemetry (tourists, trips, breadcrumbs,
 * incidents, hazards). Those arrive exclusively from the Android app via
 * the ingestion APIs — anything seeded here would be shown to authorities
 * as if it were real and would break the "no fake data" guarantee.
 *
 * What is legitimately reference/config data:
 *  - Geofences: the static safety-risk map of the region.
 *  - Responders: the configured rescue-unit registry (stations, capabilities).
 *  - Route segments: static responder routing network used by the
 *    RescueabilityEngine.
 */
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
  ]
};
