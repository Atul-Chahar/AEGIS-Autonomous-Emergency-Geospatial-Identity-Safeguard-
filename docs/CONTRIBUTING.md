# 🤝 AEGIS Developer Onboarding & Contribution Guide

Welcome to the AEGIS team! Follow this guide to set up your environment, understand module responsibilities, and contribute code cleanly.

---

## 🛠 Local Setup Instructions

### 1. Clone & Set Remote
```bash
git clone https://github.com/Atul-Chahar/AEGIS-Autonomous-Emergency-Geospatial-Identity-Safeguard-.git
cd AEGIS-Autonomous-Emergency-Geospatial-Identity-Safeguard-
```

### 2. Backend & WebSockets API Gateway (`aegis-backend`)
```bash
cd aegis-backend
npm install
npm start
# Server starts at http://localhost:5000
```

### 3. Authority Command Center Web Dashboard (`aegis-dashboard`)
```bash
cd aegis-dashboard
npm install
npm run dev
# Open http://localhost:5173 in browser
```

### 4. Smart Contracts (`aegis-contracts`)
```bash
cd aegis-contracts
npm install
npx hardhat compile
# Deploy script: npx hardhat run scripts/deploy.js --network sepolia
```

### 5. Native Android App (`aegis-android`)
```bash
cd aegis-android
export JAVA_HOME=/path/to/jdk-21
./gradlew assembleDebug
```

---

## 🌴 Branching & Commit Workflow

- **Branch Naming**:
  - `feature/android-offline-geofence`
  - `feature/smart-contract-sepolia`
  - `feature/dashboard-leaflet-maps`
  - `fix/websocket-reconnect`

- **Commit Message Format**:
  - `feat(android): implement local room raycasting geofence engine`
  - `feat(contracts): add autoExpire logic to AegisTouristID.sol`
  - `docs: update API specification for SMS fallback payload`

---

## 💬 Code Ownership & Responsibilities

| Teammate | Focus Area | Key Directory |
| :--- | :--- | :--- |
| **Mobile Team** | Android Jetpack Compose, Room DB, BLE Mesh Relay | `aegis-android/` |
| **Web UI Team** | React, Leaflet Maps, Glassmorphic CSS Dashboard | `aegis-dashboard/` |
| **Backend & Spatial** | Node.js, WebSockets, PostGIS, Turf.js Nearest Responder | `aegis-backend/` |
| **Blockchain Team** | Solidity, Hardhat, Sepolia / Amoy Contract Deployment | `aegis-contracts/` |
