# AEGIS Architecture — Data Flow & Component Interactions

## Authority Dashboard User Tracking Plan

The web command center should become the operator-facing live tracking surface for active tourist trips and emergency incidents. Tracking is bounded to active trip monitoring and emergency response, and all UI/API contracts remain privacy-first: raw PII stays off the dashboard and off-chain.

```mermaid
flowchart LR
    A["Android active trip"] -->|"breadcrumb sync"| B["Express API"]
    C["Android SOS / BLE relay"] -->|"RescuePacket"| B
    B --> D["PostGIS / in-memory dev store"]
    B -->|"REST hydration"| E["React dashboard"]
    B -->|"WebSocket events"| E
    E -->|"incident state updates"| B
    E -->|"responder match + search probability"| B
```

Dashboard operational layers:

| Layer | Source | Dashboard usage |
|---|---|---|
| Active subjects | `GET /api/trips` plus latest breadcrumb | Show every monitored active trip, not only SOS cases. |
| Breadcrumb trail | `GET /api/breadcrumbs/:tripId` | Render selected trip BlackBox trajectory. |
| Incidents | `GET /api/incidents` and `EMERGENCY_SOS` | Show emergency markers and state machine actions. |
| Geofences | `GET /api/geofences` | Render safe, caution, and high-risk polygons. |
| Hazards | `GET /api/hazards` and hazard events | Show reported or confirmed route risks. |
| Responders | `GET /api/responders` and `POST /api/responders/match` | Show available units and recommended dispatch. |
| Search sectors | `POST /api/search-probability` | Estimate likely search sectors from last-known telemetry. |

The detailed implementation plan is maintained in `docs/superpowers/plans/2026-08-14-web-dashboard-user-tracking.md`.

## SOS Emergency Flow

```mermaid
sequenceDiagram
    participant User as Tourist Android
    participant SF as SensorFusionRiskEngine
    participant BB as BlackBox Room DB
    participant ER as EmergencyRepository
    participant OB as Outbox Room DB
    participant NT as NearbyTransport BLE
    participant WM as WorkManager
    participant BE as Backend Express
    participant WS as WebSocket
    participant DB as Dashboard React

    User->>SF: Sensor event detected
    SF->>SF: analyzeWindow 30s before 60s after
    SF-->>User: EMERGENCY_CANDIDATE

    User->>ER: dispatchSos SosRequest
    ER->>BB: Get latest breadcrumb and active trip
    ER->>ER: Build RescuePacket
    ER->>OB: INSERT outbox status PENDING

    alt HTTPS Available
        ER->>BE: POST /api/sos RescuePacket
        BE->>BE: Idempotent ingestion packet_id
        BE-->>ER: success incidentId
        ER->>OB: UPDATE status SENT serverAckId
        BE->>WS: Broadcast incident
        WS->>DB: Real-time alert
    else OFFLINE
        ER->>OB: UPDATE status FAILED
        ER->>WM: SosRetryWorker enqueueRetry
        ER-->>User: PendingSmsFallback SMS payload ready
        User->>NT: broadcastRescuePacket
        NT->>NT: Nearby Connections broadcast
    end

    WM->>WM: Wait for NetworkType CONNECTED
    WM->>OB: getPendingPackets and getFailedPackets
    WM->>BE: Retry HTTPS delivery
```

## Identity Registration Flow

```mermaid
sequenceDiagram
    participant App as Android App
    participant Hash as CanonicalIdentityHash
    participant BE as Backend
    participant EC as EthereumClient
    participant SC as AegisTouristID sol

    App->>Hash: computeCanonicalHash touristId salt
    Hash->>Hash: Bouncy Castle Keccak-256
    Hash-->>App: 0x 32-byte hex

    App->>BE: POST /api/identity/register
    BE->>EC: computeCanonicalHash touristId salt
    EC->>EC: ethers keccak256 toUtf8Bytes
    BE->>SC: registerTripVoucher idHash itineraryHash days
    SC-->>BE: tx receipt
    BE-->>App: idHash txHash contractAddress
```

## Sensor Fusion Decision Tree

```mermaid
flowchart TD
    A["Sensor Event"] --> B{"Peak linear acceleration?"}
    B -->|"lt 15 m/s2"| C["IGNORE"]
    B -->|"gte 15 m/s2"| D{"Activity Mode?"}
    D -->|"IN_VEHICLE"| E{"Gyro rotation magnitude?"}
    E -->|"lt 1.5 rad/s"| F{"Speed drop?"}
    F -->|"Gradual"| G["SPEED_BUMP - IGNORE"]
    F -->|"Sharp gt 8 m/s"| H["HARD_BRAKING - LOG"]
    E -->|"gte 1.5 rad/s"| I{"Post-event motion?"}
    I -->|"Speed 0 for 60s"| J["VEHICLE_CRASH - EMERGENCY"]
    I -->|"Speed recovers"| K["HARD_BRAKING - LOG"]
    D -->|"WALKING"| L{"Orientation changed?"}
    L -->|"Phone flat, user walks"| M["PHONE_DROPPED - IGNORE"]
    L -->|"User flat, no steps"| N{"Duration motionless?"}
    N -->|"lt 15s then moves"| O["POTENTIAL_FALL - ASK_USER"]
    N -->|"gt 60s motionless"| P["POTENTIAL_FALL - HIGH_RISK_CHECK"]
```

## BLE Mesh Relay Protocol

```mermaid
flowchart LR
    A["Device A SOS Origin"] -->|"RescuePacket hopCount 0"| B["Device B Relay"]
    B -->|"hopCount 1"| C["Device C Relay"]
    C -->|"hopCount 2"| D["Device D Has Internet"]
    D -->|"HTTPS"| E["Backend"]

    subgraph Deduplication
        B --> F{"PacketDeduplicator"}
        F -->|"Seen before?"| G["DROP"]
        F -->|"New packet"| H["Relay and Store in RelayInbox"]
    end

    subgraph TTL_Guard
        H --> I{"hopCount lt ttl?"}
        I -->|"Yes"| J["Forward to peers"]
        I -->|"No"| K["Store only dont relay"]
    end
```

## Database Schema - Android Room

| Entity | Primary Key | Key Fields |
|---|---|---|
| TripEntity | tripId | touristId, startedAt, endedAt, status |
| BreadcrumbEntity | breadcrumbId | tripId, lat, lon, accuracy, altitude, speed, bearing, battery, activityMode |
| SensorEventChunkEntity | chunkId | tripId, eventType, confidence, startTime, endTime, jsonPayload |
| OutboxEntity | packetId | eventType, payloadJson, status, attemptCount, serverAckId |
| CheckInEntity | id | touristId, zoneId, lat, lon, checkedInAt |
| RelayPacketEntity | packetId | originTouristId, payload, receivedAt, forwarded |
| SafetyZoneEntity | zoneId | name, riskLevel, coordinatesJson |

## Database Schema - Backend PostgreSQL PostGIS

| Table | Geometry Column | Purpose |
|---|---|---|
| tourists | — | Pseudonymous identity records id_hash no PII |
| trips | — | Active and completed trip sessions |
| breadcrumbs | location POINT | GPS trail with battery and accuracy |
| incidents | location POINT | SOS events with idempotent packet_id |
| incident_events | — | Audit trail JSONB payload |
| check_ins | location POINT | Periodic safety confirmations |
| hazard_reports | location POINT | Crowd-sourced hazard intelligence |
| safety_zones | boundary POLYGON | Geofence risk classification |
| responder_units | location POINT | Rescue team positions |
| responder_capabilities | — | Equipment and skill per responder |
| relay_packet_receipts | — | BLE mesh relay audit |
