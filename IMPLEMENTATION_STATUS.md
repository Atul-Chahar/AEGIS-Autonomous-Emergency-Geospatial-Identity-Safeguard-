# AEGIS System Implementation Status

## 🚀 Overview

AEGIS (Autonomous Emergency & Geospatial Identity Safeguard) is an offline-first emergency response system. This status document outlines real production implementations, partial components, and preview/mocked layers.

---

## 🟢 Real Implementations (Production Ready)

1. **AEGIS RescueabilityEngine & Terrain-Aware Routing (`aegis-backend`)**:
   - **`RescueabilityEngine.js`**: Replaced straight-line nearest responder selection with a graph-based pathfinding & capability evaluation engine.
   - **Feasible Travel ETA Calculation**: Evaluates route graph segments considering distance, surface type (`PAVED`, `GRAVEL`, `DIRT_TRAIL`, `MOUNTAIN_PATH`), slope/elevation cost multipliers, and blockages (`isBlocked`, `currentHazard`, `bridgeRiverRestriction`).
   - **Capability Matching**: Evaluates `medicalCapability`, `ropeMountainCapability`, `waterRescueCapability`, `vehicle` type, and `teamSize`.
   - **Required Demonstration Scenario**:
     * **Responder A** (Cherrapunji Station): 3.0 km away geographically, BUT primary route segment `SEG-DAWKI-BRIDGE` is **BLOCKED** by landslide collapse.
     * **Responder B** (Shillong S&R Unit 2): 7.0 km away geographically, open valid route + mountain rope gear + 4WD medical ambulance.
     * **AEGIS Result**: Correctly recommends **Responder B** (`operationallyRecommended`), flags Responder A as `geographicallyNearest` but **IMPASSABLE**, and formats explicit operational divergence explanation detailing why they differ.
   - **Deterministic Test Suite (`test/rescueabilityEngine.test.js`)**: 3 unit tests covering required demonstration scenario, capability filtering, and terrain surface speed cost penalty (passing 100%).

2. **AEGIS Clean-Room Search-Probability Engine (`aegis-backend`)**:
   - **`SearchProbabilityEngine.js`**: Original, LandSAR-inspired probability evaluation engine (100% clean-room, zero GPL code copied).
   - **Bounded Multi-Factor Spatial Grid**: Generates a $10 \times 10$ cell spatial grid around the last surviving reliable location fix (`lat`, `lon`).
   - **Evaluation Factors**:
     * **Physically Reachable Distance Bound ($R_{\text{max}}$)**: Calculated from estimated speed (m/s) and elapsed time (mins). Cells beyond $R_{\text{max}}$ receive 0 probability.
     * **Gaussian Distance Decay**: Distance probability weighting within $R_{\text{max}}$.
     * **Trail Preference Multiplier**: +2.5x weight multiplier for cells intersecting or near `plannedTrail`.
     * **Directional Alignment Bias**: Gaussian angle delta decay relative to `lastDirectionDeg` (bearing).
     * **Hazard Exclusion Barriers**: Cells intersecting hazard barriers (e.g. Nohkalikai plunge cliff line) receive 0 probability.
     * **Normalized Score**: All cell probabilities are normalized to sum to 1.0 (100%).
   - **Measurable Search Area Reduction Metric**:
     * `SEARCH_AREA_BEFORE_BLACKBOX` ($km^2$ unconstrained radius) vs `SEARCH_AREA_AFTER_BLACKBOX` ($km^2$ constrained high-probability grid).
     * Evaluates % search area reduction (e.g. **87.1% reduction from 78.5 km² down to 10.1 km²**).
   - **Honest UI Presentation**: Displays **"Most likely search sectors"** (Sector Alpha, Sector Bravo, Sector Charlie) with factor explanations. **Never claims certainty or "Victim is here."**
   - **GeoJSON Output**: Exports `searchBoundingArea`, `probabilityHeatmap` (FeatureCollection), and `topSearchSectors`.
   - **Deterministic Test Suite (`test/searchProbability.test.js`)**: 5 unit tests covering normalization (sum = 1.0), trail preference, barrier exclusion, area reduction calculation, and top 3 sectors extraction (passing 100%).

3. **React AEGIS Dashboard Refactoring (`aegis-dashboard`)**:
   - **Preserved Glassmorphic UI & Layout**: Retained `#090D16` dark mode glassmorphism layout, Leaflet map, OSM tiles, styling, and visual theme.
   - **Removal of Fake / Hard-Coded Statistics**: Replaced hardcoded numbers (`3,492 tourists`, fixed responder totals, fake incidents) with live data hydrated from backend REST APIs (`/api/incidents`, `/api/geofences`, `/api/hazards`, `/api/responders`, `/api/trips`).
   - **Authenticated WebSocket Reconnect**: Auto-reconnecting WebSocket gateway handler for live `EMERGENCY_SOS`, `INCIDENT_STATUS_CHANGED`, `HAZARD_EVALUATED` events.
   - **Incident 6-State Machine**: Interactive state machine buttons (`OPEN` $\rightarrow$ `ACKNOWLEDGED` $\rightarrow$ `TEAM_DISPATCHED` $\rightarrow$ `SEARCHING` $\rightarrow$ `LOCATED` $\rightarrow$ `RESOLVED`). State changes persist via `PATCH /api/incidents/:id/status`, log `incident_events` audit trail, and broadcast over WebSockets.
   - **Mandatory Incident Telemetry Drawer**: Displays last confirmed position, location accuracy (m), activity mode (`STILL`, `WALKING`, `RUNNING`, `IN_VEHICLE`), latest battery (%), event source (`MANUAL_SOS`, `SENSOR_FUSION_IMPACT`), incident confidence rating, last check-in, communication channel (`HTTPS`, `BLE_MESH_RELAY`), and BlackBox breadcrumb availability.
   - **Leaflet BlackBox Trajectory & Search Sector Layer**: Renders GeoJSON / Leaflet `Polyline` trajectories and dashed Polygon search sectors.
   - **Rescueability Engine Integration**: Renders both `GEOGRAPHICALLY NEAREST` vs `OPERATIONALLY RECOMMENDED` units with operational divergence callout box.
   - **Vite Build**: Compiled cleanly without errors.

4. **AEGIS Identity Architecture & Smart Contract Integration (`aegis-contracts` & `aegis-backend`)**:
   - **Canonical Identity Commitment Specification**: Standardized on **`keccak256(touristId + ":" + salt)`** across Solidity smart contracts, Node.js backend, and Android Kotlin (`CanonicalIdentityHash.kt`). Verified with shared test vector (`touristId = "TST-MEGHALAYA-101"`, `salt = "AEGIS-SALT-2026"`).
   - **Removal of ZK Misnomers**: Audited and replaced all false "Zero-Knowledge" references across code, comments, UI, and documentation with accurate terminology: **"Privacy-Preserving Ephemeral Pseudonymous Identity Commitment"**.
   - **Smart Contract Test Suite (`AegisTouristID.sol`)**: 7 Hardhat tests passing 100%: `register`, `duplicate registration` (reverts), `validity`, `expiry` (timestamp jump), `revocation` by admin, and `unauthorized revocation` (reverts).
   - **Sepolia Testnet Deployment Config**: Deployment script (`scripts/deploy.js`) and network config (`hardhat.config.js`) configured for Ethereum Sepolia testnet (`11155111`) reading from environment variables with **zero committed private keys**.
   - **Backend Ethers.js Integration**: `IdentityService` interacts with `AegisTouristID.sol` via `ethers` and returns `transactionHash`, `contractAddress`, `networkChainId`, and `confirmed`.
   - **Android Real 2D QR Code Generator**: Replaced `FakeQr` with a **Pure Kotlin 2D QR Code Matrix Generator** (`QrCodeGenerator.kt`) rendering scannable QR bitmaps in `TouristIdScreen.kt`.
   - **Signed Offline-Verifiable Tourist Voucher**: Embeds `pseudonymousId`, `idHash`, `validFrom`, `validTo`, `tripId`, `issuer`, `signature`, and `blockchainRef`. **Zero raw Aadhaar / passport PII stored or embedded.**
   - **Honest UI Status**: Displays `"ON CHAIN"` **ONLY** when `confirmed == true`; otherwise displays `"PENDING ON-CHAIN SYNC"`.

5. **AEGIS Hazard Confidence Engine & Sybil Protection (`aegis-backend`)**:
   - **`HazardConfidenceEngine`**: Multi-factor hazard evaluation engine:
     * **Sybil Protection**: 1 effective report per distinct `reporterId` within time window. 3 reports from same user ID result in score 1.0 (`UNVERIFIED`).
     * **Maximum Age Filter**: Reports older than **2 hours** (120 minutes) do not contribute to confidence scoring.
     * **Spatial Distance Filter**: Reports farther than **500 meters** (0.5 km) are excluded via PostGIS `ST_DWithin` / spatial distance calculations.
     * **Hazard-Type Compatibility**: Only compatible hazard categories (e.g. `LANDSLIDE`) accumulate score. Conflicting types (`FLOOD` vs `FIRE`) do NOT accumulate confidence.
     * **Multi-Factor Source Weighting**: `AUTHORITY` / `DISPATCHER` = 10.0 (Immediately triggers `AUTHORITY_CONFIRMED`), `VERIFIED_GUIDE` = 2.5, `TOURIST` = 1.0, Photo/Sensor evidence = +0.5 bonus, Weather evidence = +1.0 bonus.
     * **Verification Status Bands**: `UNVERIFIED` (<2.0), `POSSIBLE` (2.0–4.9), `LIKELY` (5.0–9.9), `AUTHORITY_CONFIRMED` ($\ge$10.0 or explicit authority).
     * **Audit Trail**: Detailed explanation reason logged for every confidence change and route/geofence closure (`GEOFENCE_RISK_UPDATED`, `ROUTE_CLOSED`).
   - **Confidence Unit Tests (`test/hazardConfidence.test.js`)**: 6 dedicated tests covering same reporter 3x (Sybil check), 3 distinct reporters, reports older than 2 hours, reports farther than 500m, conflicting hazard types, and authority confirmation.

6. **AEGIS Backend Modular Architecture & PostGIS Persistence (`aegis-backend`)**:
   - **Modular Layer Structure**: Refactored into clean `routes`, `controllers`, `services`, `repositories`, `websocket`, `validation`, `auth`, `database`, and `geospatial` modules.
   - **PostgreSQL / PostGIS Database Migration (`001_initial_schema.sql` & `002_hazard_confidence_schema.sql`)**: 11 PostGIS database tables + `hazard_events` audit table:
     * `tourists` (**Zero raw PII stored**, pseudonymous voucher hashes `keccak256(TouristID + Salt)`), `trips`, `breadcrumbs` (`GEOMETRY(Point, 4326)`), `incidents` (unique `packet_id` constraint for idempotency), `incident_events` (audit log), `check_ins`, `hazard_reports` (confidence score & verification status), `safety_zones` (`GEOMETRY(Polygon, 4326)`), `responder_units`, `responder_capabilities`, `relay_packet_receipts`, `hazard_events`.
   - **Idempotent Ingestion**: Duplicate `packet_id` submissions to `/api/sos` return existing incident acknowledgements without creating duplicate incidents or audit records.
   - **Security & Authorization**: `express-rate-limit` rate limiting middleware, JWT authentication for authority command center endpoints, request validation, structured error handling middleware.
   - **Reference-Only Dev Fixtures**: `dev_fixtures.js` seeds **only static reference data** (safety geofences, responder-unit registry, route segments). Telemetry stores (tourists, trips, breadcrumbs, incidents, hazards) start **empty** and are filled exclusively by the Android ingestion APIs — the authority dashboard can never show a fabricated incident or a fake live tourist.
   - **Trip & Breadcrumb Ingestion**: `POST /api/trips` (idempotent trip upsert) and `POST /api/breadcrumbs` (idempotent per `breadcrumbId`) accept Android BlackBox syncs, validate coordinates, and broadcast `TRIP_STARTED` / `BREADCRUMB_RECORDED` over WebSocket.
   - **Enriched SOS broadcast**: the `EMERGENCY_SOS` WebSocket payload carries the full incident record (`id`, `lat`, `lon`, `touristId`, `batteryPct`, `channel`, `status`) so the authority dashboard can pin it on the map immediately.
   - **Backend Integration Tests**: 34 automated unit & integration tests (`npm test`) passing 100%.

7. **AEGIS Offline Peer Relay (Google Nearby Connections)**:
   - **`NearbyTransport`**: Google Nearby Connections driver implementing advertising (`Strategy.P2P_CLUSTER`), discovery, authenticated connection handshakes (`onConnectionInitiated` token authentication), and byte payload exchange (`Payload.fromBytes()`).
   - **`RelayInbox` & `RelayInboxDao`**: Room-backed local relay storage (`RelayInboxEntity`). Phone B stores incoming packets locally from Phone A. **Phone A can turn off; Phone B safely retains the packet in Room database.**
   - **`RelayOutbox`**: When Phone B connects to internet, reads stored pending relay packets from `RelayInboxDao` (ordered by `priority DESC, receivedAt ASC`) and forwards them to backend `/api/sos`.
   - **`PacketDeduplicator`**: In-memory + Room bloom filter enforcing loop prevention, hop count incrementing (`hopCount + 1`), and TTL/expiration checks. SOS packets are never duplicated indefinitely.
   - **Honest UI Integration**: UI displays `"Mesh Active"` **ONLY** when `NearbyTransport` is actively advertising or discovering. UI peer count reflects **REAL** connected Nearby devices (`activePeers.size`), eliminating mock data.

8. **AEGIS Outbox Pattern & Backend Connection**:
   - **`RescuePacket`**: Transport-independent packet model containing `packetId` (UUID), `version`, `eventType`, `priority`, `touristId`, `tripId`, `timestamp`, `latitude`, `longitude`, `locationAccuracy`, `batteryPercent`, `activityMode`, `incidentConfidence`, `latestBreadcrumbId`, `createdAt`, `hopCount`, `ttl`, `signature`, and `transportUsed`.
   - **`OutboxEntity` & `OutboxDao`**: Room-backed outbox queue storing all outgoing events as `PENDING` **FIRST** before delivery attempts.
   - **`RealEmergencyRepository`**: Dispatches SOS alerts to backend `/api/sos` through the real `OkHttpAegisApi`. Saves `serverAckId` and `transportUsed = "HTTPS"` on server acknowledgement. Retains packet as `PENDING` for retry when offline.
   - **`OkHttpAegisApi` (real HTTP client)**: `data/remote/OkHttpAegisApi.kt` implements `health`, `submitSos`, `incidents`, `identity`, `startTrip`, and `submitBreadcrumb` against `BuildConfig.AEGIS_BACKEND_BASE_URL` (default `http://10.0.2.2:5000`). `SosRetryWorker` re-dispatches failed outbox packets over HTTPS.
   - **Backend Idempotency (`/api/sos`)**: `aegis-backend` checks `packetId` and returns existing incident ack without duplicating alerts on retries.
   - **`SmsFallbackAdapter`**: User-confirmed Android SMS handoff Intent adapter (`smsto:`) formatting compact SOS payloads (`SOS:TST123|25.141|91.261|85%`).
   - **Honest UI Delivery States**: Displays real states (`Sending…`, `Delivered via Internet (Ack: INC-12345)`, `Waiting for connectivity`, `SMS handoff ready`, `Failed — retrying`). Never displays fake checkmarks.

9. **AEGIS Offline Geospatial Safety Engine**:
   - **`LocationSanityChecker`**: Filters out low accuracy (>75m), impossible speed jumps (>50 m/s for vehicles, >10 m/s for pedestrian), and teleport & return spikes. **Never declares emergency from single GPS jump.**
   - **`OfflineGeofenceEngine`**: Performs local Ray-Casting Point-In-Polygon tests 100% offline. Classifies any point into `SAFE`, `CAUTION`, `HIGH_RISK`, or `UNKNOWN`.
   - **`RouteDeviationEngine`**: Computes cross-track perpendicular distance from current location fix to trek route polyline corridor. Evaluates `ON_ROUTE`, `NEAR_CORRIDOR`, `MINOR_DEVIATION`, and `CRITICAL_DEVIATION`.
   - **`SafetyCheckInManager`**: Real check-in state machine (`NORMAL` $\rightarrow$ `CHECK_REQUIRED` $\rightarrow$ `USER_PROMPTED` $\rightarrow$ `SAFE_CONFIRMED` / `NO_RESPONSE`). Wires "I'm Safe" button to actual trip state and stores a check-in event in Room SQLite (`CheckInEntity`). Honest UI state (no claiming "guardian notified" unless actually sent).
   - **`RoomSafetyZoneRepository`**: Dynamic geofence-backed repository replacing hardcoded zone status in production.

10. **AEGIS Sensor Fusion Risk Engine**:
   - **Multi-Phase State Machine**: Evaluates 30s BEFORE, impact EVENT, and 60s AFTER windows.
   - **False Positive Elimination**: Speed bumps, bus vibration, phone drops, and hard braking are filtered out and **NEVER** generate emergency candidates.
   - **Multi-Sensor Fusion**: Combines linear acceleration, accelerometer, gyroscope, rotation vector, step counter, GPS speed, and activity recognition.
   - **In-Memory Rolling Ring Buffer**: `SensorRingBuffer` holds sliding temporal windows (30s before, event, 60s after) and evicts old samples.
   - **Context Fallback**: Speed & energy fallback when Activity Recognition is `UNKNOWN`.

11. **AEGIS BlackBox (Offline Breadcrumb & Sensor Logging)**:
   - **Persistent Storage**: Room SQLite database storing `TripEntity`, `BreadcrumbEntity`, and `SensorEventChunkEntity`.
   - **Repository Pattern**: `RoomBlackBoxRepository` fully decoupled from UI layer via `BlackBoxRepository` interface.
   - **Keystore Encryption**: Android Keystore AES-256 GCM (`BlackBoxEncryptor`) encrypts sensitive locally stored sensor payloads.
   - **Foreground Service**: `TripTrackingService` displays persistent notification and records real location fixes via **FusedLocationProvider (Play Services) with a platform `LocationManager` fallback** (GPS/network `requestSingleUpdate`) for devices without Play Services — breadcrumbs are labeled with the honest `locationSource` (`FUSED` / `GPS` / `NETWORK`). Battery via `BatteryManager`, motion via `SensorManager`.
   - **Process Recovery**: Restarts automatically (`START_STICKY`) upon process kill, guaranteeing active trips continue recording without losing unsynced breadcrumbs. Stopping a trip also resolves the active trip even if the service was freshly restarted (fixes trips stuck `ACTIVE`).
   - **Gateway Sync**: `BreadcrumbSyncWorker` pushes the active trip + pending breadcrumbs to the gateway (`POST /api/trips` + `POST /api/breadcrumbs`) — immediately on trip start (`syncNow`) and then periodically (2-min cadence, clamped by WorkManager) — then marks them `SYNCED` only after backend acknowledgement.
   - **UI Integration**: `HomeViewModel` exposes real active trip state, `locationText`, and `routeDeviationText`. Shows formatted coordinates or `"Location unavailable"` when no fix exists (**Zero Fake Coordinates**).

---

## 📱 Android App UI Wiring (aegis-android) — Post Restructure

### 🟢 Wired to Real Systems

- **Home guardian status**: `HomeViewModel.guardianState` is derived from **real state** — active trip (Room `TripEntity`), latest location fix (Room `BreadcrumbEntity`), route deviation (`RouteDeviationEngine`), and the SOS overlay visibility mirrored from `EmergencyViewModel` via `AppContainer.emergencyOverlayActive`. No hard-coded levels: SOS open → `EMERGENCY`; no trip → `LIMITED`; trip + deviation → `ATTENTION`; trip + no fix → `LIMITED`; trip + fix on-route → `ACTIVE`. Home renders it with the shared `GuardianStatePill` (same as Map / Activity / SafetyCenter).
- **Trip start is real**: `TripSetup`'s "Start Safe Journey" (centered button) starts the foreground `TripTrackingService` with the container identity, plus `NearbyTransport` advertising/discovery for the offline peer relay.
- **Runtime permissions**: starting a journey requests location **and** `POST_NOTIFICATIONS` (Android 13+) via `LocationPermissions.requiredForTrip`, so the foreground tracking notification is visible on API 33+.
- **SOS dispatch pipeline**: `EmergencyViewModel` builds the payload from the real Tourist ID + latest Room breadcrumb + battery, calls `DispatchSosUseCase` → `RealEmergencyRepository` which persists the packet in the Room **outbox as `PENDING` first**, then attempts the backend **over real HTTPS (`OkHttpAegisApi`)**. `SosOverlay` shows the honest 7-step progress (`ui/state/SosSteps.kt`): checkmarks appear only when the real state reports success (backend ack `Sent`), and offline storage surfaces as `PendingSmsFallback` with SMS/relay steps stuck `IN_PROGRESS` — never fake checkmarks. Dispatch requires a press-and-hold to reduce accidental activation.
- **Map (real OSM)**: `MapViewModel` drives `MapScreen` from **real data** — the guardian pill, destination, route summary, corridor status and live position all come from the active trip + real Room breadcrumbs. The map itself is a **real OpenStreetMap basemap (osmdroid, Mapnik tiles — the same tiles the web dashboard uses via Leaflet)** with real zone markers at zone centroids, the recorded breadcrumb trail polyline, and a live position marker. Production never renders the sample "3.6 km recorded" strings.
- **Journey BlackBox & Activity (real)**: `JourneyBlackBoxViewModel` / `ActivityViewModel` derive recording state, last-fix time, accuracy, activity, battery, stored/synced/pending breadcrumb counts and the timeline from **real Room data** (BlackBox + check-ins). No fabricated "148 breadcrumbs" or fake timeline events.
- **Zone detail**: Map zone chips open real `ZoneDetail` (Cherrapunji / Roots / **Dawki River** fallback / Nohkalikai are all resolvable via `RoomSafetyZoneRepository`), with real local check-ins stored in Room and honest SOS state.
- **Tourist ID page**: QR voucher is centered with a fixed square bitmap; the on-chain chip honestly shows `PENDING ON-CHAIN SYNC` (with an explanatory note that publication activates when the bridge is connected) until real `confirmed == true`.

### 🔵 Preview / Mock Layers (isolated to `ui/state` + demo repositories)

- **`AegisSampleState` defaults**: kept only as the composable default parameter used by `@Preview` and any screen that has not yet been wired to a ViewModel (SafetyCenter capability rows, TripSetup config rows, IncidentCheck preview). Production screens (Home, Map, Activity, JourneyBlackBox) always receive real state from their ViewModels.
- **`DemoIdentityRepository`**: still supplies the Tourist ID until the identity service is attached; the ID is used only as a local pseudonymous voucher (no on-chain claims).
- **`IncidentCheckScreen`**: an explicit **preview** of the future sensor-fusion incident check flow — intentionally not connected to live sensor state yet.

### ⚠️ Known Android Unit-Test Environment Issue

- `./gradlew testDebugUnitTest` fails with `ClassNotFoundException` for the test classes under **AGP 9.0.1's built-in Kotlin test worker**. This is a local toolchain/environment issue, not a code failure — **do not rabbit-hole on it**. `assembleDebug` and `assembleDebugAndroidTest` are the compile gates and must stay green.

---

## 🧪 Verification & Test Coverage

- Smart Contract Hardhat tests (`npx hardhat test` in `aegis-contracts`): **7 passed**.
- Backend Integration, Search Probability, Rescueability & trip/breadcrumb ingestion tests (`npm test` in `aegis-backend`): **34 passed**.
- Dashboard reducer tests (`npm test` in `aegis-dashboard`): **7 passed**; `npm run build` compiles cleanly.
- Android compile gates (`assembleDebug` + `assembleDebugAndroidTest`): **green**.
- Instrumented UI test on emulator (`connectedDebugAndroidTest` — HomeScreenTest): **1 passed, 0 failures**.
- Android unit tests (`testDebugUnitTest`): blocked by the documented AGP 9.0.1 test-worker environment issue (see below); the same tests pass when run directly against the worker classpath.
