# AEGIS System Implementation Status

## 🚀 Overview

AEGIS (Autonomous Emergency & Geospatial Identity Safeguard) is an offline-first emergency response system. This status document outlines real production implementations, partial components, and preview/mocked layers.

---

## 🟢 Real Implementations (Production Ready)

1. **AEGIS Offline Peer Relay (Google Nearby Connections)**:
   - **`NearbyTransport`**: Google Nearby Connections driver implementing advertising (`Strategy.P2P_CLUSTER`), discovery, authenticated connection handshakes (`onConnectionInitiated` token authentication), and byte payload exchange (`Payload.fromBytes()`).
   - **`RelayInbox` & `RelayInboxDao`**: Room-backed local relay storage (`RelayInboxEntity`). Phone B stores incoming packets locally from Phone A. **Phone A can turn off; Phone B safely retains the packet in Room database.**
   - **`RelayOutbox`**: When Phone B connects to internet, reads stored pending relay packets from `RelayInboxDao` (ordered by `priority DESC, receivedAt ASC`) and forwards them to backend `/api/sos`.
   - **`PacketDeduplicator`**: In-memory + Room bloom filter enforcing loop prevention, hop count incrementing (`hopCount + 1`), and TTL/expiration checks. SOS packets are never duplicated indefinitely.
   - **Honest UI Integration**: UI displays `"Mesh Active"` **ONLY** when `NearbyTransport` is actively advertising or discovering. UI peer count reflects **REAL** connected Nearby devices (`activePeers.size`), eliminating mock data.

2. **AEGIS Outbox Pattern & Backend Connection**:
   - **`RescuePacket`**: Transport-independent packet model containing `packetId` (UUID), `version`, `eventType`, `priority`, `touristId`, `tripId`, `timestamp`, `latitude`, `longitude`, `locationAccuracy`, `batteryPercent`, `activityMode`, `incidentConfidence`, `latestBreadcrumbId`, `createdAt`, `hopCount`, `ttl`, `signature`, and `transportUsed`.
   - **`OutboxEntity` & `OutboxDao`**: Room-backed outbox queue storing all outgoing events as `PENDING` **FIRST** before delivery attempts.
   - **`RealEmergencyRepository`**: Dispatches SOS alerts to backend `/api/sos`. Saves `serverAckId` and `transportUsed = "HTTPS"` on server acknowledgement. Retains packet as `PENDING` for retry when offline.
   - **Backend Idempotency (`/api/sos`)**: `aegis-backend/src/server.js` checks `packetId` and returns existing incident ack without duplicating alerts on retries.
   - **`SmsFallbackAdapter`**: User-confirmed Android SMS handoff Intent adapter (`smsto:`) formatting compact SOS payloads (`SOS:TST123|25.141|91.261|85%`).
   - **Honest UI Delivery States**: Displays real states (`Sending…`, `Delivered via Internet (Ack: INC-12345)`, `Waiting for connectivity`, `SMS handoff ready`, `Failed — retrying`). Never displays fake checkmarks.

3. **AEGIS Offline Geospatial Safety Engine**:
   - **`LocationSanityChecker`**: Filters out low accuracy (>75m), impossible speed jumps (>50 m/s for vehicles, >10 m/s for pedestrian), and teleport & return spikes. **Never declares emergency from single GPS jump.**
   - **`OfflineGeofenceEngine`**: Performs local Ray-Casting Point-In-Polygon tests 100% offline. Classifies any point into `SAFE`, `CAUTION`, `HIGH_RISK`, or `UNKNOWN`.
   - **`RouteDeviationEngine`**: Computes cross-track perpendicular distance from current location fix to trek route polyline corridor. Evaluates `ON_ROUTE`, `NEAR_CORRIDOR`, `MINOR_DEVIATION`, and `CRITICAL_DEVIATION`.
   - **`SafetyCheckInManager`**: Real check-in state machine (`NORMAL` $\rightarrow$ `CHECK_REQUIRED` $\rightarrow$ `USER_PROMPTED` $\rightarrow$ `SAFE_CONFIRMED` / `NO_RESPONSE`). Wires "I'm Safe" button to actual trip state and stores a check-in event in Room SQLite (`CheckInEntity`). Honest UI state (no claiming "guardian notified" unless actually sent).
   - **`RoomSafetyZoneRepository`**: Dynamic geofence-backed repository replacing hardcoded zone status in production.

4. **AEGIS Sensor Fusion Risk Engine**:
   - **Multi-Phase State Machine**: Evaluates 30s BEFORE, impact EVENT, and 60s AFTER windows.
   - **False Positive Elimination**: Speed bumps, bus vibration, phone drops, and hard braking are filtered out and **NEVER** generate emergency candidates.
   - **Multi-Sensor Fusion**: Combines linear acceleration, accelerometer, gyroscope, rotation vector, step counter, GPS speed, and activity recognition.
   - **In-Memory Rolling Ring Buffer**: `SensorRingBuffer` holds sliding temporal windows (30s before, event, 60s after) and evicts old samples.
   - **Context Fallback**: Speed & energy fallback when Activity Recognition is `UNKNOWN`.

5. **AEGIS BlackBox (Offline Breadcrumb & Sensor Logging)**:
   - **Persistent Storage**: Room SQLite database storing `TripEntity`, `BreadcrumbEntity`, and `SensorEventChunkEntity`.
   - **Repository Pattern**: `RoomBlackBoxRepository` fully decoupled from UI layer via `BlackBoxRepository` interface.
   - **Keystore Encryption**: Android Keystore AES-256 GCM (`BlackBoxEncryptor`) encrypts sensitive locally stored sensor payloads.
   - **Foreground Service**: `TripTrackingService` displays persistent notification and records real location fixes via `FusedLocationProviderClient`, battery status via `BatteryManager`, and motion metrics via `SensorManager`.
   - **Process Recovery**: Restarts automatically (`START_STICKY`) upon process kill, guaranteeing active trips continue recording without losing unsynced breadcrumbs.
   - **UI Integration**: `HomeViewModel` exposes real active trip state, `locationText`, and `routeDeviationText`. Shows formatted coordinates or `"Location unavailable"` when no fix exists (**Zero Fake Coordinates**).

6. **Offline Local Check-In System**:
   - `RoomCheckInRepository` storing user safety check-ins locally in SQLite via `CheckInDao` and `CheckInEntity`.

7. **Risk Evaluation Engine**:
   - `RiskEvaluator` with risk score boundary mapping (0–30 Safe, 31–60 Caution, 61–100 High Risk).

8. **SOS Payload Compact Codec**:
   - `SosPayloadCodec` encoding compact Base64 SMS payloads for emergency situations without cellular data.

9. **Security & Cryptography**:
   - Keystore AES-256 GCM encryption for sensitive offline BlackBox sensor logs.

---

## 🟡 Partial / In-Progress Implementations

1. **WebSocket Gateway Sync (`data/remote`)**:
   - `NetworkModule`, `AegisApi`, and `ApiConfig` configured with configurable base URL (`http://10.0.2.2:5000` / `-PaegisBackendBaseUrl`).
   - Breadcrumb background sync worker prepared for WebSocket sync dispatch.

---

## 🔵 Preview Data (UI Preview / Temporary Demo Layer)

- **`DemoIdentityRepository`**: Used strictly as preview/demo data for initial layout testing until remote sync is fully attached. No production feature claims or status screens depend on fake success flags.

---

## 🧪 Verification & Test Coverage (51 / 51 Passed)

- Unit tests pass cleanly for:
  - `PeerRelayMeshTest` (5 tests: Phone B stores packet in RelayInbox even if Phone A turns off, deduplication prevents loops, hop count & TTL expiration, RelayOutbox internet forwarding to backend, CRITICAL emergency priority ordering)
  - `OutboxEmergencyRepositoryTest` (7 tests)
  - `LocationSanityCheckerTest` (5 tests)
  - `OfflineGeospatialEngineTest` (7 tests)
  - `SensorFusionRiskEngineTest` (10 tests)
  - `BlackBoxRepositoryTest` (7 tests)
  - `BlackBoxEncryptorTest` (2 tests)
  - `RiskEvaluatorTest` (5 tests)
  - `SosPayloadCodecTest` (3 tests)
