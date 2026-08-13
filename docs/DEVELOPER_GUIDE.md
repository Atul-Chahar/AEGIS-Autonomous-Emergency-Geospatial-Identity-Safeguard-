# 🛡️ AEGIS Developer Guide

> **Autonomous Emergency & Geospatial Identity Safeguard System**  
> For high-risk, low-connectivity tourist regions (Meghalaya, India)

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                    AEGIS Monorepo                                │
├────────────────┬──────────────┬───────────────┬─────────────────┤
│  aegis-android │aegis-backend │aegis-contracts│ aegis-dashboard │
│  (Kotlin/      │ (Node.js/    │ (Solidity/    │ (React/Vite/    │
│  Jetpack       │  Express/    │  Hardhat)     │  Leaflet)       │
│  Compose)      │  WebSocket)  │               │                 │
├────────────────┴──────────────┴───────────────┴─────────────────┤
│                 Ethereum Sepolia Testnet                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## Quick Start

### Prerequisites
| Component | Requirement |
|---|---|
| Android | JDK 17+, Android SDK 35, Kotlin 2.0 |
| Backend | Node.js 18+, npm |
| Contracts | Node.js 18+, Hardhat |
| Dashboard | Node.js 18+, Vite |

### Build & Test Commands
```bash
# Smart Contracts — compile + test
cd aegis-contracts && npm install && npx hardhat compile && npx hardhat test

# Backend — install + test
cd aegis-backend && npm install && npm test

# Dashboard — install + build
cd aegis-dashboard && npm install && npm run build

# Android — compile (requires JDK 17+)
cd aegis-android && ./gradlew assembleDebug

# Android — unit tests
cd aegis-android && ./gradlew test
```

---

## Component Deep Dives

### 1. aegis-android (Kotlin / Jetpack Compose)

**Package Structure:**
```
com.example.aegis/
├── AegisApplication.kt        # App entry, manual DI via AppContainer
├── AppContainer.kt            # Dependency container (swap demo ↔ real)
├── data/
│   ├── local/
│   │   ├── AegisDatabase.kt   # Room database (7 DAOs)
│   │   ├── dao/               # TripDao, BreadcrumbDao, OutboxDao, etc.
│   │   ├── entity/            # Room entities matching Prompts spec
│   │   └── security/          # BlackBoxEncryptor (AES-GCM, Android Keystore)
│   ├── remote/
│   │   ├── AegisApi.kt        # Backend REST interface (not yet implemented)
│   │   └── SmsFallbackAdapter # SMS-based SOS fallback (Intent-based, no paid API)
│   └── repository/            # Real + Demo repository implementations
├── domain/model/              # RescuePacket, Breadcrumb, Trip, SafetyZone, etc.
├── identity/
│   └── CanonicalIdentityHash  # keccak256(touristId + ":" + salt) via Bouncy Castle
├── location/
│   ├── AndroidLocationProvider # FusedLocationProviderClient wrapper
│   ├── LocationSanityChecker   # GPS spike detection, teleport filtering
│   └── LocationResult          # Sealed interface (Success/Unavailable)
├── mesh/
│   ├── NearbyTransport         # Google Nearby Connections BLE mesh relay
│   ├── RelayInbox/RelayOutbox  # Room-backed packet persistence
│   └── PacketDeduplicator      # TTL + seen-set deduplication
├── safety/
│   ├── OfflineGeofenceEngine   # Ray-casting PIP for offline zone classification
│   ├── RouteDeviationEngine    # Corridor-based route deviation detection
│   ├── SensorFusionRiskEngine  # 30s-before/60s-after impact analysis
│   ├── SafetyCheckInManager    # Periodic check-in state machine
│   └── SensorRingBuffer        # Sliding window for sensor samples
├── service/
│   ├── TripTrackingService     # Foreground service (START_STICKY)
│   ├── CheckInScheduler        # WorkManager periodic check-ins
│   ├── CheckInWorker           # WorkManager check-in execution
│   └── SosRetryWorker          # WorkManager offline SOS retry
├── qr/
│   └── QrCodeGenerator         # ZXing ISO 18004 real QR codes
└── ui/                         # Compose screens + glassmorphism components
```

**Key Design Decisions:**
- **Offline-First**: Room SQLite stores everything locally first. Internet is optional.
- **No DI Framework**: Manual `AppContainer` for explicit dependency wiring.
- **Privacy**: Only `keccak256(touristId + ":" + salt)` hashes leave the device.
- **Sensor Fusion**: 5 incident types (VEHICLE_CRASH, POTENTIAL_FALL, PHONE_DROPPED, SPEED_BUMP, HARD_BRAKING) with escalation ladder (IGNORE → LOG → ASK_USER → HIGH_RISK_CHECK → EMERGENCY_CANDIDATE).

---

### 2. aegis-backend (Node.js / Express / WebSockets)

**Module Structure:**
```
src/
├── server.js                  # Express + WS server entry
├── routes/                    # REST API routes
├── controllers/               # Request handlers
├── services/                  # Business logic (IdentityService, IncidentService)
├── repositories/              # Data access (TouristRepository, IncidentRepository)
├── database/
│   ├── pool.js                # PostgreSQL + PostGIS connection pool
│   └── migrations/            # SQL schema (11 tables)
├── geospatial/
│   ├── SearchProbabilityEngine  # 10×10 grid, Gaussian decay, trail preference
│   ├── HazardConfidenceEngine   # Sybil-resistant multi-reporter verification
│   ├── RescueabilityEngine      # Graph routing with capability matching
│   └── postgisHelper            # Haversine distance calculations
├── blockchain/
│   └── ethereumClient           # Ethers.js contract interaction
├── websocket/                   # Real-time incident push via WS
├── auth/                        # JWT authority authentication
└── validation/                  # Request validation middleware
```

**Key APIs:**
| Endpoint | Method | Description |
|---|---|---|
| `/api/health` | GET | System health check |
| `/api/identity/register` | POST | Register tourist identity (on-chain voucher) |
| `/api/identity/verify/:hash` | GET | Verify voucher validity |
| `/api/sos` | POST | Idempotent SOS packet ingestion |
| `/api/incidents` | GET | List all incidents |
| `/api/incidents/:id/status` | PATCH | Update incident state machine |
| `/api/geofences` | GET | Safety zone polygons |
| `/api/hazards` | POST | Submit hazard report |
| `/api/responders/match` | POST | Rescueability evaluation |
| `/api/search-probability` | POST | Search probability grid |

---

### 3. aegis-contracts (Solidity / Hardhat)

**Contract**: [AegisTouristID.sol](file:///home/dude/Let's_GO/aegis-contracts/contracts/AegisTouristID.sol)

**Identity Commitment Specification:**
```
On-chain: keccak256(touristId + ":" + salt)
```

| Function | Access | Description |
|---|---|---|
| `registerTripVoucher(idHash, itineraryHash, validDays)` | Public | Register a new voucher (1-90 days) |
| `verifyVoucher(idHash)` | Public | Check validity, auto-expire stale vouchers |
| `revokeVoucher(idHash, reason)` | Admin only | Manual revocation |

**Network Config** (via env vars):
```bash
SEPOLIA_RPC_URL=https://rpc.sepolia.org
PRIVATE_KEY=0x... # Your testnet deployer key
```

---

### 4. aegis-dashboard (React / Vite / Leaflet)

**Features:**
- Real-time incident map with Leaflet + OpenStreetMap
- WebSocket auto-reconnect for live updates
- Incident state machine (OPEN → ACKNOWLEDGED → TEAM_DISPATCHED → SEARCHING → LOCATED → RESOLVED)
- Hazard confidence visualization
- Rescueability evaluation panel
- Search probability sector overlay
- Identity voucher verification

**Configuration:**
```bash
VITE_API_BASE_URL=http://localhost:5000/api
VITE_WS_URL=ws://localhost:5000
```

---

## Absolute Rules for Contributors

1. **🚫 Zero-Cost**: Never add paid APIs (no Twilio, no Google Maps paid tiers, no paid DBs)
2. **🔒 Privacy-First**: Never store raw PII on-chain or server. Only `keccak256` hashes.
3. **📴 Offline-First**: Android MUST work without internet. Room SQLite first, sync later.
4. **🔧 Root Cause Fixes**: Never swallow exceptions or comment out assertions.
5. **🔐 Auth Required**: NearbyTransport connections MUST validate auth tokens.

---

## Running the Full Stack Locally

```bash
# Terminal 1: Backend
cd aegis-backend && npm start
# → Express on :5000, WebSocket on :5000

# Terminal 2: Dashboard
cd aegis-dashboard && npm run dev
# → Vite dev server on :5173

# Terminal 3: Android (physical device or emulator)
cd aegis-android && ./gradlew installDebug
```
