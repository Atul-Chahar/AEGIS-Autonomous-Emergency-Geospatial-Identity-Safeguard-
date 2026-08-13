# Android Safety UI Restructure Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the Android app shell into a tourist-facing safety journey experience while preserving the existing Liquid Sage mobile design language.

**Architecture:** Add pure Kotlin UI state models and sample state providers first, then wire Compose screens to those models. Keep composables mostly stateless, use existing glass/theme components, and avoid real GPS, BLE, blockchain, SMS, backend, or sensor behavior.

**Tech Stack:** Kotlin, Jetpack Compose, Material3, Navigation3, JUnit 4, existing AEGIS Liquid Sage theme.

---

## File Structure

- Create `aegis-android/app/src/main/java/com/example/aegis/ui/state/AegisUiModels.kt`: shared UI-only model types for guardian, trip, map, activity, SOS, ID, safety center, route deviation, and incident check.
- Create `aegis-android/app/src/main/java/com/example/aegis/ui/state/AegisSampleState.kt`: preview/sample state provider. This may reference existing zone/photo `MockData`, but screens should consume the sample state object.
- Create `aegis-android/app/src/test/java/com/example/aegis/ui/state/AegisUiModelsTest.kt`: JVM tests for pure model helper behavior.
- Modify `aegis-android/app/src/main/java/com/example/aegis/NavigationKeys.kt`: add Map, Trip Setup, Safety Center, Journey BlackBox, and Incident Check keys.
- Modify `aegis-android/app/src/main/java/com/example/aegis/Navigation.kt`: route the new top-level and detail screens.
- Modify `aegis-android/app/src/main/java/com/example/aegis/ui/components/AegisComponents.kt`: update shared nav labels, global Guardian widget, and model-driven SOS overlay.
- Modify `aegis-android/app/src/main/java/com/example/aegis/ui/home/HomeScreen.kt`: replace technical chips with pre-trip/active-trip Journey Protection UI.
- Create `aegis-android/app/src/main/java/com/example/aegis/ui/map/MapScreen.kt`: Map-first route and safety layer UI replacing user-facing Zones.
- Create `aegis-android/app/src/main/java/com/example/aegis/ui/activity/ActivityScreen.kt`: Journey Activity timeline.
- Create `aegis-android/app/src/main/java/com/example/aegis/ui/activity/JourneyBlackBoxScreen.kt`: BlackBox detail UI.
- Create `aegis-android/app/src/main/java/com/example/aegis/ui/trip/TripSetupScreen.kt`: Start Safe Journey setup UI.
- Create `aegis-android/app/src/main/java/com/example/aegis/ui/safety/SafetyCenterScreen.kt`: global safety center UI.
- Create `aegis-android/app/src/main/java/com/example/aegis/ui/incident/IncidentCheckScreen.kt`: automatic incident confirmation UI.
- Modify `aegis-android/app/src/main/java/com/example/aegis/ui/id/TouristIdScreen.kt`: prioritize pseudonymous ID and move blockchain details into collapsible technical verification.
- Optionally leave `aegis-android/app/src/main/java/com/example/aegis/ui/zones/ZonesScreen.kt` in place if not referenced, to reduce churn.

## Task 1: UI State Models And Tests

**Files:**
- Create: `aegis-android/app/src/test/java/com/example/aegis/ui/state/AegisUiModelsTest.kt`
- Create: `aegis-android/app/src/main/java/com/example/aegis/ui/state/AegisUiModels.kt`
- Create: `aegis-android/app/src/main/java/com/example/aegis/ui/state/AegisSampleState.kt`

- [ ] **Step 1: Write failing model tests**

Create tests that define the expected public API:

```kotlin
package com.example.aegis.ui.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AegisUiModelsTest {
  @Test fun guardianAttentionUsesTouristFacingCopy() {
    assertEquals("Guardian Attention", GuardianLevel.ATTENTION.title)
    assertEquals("Safety condition requires attention", GuardianLevel.ATTENTION.subtitle)
  }

  @Test fun sosStepOnlyShowsSuccessWhenSucceeded() {
    assertFalse(SosStepStatus.PENDING.showsSuccess)
    assertFalse(SosStepStatus.IN_PROGRESS.showsSuccess)
    assertTrue(SosStepStatus.SUCCEEDED.showsSuccess)
    assertFalse(SosStepStatus.FAILED.showsSuccess)
  }

  @Test fun significantDeviationUsesHumanDistanceCopy() {
    val state = RouteDeviationState(RouteDeviationSeverity.SIGNIFICANT, distanceMeters = 180)
    assertEquals("You are 180 m away from your planned trail.", state.message)
  }
}
```

- [ ] **Step 2: Run tests and verify RED**

Run: `cd aegis-android && ./gradlew testDebugUnitTest --tests "com.example.aegis.ui.state.AegisUiModelsTest"`

Expected: compilation fails because `GuardianLevel`, `SosStepStatus`, `RouteDeviationState`, and `RouteDeviationSeverity` do not exist.

- [ ] **Step 3: Implement minimal UI model API**

Add enum and data classes for all screens in `AegisUiModels.kt`. Include `GuardianLevel.title`, `GuardianLevel.subtitle`, `SosStepStatus.showsSuccess`, and `RouteDeviationState.message`.

- [ ] **Step 4: Add sample state provider**

Add `AegisSampleState` with `homePreTrip`, `homeActiveTrip`, `map`, `activity`, `blackBox`, `tripSetup`, `safetyCenter`, `incidentCheck`, `digitalId`, and `sosOfflineSearching` sample objects. Use tourist-facing sample values and only reference `MockData` for existing IDs/zones/photos.

- [ ] **Step 5: Run tests and verify GREEN**

Run: `cd aegis-android && ./gradlew testDebugUnitTest --tests "com.example.aegis.ui.state.AegisUiModelsTest"`

Expected: tests pass.

## Task 2: Navigation And Shared Components

**Files:**
- Modify: `aegis-android/app/src/main/java/com/example/aegis/NavigationKeys.kt`
- Modify: `aegis-android/app/src/main/java/com/example/aegis/Navigation.kt`
- Modify: `aegis-android/app/src/main/java/com/example/aegis/ui/components/AegisComponents.kt`

- [ ] **Step 1: Add navigation keys**

Add keys for `Map`, `TripSetup`, `SafetyCenter`, `JourneyBlackBox`, and `IncidentCheck`. Keep existing `Zones` only if needed for compatibility, but user-facing nav must use `Map`.

- [ ] **Step 2: Update shared bottom navigation usage**

Use labels `Home`, `Map`, `Activity`, and `ID`, with central SOS unchanged. Ensure `Activity` no longer shows a toast placeholder.

- [ ] **Step 3: Replace SOS overlay internals**

Change `SosOverlay` to accept `SosFlowState` and render press-and-hold confirmation plus model-driven step statuses. Pending/in-progress steps use neutral/progress indicators; only `SosStepStatus.SUCCEEDED` gets success treatment.

- [ ] **Step 4: Add shared Guardian state component**

Add a reusable `GuardianStatePill(state: GuardianSystemState, onClick: () -> Unit)` using Liquid Sage glass styling and green/yellow/orange/red severity.

- [ ] **Step 5: Build compile checkpoint**

Run: `cd aegis-android && ./gradlew assembleDebug`

Expected: compile errors only for screens not wired yet are acceptable at this task boundary if new imports reference upcoming screens; otherwise build should pass.

## Task 3: Home And Trip Setup

**Files:**
- Modify: `aegis-android/app/src/main/java/com/example/aegis/ui/home/HomeScreen.kt`
- Create: `aegis-android/app/src/main/java/com/example/aegis/ui/trip/TripSetupScreen.kt`

- [ ] **Step 1: Make Home consume `JourneyHomeState`**

Change `HomeScreen` to accept `state: JourneyHomeState = AegisSampleState.homeActiveTrip`, callbacks for Map, ID, Activity, Trip Setup, Safety Center, and Zone Detail where still needed.

- [ ] **Step 2: Implement pre-trip and active-trip sections**

Render pre-trip destination, risk level, expected duration, offline readiness, and "Start Safe Journey". Render active-trip Guardian status, destination, journey metrics, Journey Protection rows, check-in countdown, nearest hazard, and "View Live Route".

- [ ] **Step 3: Remove technical chips**

Delete user-facing "Guardian ID", "Geofence", and "Mesh" chips from Home. Replace with Journey Protection rows: Location, Journey Log, Offline Relay, and Check-in.

- [ ] **Step 4: Add Trip Setup screen**

Create a Liquid Sage setup screen with destination, planned route, expected return time, emergency contact, offline map readiness, Journey BlackBox readiness, check-in interval, and "START SAFE JOURNEY".

- [ ] **Step 5: Compile checkpoint**

Run: `cd aegis-android && ./gradlew assembleDebug`

Expected: Home and Trip Setup compile.

## Task 4: Map Screen

**Files:**
- Create: `aegis-android/app/src/main/java/com/example/aegis/ui/map/MapScreen.kt`
- Modify: `aegis-android/app/src/main/java/com/example/aegis/Navigation.kt`

- [ ] **Step 1: Create Map-first layout**

Implement a map-like Liquid Sage safety surface with current position marker, planned route line, corridor band, safety zones, hazards, rescue posts, breadcrumb markers, and a bottom sheet style detail panel.

- [ ] **Step 2: Add layer controls**

Add horizontally scrollable layer controls for Route, Safety, Hazards, and Rescue. Avoid fixed widths that clip "High Risk" or long labels.

- [ ] **Step 3: Preserve zone detail entry**

Allow opening existing `ZoneDetailScreen` from map bottom-sheet cards without presenting the whole app as a zone list.

- [ ] **Step 4: Compile checkpoint**

Run: `cd aegis-android && ./gradlew assembleDebug`

Expected: Map screen compiles and top-level Map navigation works.

## Task 5: Activity And Journey BlackBox

**Files:**
- Create: `aegis-android/app/src/main/java/com/example/aegis/ui/activity/ActivityScreen.kt`
- Create: `aegis-android/app/src/main/java/com/example/aegis/ui/activity/JourneyBlackBoxScreen.kt`
- Modify: `aegis-android/app/src/main/java/com/example/aegis/Navigation.kt`

- [ ] **Step 1: Implement Activity timeline**

Render `ActivityTimelineState.events` as a vertical Journey Activity timeline with event title, subtitle, time, severity, and icon treatment.

- [ ] **Step 2: Add BlackBox entry point**

Add a prominent "Journey BlackBox" card with recording state, last breadcrumb time, and a CTA to open detail.

- [ ] **Step 3: Implement BlackBox detail**

Render recording state, last breadcrumb time, location accuracy, current activity type, battery, stored/synced/pending breadcrumb counts, and recent safety events.

- [ ] **Step 4: Compile checkpoint**

Run: `cd aegis-android && ./gradlew assembleDebug`

Expected: Activity and Journey BlackBox compile; Activity no longer uses toast placeholder.

## Task 6: Safety Center, Incident Check, Route Deviation, And ID

**Files:**
- Create: `aegis-android/app/src/main/java/com/example/aegis/ui/safety/SafetyCenterScreen.kt`
- Create: `aegis-android/app/src/main/java/com/example/aegis/ui/incident/IncidentCheckScreen.kt`
- Modify: `aegis-android/app/src/main/java/com/example/aegis/ui/id/TouristIdScreen.kt`
- Modify as needed: `aegis-android/app/src/main/java/com/example/aegis/ui/home/HomeScreen.kt`

- [ ] **Step 1: Implement Safety Center**

Render Journey Protection, Emergency, Privacy, and Offline sections with rows for Journey BlackBox, location protection, motion detection, offline relay, emergency contacts, medical details, data expiry, offline maps, and pending transmissions.

- [ ] **Step 2: Implement Incident Check**

Render possible incident state, "ARE YOU OKAY?", countdown progress, "I'M SAFE", and "NEED HELP". Do not call SOS dispatch directly; only route through UI callbacks.

- [ ] **Step 3: Add route deviation warning card**

Render route deviation copy from `RouteDeviationState.message` wherever the active journey sample state includes one.

- [ ] **Step 4: Restructure Digital ID**

Keep current visual direction, prioritize pseudonymous ID/status/QR/validity/privacy explanation, and move hash, contract, and network to a collapsible "Technical verification" section.

- [ ] **Step 5: Compile checkpoint**

Run: `cd aegis-android && ./gradlew assembleDebug`

Expected: all screens compile.

## Task 7: Final Verification And Cleanup

**Files:**
- Inspect all touched Android files.

- [ ] **Step 1: Run JVM tests**

Run: `cd aegis-android && ./gradlew testDebugUnitTest`

Expected: all JVM tests pass.

- [ ] **Step 2: Run Android build**

Run: `cd aegis-android && ./gradlew assembleDebug`

Expected: debug APK builds successfully.

- [ ] **Step 3: Check design constraints**

Review Home, Map, Activity, ID, SOS, Trip Setup, Safety Center, BlackBox, and Incident Check for: no fake GPS/BLE/backend/blockchain/SMS claims, no clipped filter/layer pills, small-screen-friendly text wrapping, central SOS preserved, and Liquid Sage visual language preserved.

- [ ] **Step 4: Report changed screens and verification**

Summarize changed screens/components and test/build results in the final response.
