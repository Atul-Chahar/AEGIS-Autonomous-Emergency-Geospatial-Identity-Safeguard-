const test = require('node:test');
const assert = require('node:assert/strict');
const http = require('http');

// Start backend server in test mode
process.env.PORT = '5099';
const db = require('../src/database/pool');
const server = require('../src/server');

function makeRequest(path, method = 'GET', body = null, headers = {}) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: '127.0.0.1',
      port: 5099,
      path,
      method,
      headers: {
        'Content-Type': 'application/json',
        ...headers
      }
    };

    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', chunk => { data += chunk; });
      res.on('end', () => {
        try {
          const parsed = JSON.parse(data);
          resolve({ status: res.statusCode, data: parsed });
        } catch (e) {
          resolve({ status: res.statusCode, raw: data });
        }
      });
    });

    req.on('error', reject);

    if (body) {
      req.write(JSON.stringify(body));
    }
    req.end();
  });
}

test('AEGIS Backend Integration Tests', async (t) => {
  t.after(() => {
    server.close();
  });

  t.beforeEach(() => {
    const store = db.getStore();
    store.trips = new Map();
    store.breadcrumbs = [];
    store.safetyZones = [];
    store.responderUnits = [];
    store.hazardReports = [];
    store.hazardEvents = [];
  });

  await t.test('GET /api/health returns 200 OK and database status', async () => {
    const res = await makeRequest('/api/health');
    assert.equal(res.status, 200);
    assert.equal(res.data.status, 'OK');
    assert.equal(res.data.service, 'AEGIS API Gateway');
    assert.ok(res.data.database);
  });

  await t.test('POST /api/identity/register creates cryptographic voucher', async () => {
    const res = await makeRequest('/api/identity/register', 'POST', {
      touristId: 'TST-INTEG-001',
      salt: 'salt-999',
      validDays: 7
    });
    assert.equal(res.status, 201);
    assert.equal(res.data.success, true);
    assert.equal(res.data.touristId, 'TST-INTEG-001');
    assert.ok(res.data.idHash.startsWith('0x'));

    // Test verify voucher endpoint
    const verifyRes = await makeRequest(`/api/identity/verify/${res.data.idHash}`);
    assert.equal(verifyRes.status, 200);
    assert.equal(verifyRes.data.isValid, true);
    assert.equal(verifyRes.data.touristId, 'TST-INTEG-001');
  });

  await t.test('POST /api/sos ingests RescuePacket idempotently', async () => {
    const packetId = `INTEG-PACKET-${Date.now()}`;
    const sosPayload = {
      packetId,
      touristId: 'TST-INTEG-001',
      channel: 'HTTPS',
      lat: 25.145,
      lon: 91.265,
      batteryPct: 88
    };

    // First SOS Request
    const res1 = await makeRequest('/api/sos', 'POST', sosPayload);
    assert.equal(res1.status, 200);
    assert.equal(res1.data.success, true);
    assert.equal(res1.data.isDuplicate, false);
    const incidentId = res1.data.incidentId;

    // Retry duplicate SOS Request with same packetId
    const res2 = await makeRequest('/api/sos', 'POST', sosPayload);
    assert.equal(res2.status, 200);
    assert.equal(res2.data.success, true);
    assert.equal(res2.data.isDuplicate, true);
    assert.equal(res2.data.incidentId, incidentId);
  });

  await t.test('GET /api/trips returns empty array when backend has no active trips', async () => {
    const res = await makeRequest('/api/trips');
    assert.equal(res.status, 200);
    assert.deepEqual(res.data, []);
  });

  await t.test('GET /api/breadcrumbs/:tripId returns empty array when backend has no trail', async () => {
    const res = await makeRequest('/api/breadcrumbs/TRIP-MISSING');
    assert.equal(res.status, 200);
    assert.deepEqual(res.data, []);
  });

  await t.test('GET /api/geofences returns empty array when backend has no zones', async () => {
    const res = await makeRequest('/api/geofences');
    assert.equal(res.status, 200);
    assert.deepEqual(res.data, []);
  });

  await t.test('GET /api/responders returns empty array when backend has no responder units', async () => {
    const res = await makeRequest('/api/responders');
    assert.equal(res.status, 200);
    assert.deepEqual(res.data, []);
  });

  await t.test('POST /api/hazards handles spatial report creation', async () => {
    const res = await makeRequest('/api/hazards', 'POST', {
      hazardType: 'LANDSLIDE',
      lat: 25.26,
      lon: 91.70,
      description: 'Rockfall on trail',
      reporterId: 'TST-INTEG-001'
    });
    assert.equal(res.status, 201);
    assert.ok(res.data.hazard);
    assert.equal(res.data.hazard.hazardType, 'LANDSLIDE');
  });

  await t.test('POST /api/auth/login returns authority JWT token', async () => {
    const res = await makeRequest('/api/auth/login', 'POST', {
      username: 'admin',
      password: 'admin'
    });
    assert.equal(res.status, 200);
    assert.equal(res.data.success, true);
    assert.ok(res.data.token);
  });
});
