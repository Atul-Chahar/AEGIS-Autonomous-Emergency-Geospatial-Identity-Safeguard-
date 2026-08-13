# 🛡️ AEGIS — Autonomous Emergency & Geospatial Identity Safeguard
> **Smart Tourist Safety Monitoring, Offline Mesh Incident Response & Blockchain Digital ID System**

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Built for Hackathon](https://img.shields.io/badge/Built%20For-24h%20Hackathon-blue.svg)](#)
[![Zero Cost Architecture](https://img.shields.io/badge/Cost-100%25%20Free%20%2F%20Open%20Source-emerald.svg)](#)

---

## 📖 Plain English Guide (For Non-CS Readers & Stakeholders)

Looking for a simple, non-technical explanation of how AEGIS works? Read our **[Plain English Master Explainer](docs/PROJECT_EXPLAINER.md)**!

---

## 📌 Problem Statement & Executive Summary

**Problem Statement**: *"Smart Tourist Safety Monitoring & Incident Response System using AI, Geo-Fencing, and Blockchain-based Digital ID"*

**AEGIS** is a privacy-preserving tourist safety and incident response ecosystem designed to operate even in remote areas with intermittent or zero cellular connectivity (e.g., Meghalaya, mountain trails, dense forests, deserts).

The core technical vision bridges **privacy-first digital identity**, **offline-first geospatial safety calculation**, **store-and-forward emergency signaling**, **P2P offline mesh relay networks**, and a **real-time authority command center**.

---

## 🌟 Top 10 Differentiating Features

1. **Privacy-Preserving Ephemeral Pseudonymous Identity Commitment (`AegisTouristID.sol`)**:
   - Ephemeral Commitment Mechanism: Stores ONLY canonical keccak256 hashes of tourist IDs (`keccak256(touristId + ":" + salt)`), trip validity timestamps, and cryptographic vouchers on Ethereum Sepolia testnet. No raw PII (Passport, Aadhaar, phone numbers) is ever stored on-chain or on server.
2. **On-Device Offline-First GNSS Geofencing**:
   - Evaluates exact GPS polygon intersections locally on-device without internet or cell connectivity.
3. **Resilient Dual-Channel Emergency SOS**:
   - WebSockets stream when online; ultra-compact Base64 SMS string payload fallback when mobile data is down.
4. **Peer-to-Peer Offline Mesh Relay Network**:
   - Google Nearby Connections API using Bluetooth Low Energy (BLE) and Wi-Fi Direct for store-and-forward relaying of emergency messages across tourist devices in complete dead zones.
5. **Multi-Factor Sensor Fusion Risk Engine**:
   - Multi-phase state machine (BEFORE, EVENT, AFTER) filtering false positives (speed bumps, bus vibration, phone drops, hard braking) using linear acceleration, gyro, GPS, and activity recognition.
6. **Smart Inactivity & Interactive Check-In Monitor**:
   - State machine verification (`NORMAL` -> `CHECK_REQUIRED` -> `USER_PROMPTED` -> `SAFE_CONFIRMED` / `NO_RESPONSE`) before escalating immobility alerts.
7. **Dynamic Sensor-Fused Hazard Mapping**:
   - Geofence danger ratings dynamically elevate based on verified hazard reports and route closures.
8. **Sybil-Resistant Hazard Confidence Engine**:
   - Multi-factor spatial/temporal evaluation engine with distinct reporter deduplication, role weighting (Authority, Verified Guide, Tourist), evidence bonuses, and maximum 2-hour window.
9. **Glassmorphic Authority Command Center**:
   - High-density dark-mode web dashboard featuring live Leaflet / MapLibre map with open-source OpenStreetMap vector tiles, animated SOS pins, and incident drawers.
10. **Geospatial Nearest-Responder Routing & Jurisdiction Auto-Detection**:
    - PostGIS / Turf.js spatial optimization matching incidents with closest appropriate rescue unit (Police, Medical, S&R) and auto-detecting district boundaries.

---

## 📂 Repository Structure & Documentation

```
├── aegis-android/         # Native Android App (Kotlin, Jetpack Compose, Room DB, BLE Mesh)
├── aegis-contracts/       # Solidity Smart Contracts (Hardhat, AegisTouristID.sol, Sepolia/Amoy)
├── aegis-backend/         # Express & WebSockets API Gateway (PostGIS, Turf.js spatial engine)
├── aegis-dashboard/       # Glassmorphic React Authority Command Center (Leaflet, OpenStreetMap)
└── docs/                  # System Architecture, API Specs & Project Explainers
    ├── PROJECT_EXPLAINER.md # Plain-English Non-Technical Guide
    ├── ARCHITECTURE.md     # Deep System Architecture & Flowcharts
    ├── API_SPECIFICATION.md# REST, WebSockets & Smart Contract Specs
    └── CONTRIBUTING.md     # Teammate Onboarding Guide
```

---

## 🚀 Quick Start Guide

### 1. Run Backend API Server
```bash
cd aegis-backend
npm install
npm start
# Server starts on http://localhost:5000 (REST + WebSockets)
```

### 2. Run Authority Command Center Web UI
```bash
cd aegis-dashboard
npm install
npm run dev
# Dashboard launches on http://localhost:5173
```

### 3. Compile Smart Contracts
```bash
cd aegis-contracts
npm install
npx hardhat compile
# Deploy script: npx hardhat run scripts/deploy.js --network sepolia
```

### 4. Build Android Tourist App
```bash
cd aegis-android
export JAVA_HOME=/path/to/jdk-21
./gradlew assembleDebug
```

---

## 📜 Smart Contract Specification (`AegisTouristID.sol`)

```solidity
contract AegisTouristID {
    struct IDVoucher {
        bytes32 idHash;
        bytes32 itineraryHash;
        uint256 validFrom;
        uint256 validTo;
        Status status;
    }
    
    function registerTripVoucher(bytes32 _idHash, bytes32 _itineraryHash, uint256 _validDays) external returns (bool);
    function verifyVoucher(bytes32 _idHash) external returns (bool isValid);
    function revokeVoucher(bytes32 _idHash, string calldata _reason) external;
}
```

---

## 🔗 GitHub Repository

[https://github.com/Atul-Chahar/AEGIS-Autonomous-Emergency-Geospatial-Identity-Safeguard-.git](https://github.com/Atul-Chahar/AEGIS-Autonomous-Emergency-Geospatial-Identity-Safeguard-.git)
