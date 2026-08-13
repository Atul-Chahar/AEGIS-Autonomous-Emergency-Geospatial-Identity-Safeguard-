# 📡 AEGIS API & Protocol Specification

---

## 1. REST API Endpoints (`http://localhost:5000`)

### `GET /api/health`
Returns gateway operational status and active WebSocket connection count.

**Response**:
```json
{
  "status": "OK",
  "name": "AEGIS API Gateway",
  "activeSockets": 4
}
```

---

### `POST /api/identity/register`
Registers an ephemeral tourist profile and returns on-chain proof hash commitments.

**Request Payload**:
```json
{
  "name": "German Explorer",
  "passportOrAadhaar": "A12345678",
  "tripStart": "2026-08-12T00:00:00Z",
  "tripEnd": "2026-08-20T00:00:00Z",
  "route": ["Shillong", "Cherrapunji", "Dawki"],
  "emergencyContact": "+491701234567"
}
```

**Response**:
```json
{
  "success": true,
  "touristId": "TST-8F29X4",
  "idHash": "0xa7f8e32904b1c5a92d831559b6491f2419a4e7f",
  "itineraryHash": "0x53b40f4fdb39d939939fd44e81e0fc9113a597d",
  "validDays": 7,
  "qrPayload": "{\"touristId\":\"TST-8F29X4\",\"idHash\":\"0xa7f8e...\",\"expires\":\"2026-08-20\"}",
  "blockchainProof": {
    "contract": "AegisTouristID.sol",
    "network": "Ethereum Sepolia / Polygon Amoy",
    "status": "COMMITTED_ON_CHAIN"
  }
}
```

---

### `GET /api/identity/verify/:idHash`
Verifies voucher status for homestay owners, tour guides, and police checkpoints.

**Response**:
```json
{
  "valid": true,
  "idHash": "0xa7f8e32904b1c5a92d831559b6491f2419a4e7f",
  "status": "ACTIVE",
  "validTo": "2026-08-20T00:00:00Z"
}
```

---

### `GET /api/geofences`
Returns active GeoJSON polygon layers for Safe, Caution, and High Risk zones.

---

### `POST /api/sos`
Submits an emergency SOS alert via WebSockets or decoded Base64 SMS string fallback.

**Request Payload (WebSocket / HTTP)**:
```json
{
  "touristId": "TST-8F29X4",
  "idHash": "0xa7f8e32904b1c5a92d831",
  "lat": 25.145,
  "lon": 91.265,
  "batteryPct": 14,
  "channel": "WEBSOCKET"
}
```

**Request Payload (SMS Fallback)**:
```json
{
  "channel": "SMS",
  "rawSmsPayload": "U09TOlRTVC04RjI5WDR8MjUuMTQ1fDkxLjI2NXwxNCU="
}
```

---

### `POST /api/hazards`
Submits a crowdsourced hazard report. Auto-elevates polygon risk when $\ge 3$ reports match within a 500m radius.

---

### `POST /api/responders/match`
Executes spatial nearest-responder optimization.

**Request**: `{"lat": 25.145, "lon": 91.265}`

**Response**:
```json
{
  "nearestResponders": [
    {
      "id": "RES-01",
      "name": "Meghalaya S&R Unit 1",
      "type": "RESCUE",
      "distanceKm": "3.20",
      "etaMins": 8
    }
  ]
}
```

---

## 2. WebSockets Protocol (`ws://localhost:5000`)

### Outbound Events Broadcasted to Command Center:
* `EMERGENCY_SOS`: Live panic alert triggered by tourist.
* `HAZARD_ELEVATED`: Zone risk level auto-escalated by crowdsourced reports.
* `TELEMETRY_UPDATE`: Live location & risk score update.
