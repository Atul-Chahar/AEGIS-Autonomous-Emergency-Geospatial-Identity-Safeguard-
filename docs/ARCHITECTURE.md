# 🏗️ AEGIS Technical Architecture & Subsystem Specification

---

## System Overview

```mermaid
graph TD
    subgraph Tourist Device (Offline-First Native Android App)
        GNSS[GNSS Satellite Fix] --> RaycastEngine[Local Ray-Casting Geofence Engine]
        RaycastEngine --> LocalRoomDB[(Local Room DB)]
        LocalRoomDB --> QRGen[Dynamic QR ID Generator]
        LocalRoomDB --> EventQueue[Store & Forward Queue]
        BLEMesh[BLE / Wi-Fi Direct Mesh Node] <--> EventQueue
    end

    subgraph Zero-Cost Dispatch & Communication Channel
        EventQueue -->|Online: WebSockets / HTTPS| Gateway[AEGIS API Gateway]
        EventQueue -->|Cellular Only: Base64 SMS| Gateway
        BLEMesh -->|Peer Hop| RelayPhone[Nearby Tourist Device with Network]
        RelayPhone --> Gateway
    end

    subgraph Backend Core Platform (Node.js + PostGIS + Turf.js)
        Gateway --> IdentitySvc[Identity & Voucher Service]
        Gateway --> RiskEngine[AI Multi-Factor Risk Engine]
        Gateway --> Dispatcher[Nearest Responder Matching Engine]
        IdentitySvc --> SmartContract[AegisTouristID.sol - Sepolia / Polygon Amoy]
    end

    subgraph Authority Command Center (React + Leaflet + OpenStreetMap)
        RiskEngine --> LiveMap[Live Incident Map Canvas]
        Dispatcher --> RescuePanel[Responder Routing & Dispatch Panel]
        IdentitySvc --> ProofWidget[On-Chain Proof Lookup Widget]
    end
```

---

## Subsystem Details

### 1. Smart Contract Architecture (`AegisTouristID.sol`)
The smart contract acts as an immutable, tamper-evident audit ledger for tourist identity vouchers without exposing PII.

```solidity
contract AegisTouristID {
    enum Status { ACTIVE, EXPIRED, REVOKED }

    struct IDVoucher {
        bytes32 idHash;             // keccak256(TouristID + Salt)
        bytes32 itineraryHash;      // keccak256(Route JSON)
        uint256 validFrom;
        uint256 validTo;
        Status status;
    }
}
```

* **Zero Knowledge Hashing**: `idHash = keccak256(TouristID + Salt)` ensures that public observers cannot reverse-engineer personal tourist identity from on-chain records.
* **Auto-Expiry**: Verification queries check `block.timestamp <= validTo`. If expired, the voucher status transitions automatically to `EXPIRED`.

---

### 2. On-Device Offline Geofencing (Ray-Casting Algorithm)
Instead of pinging remote servers, the Android app evaluates location locally against vector polygons stored in Room SQLite DB:

$$\text{Intersection Count} = \sum \text{RayCrossesEdge}(P_{\text{GPS}}, E_i)$$

If the count is odd, the tourist is inside the polygon (Safe 🟢, Caution 🟡, or High Risk 🔴). The check executes every 30 seconds locally, using 0 bytes of mobile data.

---

### 3. Dual-Channel SOS Dispatch
* **Primary Path (Online)**: JSON payload sent over persistent WebSocket connection (`ws://localhost:5000`).
* **Fallback Path (Cellular Only)**: Formatted as compact string payload:
  $$\text{Payload} = \text{Base64}\left(\text{"SOS:"} + \text{TouristID} + \text{"|"} + \text{Lat} + \text{"|"} + \text{Lon} + \text{"|"} + \text{Battery\%}\right)$$
  Dispatched via Android Native SMS Intent to authority emergency numbers at zero API cost.

---

### 4. P2P Bluetooth LE / Wi-Fi Direct Mesh Relay Network
In zero-connectivity dead zones, the Android app uses the Android Nearby Connections API.
* **Packet Hop**: An emergency SOS alert packet signed with TTL (Time-To-Live = 5) hops between peer tourist devices.
* **Store-and-Forward**: Each node buffers the packet in local storage and forwards it when a peer with network connectivity comes into range.

---

### 5. AI Multi-Factor Risk Scoring Engine
The backend continuously calculates a composite Risk Score:

$$\text{Risk Score} = w_1(\text{Route Deviation}) + w_2(\text{Inactivity}) + w_3(\text{Zone Risk}) + w_4(\text{Unanswered Check-In}) + w_5(\text{SOS})$$

| Score Range | Classification | System Action |
| :--- | :--- | :--- |
| **0 – 30** | SAFE 🟢 | Normal operation & quiet tracking |
| **31 – 60** | CAUTION 🟡 | Interactive check-in prompt on phone |
| **61 – 89** | ADVISORY 🟧 | Warning flag in Authority Command Center |
| **90 – 100+** | CRITICAL 🔴 | Immediate nearest-responder dispatch trigger |

---

### 6. Nearest Responder Spatial Matching (Turf.js)
Calculates geodesic distance and terrain-adjusted ETA:

$$\text{ETA (mins)} = \text{Distance (km)} \times \text{Terrain Speed Factor}$$

Ranks nearest Police, Search & Rescue, and Medical units automatically for 1-click authority dispatch.
