# AEGIS Coding-AI Implementation Prompts

## PROMPT 1 — Clean the project and create the real architecture

You are working on the existing repository `Atul-Chahar/AEGIS-Autonomous-Emergency-Geospatial-Identity-Safeguard-`.

Before changing anything:

1. Read `AGENTS.md`, `DESIGN.md`, the Android app structure, backend structure and existing tests.
2. Do NOT redesign the current Android UI. Preserve the Liquid Sage design system and current navigation/screens.
3. Treat `MockData.kt` only as temporary preview/demo data. No production screen should claim a feature is operational unless that feature actually works.
4. Do not create fake implementations or hard-coded "success" statuses.
5. Run the Android build and test tasks first and report the exact failures.

There is currently a stale `MainScreenViewModelTest.kt` referencing removed `DataRepository`, `MainScreenViewModel` and `MainScreenUiState`. Fix/remove obsolete tests correctly instead of bypassing tests.

Create a clean Android architecture with these packages:

* `data/local`
* `data/remote`
* `data/repository`
* `domain/model`
* `domain/usecase`
* `location`
* `sensors`
* `safety`
* `transport`
* `service`
* `ui`

Add only the dependencies required for the next stages:

* Room + KSP
* Google Play Services Location / Activity Recognition
* WorkManager
* OkHttp with WebSocket support
* Kotlin serialization JSON if not already available

Create configuration for a backend base URL instead of hardcoding `localhost` throughout application code.

Add appropriate Android runtime-permission infrastructure, but request permissions only when the relevant user feature is started.

Do NOT implement BLE mesh, blockchain or search AI in this task.

Deliverables:

* clean compiling project
* clean unit tests
* architecture ready for real repositories
* no production dependency on `MockData`
* short `IMPLEMENTATION_STATUS.md` describing exactly what is real, partial and still mocked

At the end run all relevant build/test commands and show me their results.

---

## PROMPT 2 — Build the real AEGIS BlackBox

Now implement the core AEGIS BlackBox inside the Android application.

Goal:

When a user taps "Start Route", the application must begin recording real trip breadcrumbs from the physical phone even if the internet is unavailable.

Use:

* `FusedLocationProviderClient` for location
* Android `SensorManager`
* battery information from the Android system
* Room for persistent offline storage
* a user-visible foreground location service while an active trip is being tracked

Create at minimum:

### `TripEntity`

Fields:

* tripId
* touristId/pseudonymous device ID
* startedAt
* endedAt
* plannedRouteId
* status

### `BreadcrumbEntity`

Fields:

* breadcrumbId UUID
* tripId
* timestamp
* latitude
* longitude
* horizontalAccuracyMeters
* altitudeMeters if available
* speedMps
* bearingDegrees
* batteryPercent
* activityMode
* locationSource
* isEstimated
* syncState

### `SensorEventChunkEntity`

Fields:

* chunkId
* tripId
* eventType
* eventTimestamp
* activityMode
* confidence
* encryptedPayload/path to encrypted payload
* createdAt

The application must:

1. Start tracking only when the user explicitly starts a route/trip.
2. Display a persistent foreground-service notification while tracking.
3. Store breadcrumbs locally when completely offline.
4. Stop tracking correctly when the trip ends.
5. Never lose unsynced breadcrumbs when the application process restarts.
6. Show the REAL last breadcrumb in the UI instead of `MockData.GNSS_FIX`.
7. Display "Location unavailable" instead of inventing coordinates if no fix exists.

Create repository interfaces so UI code does not access Room directly.

Use Android Keystore + AES-GCM to protect sensitive locally stored BlackBox payloads where appropriate. Do not invent custom cryptography.

Do not implement search prediction yet.

Add unit/instrumentation tests for:

* starting trip
* inserting breadcrumb
* reading latest breadcrumb
* app restart persistence
* ending trip
* no-location condition

At the end physically-buildable code must exist; do not return pseudocode only.

---

## PROMPT 3 — Sensor fusion and vehicle-vs-fall intelligence

Implement the AEGIS Sensor Fusion Incident Engine.

This feature MUST NOT treat a single acceleration spike as an emergency.

Use real Android inputs when available:

* linear acceleration
* accelerometer
* gyroscope
* rotation vector
* step detector/counter if available
* GPS speed
* GPS bearing
* GPS accuracy
* Activity Recognition Transition API

Create an `ActivityMode`:

* STILL
* WALKING
* RUNNING
* ON_BICYCLE
* IN_VEHICLE
* UNKNOWN

Use Android Activity Recognition as the main context hint and implement a conservative fallback when Activity Recognition is unavailable.

Create an `IncidentDetector` state machine.

It should analyse:

### BEFORE event

* current activity mode
* recent speed
* normal acceleration pattern
* recent rotation
* route context

### EVENT

* peak linear acceleration
* angular velocity
* orientation change
* speed change
* altitude change if reliable

### AFTER event

* continued movement
* post-event speed
* step activity
* prolonged inactivity
* user response

Important false-positive rules:

A vehicle speed bump must NOT become a crash just because acceleration is high.

A vehicle crash candidate should require a combination such as:

* IN_VEHICLE
* unusual impact
* abnormal rotation and/or rapid deceleration
* major speed reduction
* post-event inactivity
* optional unanswered safety check

For walking:

A fall candidate should consider:

* impact
* orientation change
* possible vertical/altitude change
* movement stopping afterward

Do not use identical thresholds for walking, running and vehicles.

Do not immediately call authorities from a medium-confidence event.

Return:

* event type
* confidence 0–1
* contributing factors
* recommended action

Possible actions:

* IGNORE
* LOG
* ASK_USER
* HIGH_RISK_CHECK
* EMERGENCY_CANDIDATE

Create a rolling in-memory sensor buffer.

Keep approximately:

* 30 seconds BEFORE a suspicious event
* event samples
* 60 seconds AFTER

If no event occurs, old high-frequency samples may be discarded.

When an event occurs, persist the relevant window into `SensorEventChunkEntity`.

Create tests using synthetic traces for:

1. normal walking
2. running
3. phone dropped while user keeps moving
4. bus/car vibration
5. speed breaker
6. hard braking
7. possible vehicle crash
8. walking fall followed by continued movement
9. walking fall followed by prolonged inactivity

The tests must specifically prove that a normal vehicle ride does not repeatedly generate emergencies.

Do not call this an ML/AI model unless an actual trained model is introduced. For now call it the Sensor Fusion Risk Engine.

---

## PROMPT 4 — GPS sanity checking, geofencing and route deviation

Implement the real offline geospatial safety engine.

Remove production reliance on hard-coded zone status.

Create:

* `LocationSanityChecker`
* `OfflineGeofenceEngine`
* `RouteDeviationEngine`
* `SafetyCheckInManager`

### GPS sanity checking

Reject or strongly down-weight suspicious location samples using:

* reported GPS accuracy
* elapsed time
* physically impossible speed
* sudden teleport and return
* disagreement with recent motion/activity context

Never declare an emergency because of one impossible GPS jump.

### Offline geofencing

Store versioned GeoJSON safety polygons locally.

The phone must be able to classify its location as:

* SAFE
* CAUTION
* HIGH_RISK
* UNKNOWN

without internet.

Perform point-in-polygon calculations locally.

### Route corridor

Represent the expected trek/route as a polyline with an allowed corridor width.

Calculate distance from current position to the planned route.

Detect meaningful route deviation while considering GPS accuracy.

### Safety check-in

Implement a real check-in state machine:

NORMAL
→ CHECK_REQUIRED
→ USER_PROMPTED
→ SAFE_CONFIRMED or NO_RESPONSE
→ risk escalation

The existing "I'm Safe" button must update the actual trip state and store a check-in event.

Do not claim "guardian notified" unless a notification was actually queued or delivered.

Wire the real current zone, route deviation and calculated risk factors into the existing UI.

Preserve the current design.

Add tests for boundary points, bad GPS, safe zone, danger zone, route deviation and unanswered check-in.

---

## PROMPT 5 — Real RescuePacket + Internet SOS

Connect the Android application to the existing AEGIS backend.

Create a transport-independent `RescuePacket`.

Fields should include at minimum:

* packetId UUID
* version
* eventType
* priority
* tourist pseudonymous ID
* tripId
* timestamp
* latitude
* longitude
* locationAccuracy
* batteryPercent
* activityMode
* incidentConfidence
* latestBreadcrumbId
* createdAt
* hopCount
* TTL
* signature
* transportUsed

Create a Room-backed `OutboxEntity`.

Every important outgoing event must be written to the outbox FIRST.

Then attempt delivery.

Implement:

ONLINE:
Outbox
→ HTTPS/backend
→ server acknowledgement
→ mark SENT

OFFLINE:
Outbox remains PENDING
→ WorkManager retries when appropriate

The existing SOS button must use the latest REAL breadcrumb and battery value.

Do not display:

* WebSocket ✓
* SMS ✓
* Mesh ✓

unless each transport really succeeded.

UI should display actual states such as:

* Sending
* Delivered via Internet
* Waiting for connectivity
* SMS handoff ready
* Relay pending
* Failed — retrying

Connect the Android app to the existing `/api/sos` backend endpoint.

Add backend acknowledgement IDs and idempotency using `packetId`, so retries cannot create duplicate emergencies.

Implement an SMS fallback adapter separately.

Prefer a user-confirmed Android SMS handoff/Intent unless the project has a valid reason and permission model for direct SMS sending.

Do not implement Nearby multi-hop yet.

Tests:

* successful HTTPS SOS
* backend unavailable
* duplicate retry
* process restart with pending SOS
* delivery acknowledgement
* no location available
* stale breadcrumb handling

---

## PROMPT 6 — Real Nearby offline relay between TWO Android phones

Implement the first REAL AEGIS offline peer relay using Google Nearby Connections.

Do NOT attempt a giant mesh first.

The milestone is:

PHONE A
→ PHONE B
→ store packet
→ Phone A can turn off
→ Phone B retains the packet

Use the latest Google Nearby Connections Android API and permissions.

Implement:

* advertising
* discovery
* authenticated connection establishment
* byte payload exchange
* connection state
* peer state
* permission handling by Android version
* foreground behavior when trip/relay mode is active

Use the `RescuePacket` created previously.

Each packet must have:

* globally unique packet ID
* origin signature
* TTL
* hop count
* created time
* expiry
* priority

Create:

* `NearbyTransport`
* `RelayInbox`
* `RelayOutbox`
* `PacketDeduplicator`

Rules:

1. Never forward the same packet indefinitely.
2. Never increment an SOS into duplicates.
3. A relay must not need to decrypt private BlackBox details that it isn't authorized to see.
4. Emergency packets have higher forwarding priority.
5. Packets expire.
6. Connection authentication must not be skipped.
7. UI must show "Mesh Active" only when the actual transport is active.
8. UI peer count must reflect real discovered/connected peers, not `MockData`.

First validation must use TWO physical Android phones.

Required demonstration:

1. disable mobile data/Wi-Fi internet on Phone A
2. create RescuePacket on Phone A
3. transfer to Phone B over Nearby
4. confirm Phone B persisted the packet
5. terminate/turn off Phone A
6. reconnect Phone B to internet
7. Phone B forwards packet to backend
8. dashboard receives incident

Do not add LoRa in this phase.

---

## PROMPT 7 — Productionize the backend

Refactor `aegis-backend/src/server.js`.

Keep API compatibility where reasonable, but replace the in-memory production state with PostgreSQL/PostGIS.

Create proper modules:

* routes
* controllers
* services
* repositories
* websocket
* validation
* auth
* database
* geospatial

Create database tables for:

* tourists/pseudonymous identities
* trips
* breadcrumbs
* incidents
* incident events
* check-ins
* hazard reports
* safety zones
* responder units
* responder capabilities
* relay packet receipts

Do not store unnecessary passport/Aadhaar data.

Add:

* request validation
* rate limiting
* structured error handling
* idempotent RescuePacket ingestion
* authenticated authority endpoints
* device/session authorization
* WebSocket authentication
* audit events
* database migrations

Replace hard-coded responder/geofence arrays with database records/seed fixtures clearly labelled development fixtures.

Keep Turf where useful and use PostGIS for persistent spatial queries.

Do not display development seed data as real production statistics.

Add backend integration tests.

---

## PROMPT 8 — Fix crowdsourced hazard validation

Replace the current "three requests within 500 m = verified" implementation.

Implement a confidence engine using:

* distinct verified/pseudonymous reporter IDs
* one effective report per reporter for the same incident window
* maximum age/window, initially 2 hours
* spatial proximity
* hazard-type similarity
* optional evidence reference
* reporter/source confidence
* trusted guide/authority weighting
* later external weather/sensor evidence

Use PostGIS spatial queries where appropriate.

Return:

* UNVERIFIED
* POSSIBLE
* LIKELY
* AUTHORITY_CONFIRMED

Do not automatically declare a severe hazard because three HTTP requests were submitted.

Prevent simple Sybil/duplicate-report attacks.

Record why the confidence score changed.

When a hazard changes a geofence or closes a route, preserve an audit event.

Add tests for:

* same reporter submitting three times
* three distinct reporters
* reports older than two hours
* reports farther than 500 m
* conflicting hazard types
* authority confirmation

---

## PROMPT 9 — Make Tourist ID and blockchain honest and real

Audit the Solidity contract, backend identity endpoint, Android Tourist ID screen and README together.

Currently the project mixes SHA-256 and keccak wording and calls the design Zero-Knowledge even though there is no ZK proof system.

Fix this.

Choose ONE canonical identity commitment specification and create test vectors so Android/backend/contract agree.

Add proper Hardhat tests for:

* register
* duplicate registration
* validity
* expiry
* revocation
* unauthorized revocation

Deploy to one testnet only for the MVP.

Store deployment configuration in environment/config files.

Never commit private keys.

Connect the backend with `ethers` to the actual deployed contract.

Backend registration must return:

* transaction hash
* contract address
* network/chain ID
* confirmed status

Only display "ON CHAIN" after the transaction is actually confirmed.

Replace `FakeQr` with a REAL QR encoder.

Create a signed offline-verifiable tourist voucher containing only the minimum necessary information:

* pseudonymous ID
* validity period
* trip/voucher ID
* issuer
* cryptographic signature
* optional blockchain commitment reference

No raw Aadhaar/passport number in QR or blockchain.

Remove every "Zero-Knowledge" claim unless an actual ZK circuit/proof/verifier is implemented.

---

## PROMPT 10 — Connect the Command Center to real data

Refactor the React AEGIS dashboard.

Do NOT redesign it first.

Remove hard-coded production-looking data such as:

* 3,492 tourists
* fixed responder totals
* fake incidents
* fake blockchain verification
* fake mesh-active status

Load real information from backend APIs.

Implement:

* initial REST hydration
* authenticated WebSocket reconnect
* live SOS
* active trips
* real risk score
* current/last-known breadcrumb
* geofences
* hazard reports
* responder availability
* delivery transport
* last-seen timestamp

For every incident show:

* last confirmed position
* location accuracy
* last known activity mode
* latest battery
* SOS/automatic event source
* incident confidence
* last successful check-in
* communication channel
* BlackBox breadcrumb availability

Add an incident state machine:

OPEN
→ ACKNOWLEDGED
→ TEAM_DISPATCHED
→ SEARCHING
→ LOCATED
→ RESOLVED

All state changes must persist on backend and be broadcast through WebSocket.

Add a BlackBox trajectory layer to the existing Leaflet map.

---

## PROMPT 11 — Build RescueGraph search probability MVP

Do this only after real BlackBox breadcrumbs are functioning.

Study LandSAR concepts, but DO NOT copy GPL-licensed LandSAR source code into this project without explicit license compatibility review.

Build an original AEGIS search-probability MVP.

Input:

* last surviving reliable breadcrumb
* breadcrumb timestamp
* last direction
* last activity mode
* speed estimate
* planned trail
* elapsed time
* safety zones
* known barriers/exclusion zones

Optional later:

* elevation
* land cover
* streams
* weather

Generate a bounded grid around the last reliable observation.

Calculate a normalized probability for each grid cell based on:

* physically reachable distance
* trail preference
* recent direction
* route destination
* barriers
* terrain cost when available

Output GeoJSON:

* search bounding area
* probability cells/heatmap
* top 3 search sectors
* explanations for major factors
* estimated search-area size

Do not present the probability as certainty.

Dashboard should display:

"Most likely search sectors"

not:

"Victim is here."

Add a measurable metric:

SEARCH AREA BEFORE BLACKBOX
vs
SEARCH AREA AFTER BLACKBOX

Use only values calculated from the current simulated/real scenario.

Add deterministic tests.

---

## PROMPT 12 — Rescueability instead of nearest responder

Replace straight-line nearest-responder selection.

Create a `RescueabilityEngine`.

Each responder has:

* location
* status
* vehicle
* medical capability
* rope/mountain capability
* water-rescue capability
* team size

Each traversable route segment can contain:

* distance
* expected travel time
* surface type
* slope/elevation cost
* road/trail
* blocked status
* current hazard
* bridge/river restriction

Calculate:

FEASIBLE ETA
+
CAPABILITY MATCH
+
CURRENT HAZARDS

rather than straight-line distance.

Required demonstration:

Responder A:
3 km away
but bridge blocked

Responder B:
7 km away
valid route
proper rescue equipment

AEGIS must correctly recommend Responder B.

Return both:

* geographically nearest
* operationally recommended

and explain why they differ.

Do not claim terrain-aware routing until actual route/terrain information participates in the calculation.

---

# FUTURE — Do not implement until Prompts 1–12 are stable

After the core system works, research these separately:

### A. LoRa / fixed trail relays

Reference ideas from HEARD, Trail Mate, Reticulum and Columba.

Target architecture:

Android
→ BLE
→ Guide/AEGIS LoRa bridge
→ LoRa trail relay
→ gateway
→ backend

### B. GNSS-denied BlackBox

Fuse:

* last GNSS point
* step detection
* rotation vector
* IMU
* barometer

to produce an ESTIMATED trajectory with explicit uncertainty.

Never present dead-reckoned position as exact GPS.

### C. Wi2SAR / rescue drone

Use the AEGIS probability sector to narrow the drone search area.

Investigate Wi-Fi/BLE phone detection and thermal/RGB vision.

Treat Wi2SAR as research/reference technology, not functionality we already possess.

### D. Threshold emergency privacy

Explore threshold/key-splitting so precise historical trajectory is unavailable during normal travel and can be decrypted only under an authorized missing-person/rescue workflow.

Do not invent custom cryptography.