# 🤖 AI Agent Guidelines & Architecture Rules — Project AEGIS

Welcome AI Agent! If you are pair programming with a human developer on the **AEGIS** repository, follow these rules strictly.

---

## 🏛 Project Overview & Monorepo Structure

AEGIS is an Autonomous Emergency & Geospatial Identity Safeguard System designed for high-risk, low-connectivity tourist regions (e.g., Meghalaya).

```
├── aegis-android/         # Native Android App (Kotlin, Jetpack Compose, Room DB, BLE Mesh)
├── aegis-contracts/       # Solidity Smart Contracts (Hardhat, AegisTouristID.sol, Sepolia/Amoy)
├── aegis-backend/         # Express & WebSockets API Gateway (PostGIS, Turf.js spatial engine)
├── aegis-dashboard/       # Glassmorphic React Authority Command Center (Leaflet, OpenStreetMap)
└── docs/                  # System Architecture, API Specs & Implementation Plans
```

---

## ⛔ Absolute Rules & Constraints

1. **Zero-Cost Constraint**: NEVER add or suggest paid APIs (such as Twilio SMS API, Google Maps Platform paid tiers, or paid cloud databases). Always use free, open-source alternatives (Leaflet/MapLibre + OpenStreetMap, Base64 compact native SMS payloads, free testnets like Ethereum Sepolia / Polygon Amoy, local Room DB).
2. **Privacy-First Pseudonymous Commitments**: NEVER write code that stores raw PII (Passport numbers, Aadhaar, phone numbers) on the public blockchain or server. Store ONLY `keccak256(TouristID + ":" + Salt)` cryptographic hash vouchers on-chain in `AegisTouristID.sol`.
3. **Offline-First Architecture**: Android functionality MUST compute geofences and store events locally in Room SQLite first before transmitting. Internet is treated as an optional enhancement, not a requirement.
4. **No Superficial Symptom Patches**: Always fix the root cause of build or test failures. Never swallow exceptions or comment out assertions.

---

## 🛠 Tech Stack Conventions

- **Android App**: Kotlin, Jetpack Compose, Material3, Room DB for local offline spatial evaluation.
- **Smart Contracts**: Solidity 0.8.20, Hardhat, Ethers.js.
- **Backend API**: Node.js, Express, `ws` (WebSockets), `@turf/turf` for geospatial calculations.
- **Dashboard**: React 18, Vite, Leaflet, `lucide-react`, Glassmorphism CSS system (`#090D16` dark mode background).

---

## 🚀 Running Verification Commands

- **Android Build**: `cd aegis-android && ./gradlew assembleDebug`
- **Smart Contracts Compile**: `cd aegis-contracts && npx hardhat compile`
- **Backend Server**: `cd aegis-backend && npm start`
- **Dashboard Build**: `cd aegis-dashboard && npm run build`
