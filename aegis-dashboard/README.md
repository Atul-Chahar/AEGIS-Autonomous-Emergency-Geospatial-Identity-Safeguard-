# AEGIS Authority Dashboard

React/Vite command center for AEGIS authority operators. The dashboard tracks active tourist trips, SOS incidents, hazards, geofences, responders, and BlackBox breadcrumb trails through the local AEGIS backend.

## Purpose

The dashboard is an emergency operations surface, not a general surveillance product. It should show pseudonymous safety state only:

- `touristId`, `idHash` preview, and `tripId`
- active incident status
- last-known location, accuracy, timestamp, and battery
- selected trip breadcrumb trail
- geofence, hazard, search probability, and responder overlays

It must not display raw passport numbers, Aadhaar numbers, phone numbers, emergency contacts, or identity documents.

## Tech Stack

- React 18 + Vite
- Leaflet + React Leaflet
- lucide-react icons
- OpenStreetMap tiles
- AEGIS Express REST API
- AEGIS WebSocket gateway

## Local Development

```powershell
npm install
npm run dev
```

Default API configuration:

- `VITE_API_BASE_URL=http://localhost:5000/api`
- `VITE_WS_URL=ws://localhost:5000`

## Verification

```powershell
npm run build
```

Backend verification:

```powershell
cd ..\aegis-backend
npm test
```

## Dashboard Roadmap

The implementation plan for user/trip tracking is documented at:

- `docs/superpowers/plans/2026-08-14-web-dashboard-user-tracking.md`
