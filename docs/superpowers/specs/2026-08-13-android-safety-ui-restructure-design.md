# AEGIS Android Safety UI Restructure Design

## Context

AEGIS Android already has a distinct Liquid Sage visual language: sage gradient background, Sora typography, rounded glass cards, photography-led zone cards, and a floating dark bottom navigation bar with a raised central SOS trigger. This project should extend that existing mobile app language. It must not copy structure from the web dashboard, because the dashboard layout is not finalized.

The current Android app has Home, Zones, Zone Detail, Tourist ID, shared Liquid Sage components, and placeholder Activity navigation. The prompt asks for a premium tourist-facing safety experience, not a technical developer dashboard. Technical systems such as GPS, BLE mesh, sensor fusion, BlackBox, blockchain, and backend transport must be represented as future-connectable UI state, not fake implementations.

## Product Direction

The app will use human safety language first. Tourists should see clear journey and protection states such as "Guardian Active", "Journey Log Recording", "Offline Relay Available", "You are slightly off route", and "No internet. Your emergency has been safely stored." Internal terms should appear only in detail, technical verification, or developer-facing model names.

The bottom navigation becomes:

- Home
- Map
- central SOS
- Activity
- ID

The former Zones destination becomes a Map-first safety route surface. Existing zone data and photography can still support cards and previews, but the screen label and hierarchy should be journey/map oriented.

## Information Architecture

### Home

Home supports two user states:

- Pre-trip: destination, risk level, expected journey duration, offline readiness, and a "Start Safe Journey" CTA.
- Active trip: Guardian state, current safety status, destination, journey duration/distance, risk level, Journey Protection recording, check-in countdown, nearest hazard, and a "View Live Route" CTA.

The current "Guardian ID / Geofence / Mesh" chips become a "Journey Protection" section with user-facing rows:

- Location: Active
- Journey Log: Recording
- Offline Relay: Available
- Check-in: 24 min

Each row will be model-driven and can later expose technical details after tapping.

### Map

The Map screen will replace the Zones top-level destination. It should feel map-first and support future rendering of current location, planned route, route corridor, Safe/Caution/High-Risk zones, hazards, rescue posts, breadcrumb path, and search sectors.

For the current UI-only implementation, the map can be a Liquid Sage styled placeholder/canvas surface using existing zone sample data. It must avoid claiming live GPS or real routing. A draggable-style bottom sheet surface will show location or zone details, and layer controls will include Route, Safety, Hazards, and Rescue. The layout must fix existing clipped filter pills and overlapping stacked cards.

### Activity

Activity replaces the current toast placeholder with a real Journey Activity timeline prepared for events such as trip started, location recorded, safety check completed, caution zone entered, offline relay available, route deviation, possible fall detected, SOS sent, SOS delivered, and trip ended.

A Journey BlackBox detail screen will show recording state, last breadcrumb time, location accuracy, current activity type, battery, stored/synced/pending breadcrumbs, and recent safety events. This screen is still UI-only and reads from sample state.

### Guardian State And Safety Center

The top-right Guardian widget becomes a global system state component used across key screens. It supports:

- Green: Guardian Active, all protection systems working.
- Yellow: Guardian Limited, offline protection active.
- Orange: Guardian Attention, safety condition requires attention.
- Red: Emergency Mode, rescue workflow active.

Tapping the widget opens a Safety Center screen. Safety Center groups Journey Protection, Emergency, Privacy, and Offline rows for Journey BlackBox, location protection, motion detection, offline relay, emergency contacts, medical details, data expiry, offline maps, and pending transmissions.

### Route Deviation

Route deviation appears as calm warning UI with three severities:

- Minor: "You are slightly off route."
- Significant: "You are 180 m away from your planned trail."
- High-risk: stronger warning language and prominent action to view route or request help.

No raw algorithm details are shown.

### Automatic Incident Check

An incident confirmation screen is added for future sensor-fusion triggers. It supports states like possible fall, vehicle crash, or unusual impact. The screen headline asks "ARE YOU OKAY?", shows an emergency countdown progress indicator, and offers "I'M SAFE" and "NEED HELP" actions.

This is UI-only. It must not trigger real emergency dispatch.

### SOS

The central red SOS button remains. Tapping opens a full-screen emergency sheet with press-and-hold confirmation to reduce accidental activation.

SOS progress is model-driven with transport states:

- Emergency recorded
- Location locked
- Journey BlackBox attached
- Sending via internet
- SMS fallback
- Searching for offline relay
- Authority received

The UI must not display a success checkmark unless the state model marks that step as succeeded. Offline messaging is supported, including "No internet. Your emergency has been safely stored. Searching for a nearby relay."

### Digital ID

The ID screen keeps the current visual direction. The priority order becomes:

- Tourist pseudonymous ID
- Status
- QR placeholder interface
- Validity
- Privacy explanation

Blockchain hash, contract address, and network are moved into a collapsible "Technical verification" section. The screen must preserve the zero-knowledge principle and never imply raw PII is on-chain.

### Trip Setup

A Start Safe Journey setup screen collects or previews destination, planned route, expected return time, emergency contact, offline map readiness, Journey BlackBox readiness, and check-in interval. The primary CTA is "START SAFE JOURNEY".

For this UI pass, fields can be represented by sample state and selectable rows; no backend calls or real contact lookup are added.

## State And Data Architecture

Add dedicated UI state models under the Android app package, separate from composables. Composables should accept state objects and callbacks rather than reading directly from `MockData` where practical.

Sample data should live in clearly named preview/sample providers, such as `AegisSampleState`, so future repositories can replace it with ViewModel or repository-backed state. Existing `MockData` can remain for current zone/photo assets, but new screens should not bind directly to it.

Core model groups:

- `GuardianSystemState`
- `JourneyHomeState`
- `TripSetupState`
- `JourneyProtectionItem`
- `MapUiState`
- `MapLayerState`
- `ActivityTimelineState`
- `JourneyBlackBoxState`
- `RouteDeviationState`
- `IncidentCheckState`
- `SosFlowState`
- `DigitalIdUiState`
- `SafetyCenterState`

## Navigation

Add top-level navigation keys for Map, Activity, Trip Setup, Safety Center, Journey BlackBox, Incident Check, and any SOS sheet state needed locally. The bottom nav should be shared across Home, Map, Activity, and ID. SOS remains a central overlay action, not a top-level destination.

The existing `Zones` navigation key can either be renamed to `Map` or kept internally with a Map label if renaming causes unnecessary churn. User-facing text must say Map.

## Visual And Motion Rules

Preserve Liquid Sage: sage gradient, glass surfaces, Sora typography, rounded cards, dark floating nav, and photography where relevant.

Motion is restrained and tied to state changes:

- Guardian state transitions
- Risk meter movement
- SOS pulse or hold progress
- Check-in countdown
- Map bottom sheet movement
- BlackBox recording indicator

Avoid decorative constant motion.

## Accessibility And Layout

Support small phone widths and system font scaling. Avoid fixed-height text containers when long text could wrap. All interactive controls must have touch targets of at least 48 dp and useful content descriptions. Filter and layer pills should horizontally scroll or wrap without clipping. Text must not overlap cards at larger font scales.

## Testing And Verification

Implementation should include focused tests for pure UI state helpers where possible, especially:

- Guardian state label and severity mapping.
- SOS step rendering rules, including no success checkmark without a succeeded state.
- Route deviation user-facing copy.

Build verification is:

```powershell
cd aegis-android
./gradlew assembleDebug
```

If local Android test infrastructure is available, run relevant unit tests before the final build.

## Out Of Scope

This pass does not implement real GPS, BLE mesh, BlackBox persistence, sensor fusion, blockchain calls, SMS sending, internet transport, backend APIs, dashboard UI, or paid third-party services. It prepares clean UI and state contracts so those real systems can be connected later.
