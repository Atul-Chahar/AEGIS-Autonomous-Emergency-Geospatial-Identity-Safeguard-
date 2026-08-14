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
* `EMERGENCY_SOS`: Live panic alert triggered by tourist. Payload is the **full incident record** (`id`, `lat`, `lon`, `touristId`, `batteryPct`, `channel`, `status`) so the dashboard can pin it immediately.
* `TRIP_STARTED`: A trip was upserted by the Android BlackBox (`POST /api/trips`).
* `BREADCRUMB_RECORDED`: A breadcrumb was appended by the Android BlackBox (`POST /api/breadcrumbs`).
* `INCIDENT_STATUS_CHANGED`: An incident advanced through the state machine (`PATCH /api/incidents/:id/status`).
* `HAZARD_EVALUATED`: A hazard report was confidence-scored by the HazardConfidenceEngine.
* `HAZARD_ELEVATED`: Zone risk level auto-escalated by crowdsourced reports.
* `TELEMETRY_UPDATE`: Live location & risk score update.

---

## 3. Authority Dashboard Tracking Contract

The web dashboard tracks active trips and emergency incidents through pseudonymous operational records only. It must never request or render raw passport numbers, Aadhaar numbers, phone numbers, emergency contacts, or identity documents.

### Existing REST Endpoints Used By Dashboard

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/trips` | List active trip sessions for operator monitoring. |
| `GET` | `/api/breadcrumbs/:tripId` | Load the selected trip's BlackBox breadcrumb trail. |
| `GET` | `/api/incidents` | Hydrate active and historical incidents. |
| `PATCH` | `/api/incidents/:id/status` | Advance the auditable incident state machine. |
| `GET` | `/api/geofences` | Render safe, caution, and high-risk geofence layers. |
| `GET` | `/api/hazards` | Render crowdsourced and authority-confirmed hazards. |
| `GET` | `/api/responders` | Render available responder units. |
| `POST` | `/api/responders/match` | Find operationally recommended responders for a location. |
| `POST` | `/api/search-probability` | Calculate estimated search sectors from last-known telemetry. |

### Android Ingestion Endpoints (Implemented)

The Android BlackBox syncs the active trip and its breadcrumbs to these idempotent endpoints; both broadcast to dashboard WebSocket clients.

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/trips` | Idempotent trip upsert from the Android BlackBox (keyed by `tripId`). Broadcasts `TRIP_STARTED`. |
| `POST` | `/api/breadcrumbs` | Idempotent breadcrumb append (keyed by `breadcrumbId`). Broadcasts `BREADCRUMB_RECORDED`. |

`POST /api/trips` body:

```json
{
  "tripId": "uuid-v4",
  "touristId": "TST-8F29X4",
  "plannedRouteId": "cherrapunji",
  "status": "ACTIVE",
  "startedAt": 1750000000000
}
```

`POST /api/breadcrumbs` body:

```json
{
  "breadcrumbId": "uuid-v4",
  "tripId": "uuid-v4",
  "touristId": "TST-8F29X4",
  "lat": 25.181,
  "lon": 91.297,
  "accuracyMeters": 5,
  "batteryPercent": 92,
  "activityMode": "WALKING",
  "timestamp": "2026-08-14T04:30:00.000Z"
}
```

### Planned Tracking Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/trips/:tripId/latest` | Return the latest known location and telemetry for one active trip. |

### Dashboard Subject Shape

```json
{
  "subjectId": "trip:TRIP-2026-MEGHALAYA",
  "tripId": "TRIP-2026-MEGHALAYA",
  "touristId": "TST-8F29X4",
  "idHash": "0x...",
  "incidentId": "INC-...",
  "status": "ACTIVE",
  "lat": 25.145,
  "lon": 91.265,
  "accuracyMeters": 6,
  "batteryPercent": 85,
  "lastSeenAt": "2026-08-14T10:00:00.000Z",
  "plannedRouteId": "cherrapunji-ridge",
  "currentZoneId": "zone-dawki-bridge",
  "riskScore": 72,
  "source": "GPS",
  "isStale": false
}
```

### Dashboard WebSocket Events

| Event | Payload |
|---|---|
| `CONNECTED` | Gateway status and auth mode. |
| `EMERGENCY_SOS` | New idempotent SOS incident. |
| `INCIDENT_STATUS_CHANGED` | Updated incident record after operator action. |
| `HAZARD_EVALUATED` | Hazard confidence result and geofence impact. |
| `TRIP_UPDATED` | Active trip metadata changed. |
| `BREADCRUMB_RECORDED` | New last-known location and telemetry for a trip. |
| `RESPONDER_UPDATED` | Responder location, availability, or capability changed. |
| `GEOFENCE_UPDATED` | Geofence risk level or boundary changed. |
| `SEARCH_PROBABILITY_UPDATED` | Updated search sectors for an incident. |

Every event must include a server timestamp. Dashboard reducers should ignore stale events when a newer record already exists.
