# 🛡️ AEGIS — Autonomous Emergency & Geospatial Identity Safeguard

> **Smart Tourist Safety Monitoring · Offline Mesh Incident Response · Privacy-Preserving Blockchain Digital ID**

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Zero Cost Architecture](https://img.shields.io/badge/Cost-100%25%20Free%20%2F%20Open%20Source-emerald.svg)](#)
[![Android](https://img.shields.io/badge/Android-Kotlin%20%2F%20Compose-blue.svg)](#)
[![Backend](https://img.shields.io/badge/Backend-Node%20%2F%20Express-green.svg)](#)
[![Contracts](https://img.shields.io/badge/Contracts-Solidity%200.8.20-orange.svg)](#)

AEGIS is a **privacy-preserving tourist safety and incident-response ecosystem** built for high-risk, low-connectivity regions (e.g. Meghalaya's mountain trails, dense forests, gorges). It keeps working when there is **no internet, no cellular signal, and no grid power** — because it was designed offline-first from day one.

---

## 📖 Table of Contents

1. [What AEGIS Does](#-what-aegis-does)
2. [System Architecture](#-system-architecture)
3. [What Is Built & Working](#-what-is-built--working)
4. [What Is NOT Built (Preview / Mock / Planned)](#-what-is-not-built-preview--mock--planned)
5. [Repository Structure](#-repository-structure)
6. [Local Setup — Full Stack](#-local-setup--full-stack)
7. [Configuring the Android App for Your Network](#-configuring-the-android-app-for-your-network)
8. [End-to-End Demo Runbook](#-end-to-end-demo-runbook)
9. [The Offline BLE Mesh Relay Demo (Two Phones)](#-the-offline-ble-mesh-relay-demo-two-phones)
10. [Verification & Test Commands](#-verification--test-commands)
11. [Documentation Index](#-documentation-index)
12. [Contributing & Team Onboarding](#-contributing--team-onboarding)

---

## 🎯 What AEGIS Does

**Problem:** Tourists trekking through remote areas (Cherrapunji, Nongriat, Dawki, Nohkalikai) frequently lose cellular connectivity. If they have an accident or get lost, there is no way to alert authorities, and responders have no idea where to search.

**AEGIS solves this with four connected layers:**

1. **The tourist app (Android)** records the journey locally — GPS breadcrumbs, check-ins, deviations — *first in an on-device database*, then syncs when connectivity allows. An SOS button works **even with zero internet**: the emergency is stored locally, handed to any nearby phone via **BLE mesh**, and retried over the internet whenever a connection appears.
2. **A real-time authority dashboard (Web)** lets rescue operators see every active tourist, every SOS incident pinned on an OpenStreetMap map, live breadcrumb trails, battery health, and run the incident through a 6-state rescue lifecycle — all from **real backend data, never fabricated**.
3. **A Node.js/Express gateway (backend)** ingests trips, breadcrumbs, and SOS packets; runs geospatial engines (route deviation, hazard confidence, responder matching, search probability); and broadcasts live events over WebSockets.
4. **Solidity smart contracts** store **only cryptographic commitments** — `keccak256(touristId + ":" + salt)` — on Ethereum Sepolia / Polygon Amoy testnets. **No passport numbers, Aadhaar, or phone numbers ever leave the device in raw form.**

---

## 🏛️ System Architecture

```
┌─────────────────────┐        ┌──────────────────────────────┐
│   Android App       │        │   Express Gateway (:5000)    │
│  (Kotlin/Compose)   │───────▶│  REST + WebSockets           │
│                     │  HTTPS │  - Trip/Breadcrumb ingestion │
│  Room DB (offline)  │        │  - SOS ingestion             │
│  TripTrackingSvc    │        │  - HazardConfidenceEngine    │
│  SOS outbox+retry   │        │  - RescueabilityEngine       │
│  BLE mesh relay     │        │  - SearchProbabilityEngine   │
└────────┬────────────┘        └──────────────┬───────────────┘
         │ BLE Nearby Connections             │ WS live events
         ▼                                    ▼
┌─────────────────────┐        ┌──────────────────────────────┐
│  Another phone      │        │  React Dashboard (:5173)     │
│  (relay hop w/ net) │        │  Leaflet + OpenStreetMap     │
└─────────────────────┘        │  Live SOS pins, trails,      │
                               │  incident state machine      │
┌─────────────────────┐        └──────────────────────────────┘
│  AegisTouristID.sol │              ▲
│  Sepolia / Amoy     │              │ Ethers.js
│  keccak256 vouchers │  ────────────┘
└─────────────────────┘
```

**Data flow at a glance:**

| Stage | What happens | Where |
|---|---|---|
| 1. Journey starts | Foreground `TripTrackingService` starts, permissions requested (location, notifications, Bluetooth, activity) | Android |
| 2. Breadcrumbs | GPS fixes recorded to **Room SQLite first**, labeled `GPS`/`FUSED` | Android |
| 3. Sync | `BreadcrumbSyncWorker` pushes trip + breadcrumbs to `/api/trips`, `/api/breadcrumbs`; marks them `SYNCED` only after backend ack | Android → Backend |
| 4. Live view | Backend broadcasts `TRIP_STARTED` / `BREADCRUMB_RECORDED`; dashboard renders trail | Backend → Dashboard |
| 5. SOS | Press-and-hold → packet written to Room **outbox as PENDING first** → tries HTTPS → broadcasts to BLE peers → returns honest offline status | Android |
| 6. Relay | Neighbor phone stores packet in Room relay inbox, flushes to `/api/sos` as `BLE_MESH_RELAY` when online | Android → Backend |
| 7. Incident | Backend saves incident (+ tourist `idHash`), broadcasts `EMERGENCY_SOS` with full geo payload | Backend |
| 8. Response | Authorities advance `OPEN → ACKNOWLEDGED → TEAM_DISPATCHED → SEARCHING → LOCATED → RESOLVED`, match responders, view search sectors | Dashboard |

---

## ✅ What Is Built & Working

### 1. Android App (`aegis-android`) — REAL

| Capability | Status |
|---|---|
| Offline-first BlackBox (Room DB): trips, breadcrumbs, sensor chunks | ✅ Real |
| Foreground trip tracking service with real GPS breadcrumbs | ✅ Real |
| Immediate + periodic breadcrumb sync to gateway (idempotent, ack-marked `SYNCED`) | ✅ Real |
| **Unique per-install tourist ID** (no more shared demo ID) + auto-registration with gateway | ✅ Real |
| Real OSM map (osmdroid Mapnik tiles) with zone markers, live trail, position | ✅ Real |
| SOS press-and-hold dispatch: outbox-first, HTTPS, honest 7-step progress | ✅ Real |
| Offline BLE peer relay (Google Nearby Connections): advertise, discover, relay with real lat/lon | ✅ Real |
| SOS retry worker + relay flush on connectivity return | ✅ Real |
| Route deviation detection (real breadcrumb vs route, honest "far from trail") | ✅ Real |
| Activity recognition + check-in scheduler | ✅ Real |
| Permission flow: location, notifications, Bluetooth, WiFi-nearby, activity — all requested at trip start | ✅ Real |

### 2. Backend Gateway (`aegis-backend`) — REAL

| Capability | Status |
|---|---|
| Trip & breadcrumb ingestion (`POST /api/trips`, `POST /api/breadcrumbs`) | ✅ Real |
| SOS ingestion with **tourist idHash linkage** | ✅ Real |
| Incident 6-state machine with audit log | ✅ Real |
| WebSocket live broadcast (`EMERGENCY_SOS`, `TRIP_STARTED`, `BREADCRUMB_RECORDED`, …) | ✅ Real |
| HazardConfidenceEngine (Sybil protection, multi-factor scoring) | ✅ Real |
| RescueabilityEngine (terrain-aware routing, capability matching) | ✅ Real |
| SearchProbabilityEngine (LandSAR-inspired grid, area reduction) | ✅ Real |
| Identity registration → keccak256 voucher + verify endpoint | ✅ Real |
| **Zero seeded telemetry** — all trips/incidents/breadcrumbs come from real devices | ✅ Real |
| PostGIS-ready (auto-falls back to in-memory store when no DB) | ✅ Real |

### 3. Authority Dashboard (`aegis-dashboard`) — REAL

| Capability | Status |
|---|---|
| Live REST hydration + auto-reconnecting WebSocket stream | ✅ Real |
| Leaflet + OpenStreetMap with **map bounds covering the full sector (Cherrapunji + Dawki)** so SOS pins are always visible | ✅ Real |
| SOS markers with pulsing beacon + accuracy circles | ✅ Real |
| Breadcrumb trail polylines, geofence polygons, hazard/responder markers | ✅ Real |
| Incident telemetry drawer (position, accuracy, battery, channel, idHash, risk index) | ✅ Real |
| 6-state incident lifecycle buttons (persisted via `PATCH /api/incidents/:id/status`) | ✅ Real |
| Rescueability match + search-probability heatmap layers | ✅ Real |
| Voucher verifier against backend | ✅ Real |
| **No fake data**: no hardcoded stats, no fake batteries/routes/zones — honest `--`/`Not specified` when telemetry is missing | ✅ Real |

### 4. Smart Contracts (`aegis-contracts`) — REAL (testnet)

| Capability | Status |
|---|---|
| `AegisTouristID.sol` — register / verify / revoke vouchers | ✅ Real |
| 7 Hardhat tests passing (register, duplicate revert, expiry, revocation) | ✅ Real |
| Sepolia/Amoy deployment config (env-driven, no committed keys) | ✅ Real |
| Backend Ethers.js integration + deterministic local commitment fallback | ✅ Real |
| **Actual on-chain publication requires Sepolia RPC + funded key** — without it the backend returns a deterministic hash with `confirmed` handling | ⚠️ Partial (env-gated) |

---

## ❌ What Is NOT Built (Preview / Mock / Planned)

Being honest about the boundaries — nothing here claims to work when it doesn't:

| Area | Current state |
|---|---|
| **Raw PII anywhere** | Never. Only `keccak256(touristId:salt)` vouchers. This is a hard design rule, not a missing feature. |
| **On-chain voucher publication** | The app registers with the gateway; actual Sepolia tx needs `SEPOLIA_RPC_URL` + `PRIVATE_KEY` env vars. Without them, the backend returns a deterministic local commitment (`confirmed` honored in UI as "PENDING ON-CHAIN SYNC"). |
| **SMS fallback channel** | Payload formatting exists (`SmsFallbackAdapter`), the UI honestly shows "SMS fallback ◌" as not-yet-delivered; no real SMS gateway is wired (zero-cost rule — no paid SMS APIs). |
| **Preview-only screens/components** | `AegisSampleState` defaults are used strictly for `@Preview` composables and isolated screens; the live screens are driven by real ViewModels. |
| **`DemoSafetyZoneRepository` in previews** | Only in `@Preview` functions, never in the real navigation graph. |
| **`/api/trips/:tripId/latest`** | Documented as planned; the dashboard currently derives latest telemetry from the breadcrumb trail. |
| **Google Play Services on emulator** | The `aegis` AVD has no Play Services; the app falls back to the platform GPS provider (also good for cheap real devices). |
| **PostGIS in production** | Fully supported (migrations + `DATABASE_URL`), but the dev fallback is an in-memory spatial store so you can run with zero infrastructure. |
| **Android unit tests via `testDebugUnitTest`** | AGP 9.0.1's built-in Kotlin test worker throws `ClassNotFoundException` for test classes in this environment — documented, not rabbit-holed. `assembleDebug`, `assembleDebugAndroidTest`, and the instrumented test suite all pass. |

---

## 📂 Repository Structure

```
AEGIS/
├── aegis-android/            # Native Android app (Kotlin, Jetpack Compose, Room, osmdroid, BLE mesh)
│   └── app/src/main/java/com/example/aegis/
│       ├── AppContainer.kt        # Manual DI: real repositories wired to Room + OkHttp
│       ├── Navigation.kt          # Compose navigation (navigation3), SOS overlay
│       ├── data/                  # Room DB, OkHttp API, real repositories
│       ├── mesh/                  # NearbyTransport, RelayInbox/Outbox, PacketDeduplicator
│       ├── service/               # TripTrackingService, BreadcrumbSyncWorker, SosRetryWorker
│       ├── ui/                    # Home, Map, SOS, Activity, ID, Safety, TripSetup, ZoneDetail
│       └── location/              # AndroidLocationProvider, LocationPermissions
├── aegis-backend/            # Express + WebSocket gateway (PostGIS-ready, in-memory fallback)
│   └── src/
│       ├── controllers/           # REST handlers
│       ├── services/              # Incident, Identity, Hazard, Rescueability, SearchProbability
│       ├── repositories/          # Trip, Breadcrumb, Incident, Tourist, Hazard, Responder, Zone
│       ├── websocket/             # WS broadcaster
│       ├── blockchain/            # Ethers.js client (Sepolia)
│       └── database/              # pool + migrations + seeds (reference data only)
├── aegis-dashboard/          # React 18 + Vite authority command center (Leaflet/OSM)
│   └── src/
│       ├── App.jsx               # Hydration, WS listener, nav views
│       ├── state/dashboardReducer.js  # PII-stripping, subject derivation, live upserts
│       └── components/           # GeospatialMap, SubjectDetailDrawer, Sidebar, TopBar, …
├── aegis-contracts/          # Solidity 0.8.20 + Hardhat (Sepolia/Amoy)
└── docs/                     # Architecture, API spec, developer guide, status, explainer
```

---

## 🚀 Local Setup — Full Stack

### Prerequisites

- **Node.js 18+** and `npm`
- **JDK 17** (the project targets Java 17; AGP 9 works with JDK 17)
- **Android SDK** with platform 36 + `platform-tools` (`adb`)
- Optional: an Android emulator (AVD `aegis`, API 35) or a physical phone
- Optional: a local **PostgreSQL/PostGIS** — the backend runs fine without it

### 1. Backend Gateway (`:5000`)

```bash
cd aegis-backend
npm install
npm start
# → http://localhost:5000   (REST + WebSockets)
```

Optional PostGIS (skip for the zero-infra demo):

```bash
export DATABASE_URL=postgres://postgres:postgres@localhost:5432/aegis_db
# migrations live in src/database/migrations
```

**Zero-config behavior:** without a database, the gateway runs on an in-memory spatial store seeded with **reference data only** (safety geofences + responder registry). Telemetry (trips, incidents, breadcrumbs, hazards) starts empty and is filled by real devices. Verify: `curl http://localhost:5000/api/health`.

### 2. Authority Dashboard (`:5173`)

```bash
cd aegis-dashboard
npm install
npm run dev
# → http://localhost:5173
```

Open it and you should see the map with Cherrapunji/Dawki geofences and the responder units. Incident/trip data appears live as devices connect.

### 3. Smart Contracts (optional, testnet)

```bash
cd aegis-contracts
npm install
npx hardhat compile        # compile AegisTouristID.sol
npx hardhat test           # 7 tests
# Deploy (requires env): SEPOLIA_RPC_URL, PRIVATE_KEY, AEGIS_CONTRACT_ADDRESS
npx hardhat run scripts/deploy.js --network sepolia
```

### 4. Android App

```bash
cd aegis-android
JAVA_HOME=/path/to/jdk-17 ./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

**Emulator install:**

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The debug build defaults to `http://10.0.2.2:5000` (the emulator alias for your machine's localhost), so emulator + backend work with **zero configuration**.

---

## 📱 Configuring the Android App for Your Network

The backend URL is a Gradle property — **never hardcoded**:

```bash
# Emulator (default, no flag needed): http://10.0.2.2:5000
cd aegis-android
./gradlew assembleDebug

# Physical phone: point at your machine's LAN IP (backend listens on 0.0.0.0:5000)
./gradlew assembleDebug -PaegisBackendBaseUrl=http://<YOUR-LAN-IP>:5000
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

> Find your LAN IP: `hostname -I` (Linux/macOS) or `ipconfig` (Windows). Phone and computer must be on the **same network** (and the router must not have AP isolation enabled).

---

## 🎬 End-to-End Demo Runbook

1. **Start the backend:** `cd aegis-backend && npm start`
2. **Start the dashboard:** `cd aegis-dashboard && npm run dev` → open `http://localhost:5173`
3. **Boot the emulator** (`aegis` AVD) or plug in a phone, install the debug APK.
4. In the app: **Start Safe Journey** → grant **Location**, **Notifications**, **Bluetooth**, **Activity** when prompted → review → **START SAFE JOURNEY**.
5. Home shows the **real tracking card** (Guardian status from real Room data — ACTIVE/ATTENTION/LIMITED, never fake).
6. **Map tab** shows the real OSM basemap with zone circles and your live breadcrumb trail.
7. On the dashboard, the trip appears live (green subject + trail), select it to see telemetry.
8. In the app tap **🚨 SOS** and **press-and-hold**: the honest 7-step progress runs ("Emergency recorded ✓, Location locked ✓, BlackBox ✓", then internet/relay/authority steps reflecting real state).
9. The incident appears on the dashboard as a **pulsing red SOS pin** with the tourist's unique ID and idHash. Advance the state machine (OPEN → … → RESOLVED).
10. Clear/wipe the app data and reinstall on a second phone → it gets its **own unique tourist ID** (each device is distinct).

> **No fake data guarantee:** every trip, breadcrumb, and incident on the dashboard originated from a real device action. The backend seeds zero telemetry.

---

## 📡 The Offline BLE Mesh Relay Demo (Two Phones)

This is the flagship "no internet" demo:

1. Install the APK on **two phones** (both pointed at your LAN backend).
2. Start a journey on **both** (this arms BLE advertising/discovery + permissions).
3. Keep them within a few metres so they pair over BLE.
4. On **Phone A** (the one that will go offline): turn on **Airplane Mode** — keep **Bluetooth** on.
5. On Phone A: **🚨 SOS → press-and-hold**. Steps show "Emergency recorded ✓, Location locked ✓" and then honestly: "Sending via internet ✕" → "Searching for offline relay ◌".
6. Phone A broadcasts the packet over BLE → **Phone B receives it** (stored in Room) and, since B has internet, `SosRetryWorker` flushes it to `/api/sos` with **channel `BLE_MESH_RELAY`** and Phone A's **real coordinates**.
7. The incident appears on the dashboard with a map pin — delivered via the relay chain, no internet on the source phone.

> **Emulator caveat:** the emulator cannot form a BLE mesh with itself; the relay chain is verified against the backend (relay ingestion with real coords + WS broadcast), but the radio link needs two physical phones.

---

## 🧪 Verification & Test Commands

```bash
# Backend (34 tests)
cd aegis-backend && npm test

# Dashboard (7 tests + production build)
cd aegis-dashboard && npm test && npm run build

# Smart contracts (7 tests)
cd aegis-contracts && npx hardhat test

# Android — assemble + instrumented-test APK compile
cd aegis-android
JAVA_HOME=/path/to/jdk-17 ./gradlew assembleDebug
JAVA_HOME=/path/to/jdk-17 ./gradlew assembleDebugAndroidTest

# Android — instrumented UI test on a running emulator
JAVA_HOME=/path/to/jdk-17 ./gradlew connectedDebugAndroidTest
```

> **Known environment note:** `./gradlew testDebugUnitTest` fails on this setup because AGP 9.0.1's built-in Kotlin test worker throws `ClassNotFoundException` for test classes. It is a toolchain issue, not a code issue — the androidTest instrumented suite passes on the emulator.

---

## 📚 Documentation Index

| Doc | Contents |
|---|---|
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Data flow, authenticity guarantees, component interactions |
| [`docs/API_SPECIFICATION.md`](docs/API_SPECIFICATION.md) | REST + WebSocket + smart-contract protocol spec |
| [`docs/DEVELOPER_GUIDE.md`](docs/DEVELOPER_GUIDE.md) | Deep component dives, E2E runbook, two-phone relay demo |
| [`docs/PROJECT_EXPLAINER.md`](docs/PROJECT_EXPLAINER.md) | Plain-English, non-technical explainer |
| [`IMPLEMENTATION_STATUS.md`](IMPLEMENTATION_STATUS.md) | Real vs preview/mock layer inventory |
| [`docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md) | Original build plan |
| [`docs/CONTRIBUTING.md`](docs/CONTRIBUTING.md) | Team onboarding & conventions |
| [`AGENTS.md`](AGENTS.md) | AI-agent & architecture rules (monorepo conventions) |

---

## 🛠️ Tech Stack

| Layer | Tech |
|---|---|
| Android | Kotlin, Jetpack Compose, Material3, Room, osmdroid (OSM), Google Nearby Connections, WorkManager, OkHttp |
| Backend | Node.js, Express, `ws`, `@turf/turf`, Ethers.js, PostGIS (optional), express-rate-limit |
| Dashboard | React 18, Vite, Leaflet, lucide-react, glassmorphism CSS |
| Contracts | Solidity 0.8.20, Hardhat, Sepolia / Polygon Amoy |

---

## 🤝 Contributing & Team Onboarding

Read [`docs/CONTRIBUTING.md`](docs/CONTRIBUTING.md) for teammate onboarding, and `AGENTS.md` for the absolute rules:

- **Zero-cost constraint** — no paid APIs (Twilio, Google Maps paid tiers, paid DBs). Free/open-source only.
- **Privacy-first** — never store raw PII on-chain or on the server; only `keccak256(touristId:salt)` vouchers.
- **Offline-first** — compute geofences and store events locally first; internet is optional.
- **No superficial patches** — fix root causes, never swallow exceptions or fake success.

---

## 🔗 GitHub Repository

[https://github.com/Atul-Chahar/AEGIS-Autonomous-Emergency-Geospatial-Identity-Safeguard-](https://github.com/Atul-Chahar/AEGIS-Autonomous-Emergency-Geospatial-Identity-Safeguard-)

---

**License:** MIT
