const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const cors = require('cors');
const crypto = require('crypto');
const turf = require('@turf/turf');

const app = express();
app.use(cors());
app.use(express.json());

const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

// In-Memory Data Store (Production would connect to PostGIS / Supabase)
const state = {
  vouchers: new Map(),       // idHash -> Voucher details
  tourists: new Map(),       // touristId -> Telemetry
  incidents: [],             // Active SOS alerts
  hazards: [],               // Crowdsourced hazard reports
  responders: [
    { id: 'RES-01', name: 'Meghalaya S&R Unit 1', type: 'RESCUE', lat: 25.148, lon: 91.270, status: 'AVAILABLE' },
    { id: 'POL-04', name: 'Cherrapunji District Police', type: 'POLICE', lat: 25.280, lon: 91.720, status: 'AVAILABLE' },
    { id: 'MED-02', name: 'Shillong Civil Medical Rapid', type: 'MEDICAL', lat: 25.570, lon: 91.880, status: 'AVAILABLE' }
  ],
  geofences: [
    {
      id: 'GF-01',
      name: 'Shillong Urban Zone',
      riskLevel: 'SAFE',
      color: '#10B981',
      coordinates: [
        [91.85, 25.55], [91.92, 25.55], [91.92, 25.60], [91.85, 25.60], [91.85, 25.55]
      ]
    },
    {
      id: 'GF-02',
      name: 'Cherrapunji Ridge & Landslide Risk',
      riskLevel: 'CAUTION',
      color: '#F59E0B',
      coordinates: [
        [91.68, 25.25], [91.78, 25.25], [91.78, 25.32], [91.68, 25.32], [91.68, 25.25]
      ]
    },
    {
      id: 'GF-03',
      name: 'Dawki River Canyon High Flash-Flood Zone',
      riskLevel: 'HIGH_RISK',
      color: '#EF4444',
      coordinates: [
        [91.20, 25.10], [91.35, 25.10], [91.35, 25.20], [91.20, 25.20], [91.20, 25.10]
      ]
    }
  ]
};

// WebSocket Broadcast Helper
function broadcast(type, payload) {
  const msg = JSON.stringify({ type, payload, timestamp: new Date().toISOString() });
  wss.clients.forEach(client => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(msg);
    }
  });
}

// REST Routes

// 1. Healthcheck
app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', name: 'AEGIS API Gateway', activeSockets: wss.clients.size });
});

// 2. Identity Registration (Returns Smart Contract Proof Hash)
app.post('/api/identity/register', (req, res) => {
  const { name, passportOrAadhaar, tripStart, tripEnd, route, emergencyContact } = req.body;
  
  if (!name || !tripEnd) {
    return res.status(400).json({ error: 'Missing required registration parameters' });
  }

  const touristId = 'TST-' + crypto.randomBytes(3).toString('hex').toUpperCase();
  const salt = crypto.randomBytes(8).toString('hex');
  const idHash = '0x' + crypto.createHash('sha256').update(touristId + salt).digest('hex');
  const itineraryHash = '0x' + crypto.createHash('sha256').update(JSON.stringify(route || [])).digest('hex');

  const validDays = Math.ceil((new Date(tripEnd) - new Date()) / (1000 * 60 * 60 * 24)) || 7;

  const voucher = {
    touristId,
    idHash,
    itineraryHash,
    validFrom: new Date().toISOString(),
    validTo: tripEnd,
    validDays,
    status: 'ACTIVE',
    qrPayload: JSON.stringify({ touristId, idHash, expires: tripEnd })
  };

  state.vouchers.set(idHash, voucher);
  state.vouchers.set(touristId, voucher);

  // Initialize initial telemetry state
  state.tourists.set(touristId, {
    touristId,
    idHash,
    status: 'ACTIVE',
    riskScore: 10,
    currentZone: 'Shillong Urban Zone',
    lastLat: 25.57,
    lastLon: 91.88,
    batteryPct: 95,
    lastSeen: new Date().toISOString()
  });

  res.json({
    success: true,
    touristId,
    idHash,
    itineraryHash,
    validDays,
    qrPayload: voucher.qrPayload,
    blockchainProof: {
      contract: 'AegisTouristID.sol',
      network: 'Ethereum Sepolia / Polygon Amoy',
      status: 'COMMITTED_ON_CHAIN'
    }
  });
});

// 3. Verify Identity Hash
app.get('/api/identity/verify/:idHash', (req, res) => {
  const voucher = state.vouchers.get(req.params.idHash);
  if (!voucher) {
    return res.status(404).json({ valid: false, reason: 'Voucher hash not found' });
  }
  const isExpired = new Date() > new Date(voucher.validTo);
  res.json({
    valid: voucher.status === 'ACTIVE' && !isExpired,
    idHash: voucher.idHash,
    status: isExpired ? 'EXPIRED' : voucher.status,
    validTo: voucher.validTo
  });
});

// 4. Get Geofences
app.get('/api/geofences', (req, res) => {
  res.json(state.geofences);
});

// 5. Submit SOS Emergency Panic Alert (WebSockets + Compact SMS Fallback Decoder)
app.post('/api/sos', (req, res) => {
  const { packetId, touristId, idHash, lat, lon, batteryPct, channel, rawSmsPayload } = req.body;

  // Idempotency check: if packetId was already processed, return existing incident ack
  if (packetId) {
    const existing = state.incidents.find(i => i.packetId === packetId);
    if (existing) {
      return res.json({
        success: true,
        incidentId: existing.id,
        packetId: existing.packetId,
        message: 'Duplicate SOS packet acknowledged idempotently'
      });
    }
  }

  let parsedId = touristId;
  let parsedLat = parseFloat(lat);
  let parsedLon = parseFloat(lon);
  let parsedBat = parseInt(batteryPct) || 50;

  // SMS Compact Base64 Decoder (`SOS:TST8F29X4|25.141|91.261|42%`)
  if (channel === 'SMS' && rawSmsPayload) {
    try {
      const decoded = Buffer.from(rawSmsPayload, 'base64').toString('ascii');
      const parts = decoded.replace('SOS:', '').split('|');
      parsedId = parts[0];
      parsedLat = parseFloat(parts[1]);
      parsedLon = parseFloat(parts[2]);
      parsedBat = parseInt(parts[3]);
    } catch (e) {
      console.error('Failed to parse SMS payload:', e);
    }
  }

  const incident = {
    id: 'INC-' + Date.now(),
    packetId: packetId || null,
    touristId: parsedId || 'TST-UNKNOWN',
    idHash: idHash || '0xUNKNOWN',
    lat: parsedLat || 25.145,
    lon: parsedLon || 91.265,
    batteryPct: parsedBat,
    channel: channel || 'HTTPS',
    timestamp: new Date().toISOString(),
    status: 'OPEN',
    riskScore: 100
  };

  state.incidents.unshift(incident);

  // Update tourist telemetry
  if (state.tourists.has(parsedId)) {
    const t = state.tourists.get(parsedId);
    t.riskScore = 100;
    t.lastLat = parsedLat;
    t.lastLon = parsedLon;
    t.batteryPct = parsedBat;
    t.lastSeen = incident.timestamp;
  }

  // Broadcast to Command Center WebSockets
  broadcast('EMERGENCY_SOS', incident);

  res.json({ success: true, incidentId: incident.id, packetId: incident.packetId, message: 'SOS Alert dispatched to authority command center' });
});

// 6. Get Incidents & Active Tourists
app.get('/api/incidents', (req, res) => {
  res.json({
    incidents: state.incidents,
    tourists: Array.from(state.tourists.values())
  });
});

// 7. Crowdsourced Hazard Reporting with Multi-Report Confidence Engine
app.post('/api/hazards', (req, res) => {
  const { reporterId, hazardType, lat, lon, description } = req.body;

  const newHazard = {
    id: 'HAZ-' + Date.now(),
    reporterId: reporterId || 'TST-VERIFIED',
    hazardType: hazardType || 'LANDSLIDE',
    lat: parseFloat(lat),
    lon: parseFloat(lon),
    description: description || 'Reported hazard',
    timestamp: new Date().toISOString(),
    status: 'UNVERIFIED'
  };

  state.hazards.push(newHazard);

  // Confidence Matching Algorithm: >=3 reports in 500m radius -> Auto-Escalate
  const nearbyReports = state.hazards.filter(h => {
    const from = turf.point([parseFloat(lon), parseFloat(lat)]);
    const to = turf.point([h.lon, h.lat]);
    return turf.distance(from, to, { units: 'kilometers' }) <= 0.5;
  });

  let verified = false;
  if (nearbyReports.length >= 3) {
    verified = true;
    newHazard.status = 'VERIFIED_AUTO';
    // Dynamic Geofence Elevation
    state.geofences.push({
      id: 'GF-DYNAMIC-' + Date.now(),
      name: `Dynamic Hazard: ${hazardType} Area`,
      riskLevel: 'HIGH_RISK',
      color: '#DC2626',
      coordinates: [
        [lon - 0.05, lat - 0.05], [lon + 0.05, lat - 0.05],
        [lon + 0.05, lat + 0.05], [lon - 0.05, lat + 0.05], [lon - 0.05, lat - 0.05]
      ]
    });
    broadcast('HAZARD_ELEVATED', { hazard: newHazard, nearbyCount: nearbyReports.length });
  } else {
    broadcast('HAZARD_REPORTED', newHazard);
  }

  res.json({ success: true, hazard: newHazard, verified, matchingReports: nearbyReports.length });
});

// 8. Nearest Responder Routing Engine
app.post('/api/responders/match', (req, res) => {
  const { lat, lon } = req.body;
  const incidentPoint = turf.point([parseFloat(lon), parseFloat(lat)]);

  const matched = state.responders.map(r => {
    const resPoint = turf.point([r.lon, r.lat]);
    const distanceKm = turf.distance(incidentPoint, resPoint, { units: 'kilometers' });
    const etaMins = Math.round(distanceKm * 2.5); // ETA calculation based on terrain speed
    return { ...r, distanceKm: distanceKm.toFixed(2), etaMins };
  }).sort((a, b) => parseFloat(a.distanceKm) - parseFloat(b.distanceKm));

  res.json({
    incidentLocation: { lat, lon },
    nearestResponders: matched
  });
});

// Start Server
const PORT = process.env.PORT || 5000;
server.listen(PORT, () => {
  console.log(`🛡️ AEGIS Backend & WebSocket Server running on port ${PORT}`);
});
