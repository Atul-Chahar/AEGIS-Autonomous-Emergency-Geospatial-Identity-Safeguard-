# AEGIS Design System — "Liquid Sage" v1.0

**One design language for the whole AEGIS product** — Android app, Authority Dashboard (web) and everything in between.

This document is the **single source of truth** for visual design. It was derived from the implemented Android UI (`aegis-android/app/src/main/java/com/example/aegis/`) and the original travel-app mockup that inspired it: **same gradients, same liquid glass, same editorial typography** — re-skinned for a tourist-safety product.

> 🖼 Reference renders: `screenshots/` (root of this repo) — `01_home` … `07_home_again`, plus the contact sheet `aegis_all_screens.png`.

---

## 1. Design Principles

1. **Liquid glass over vivid gradients.** Content floats on frosted-white glass panels above a sage/lime gradient field. Glass = translucency + hairline white border + soft, tinted shadow. Never use harsh shadows or pure-black borders.
2. **Forest ink typography.** Bold, geometric, tight-tracked headings (Sora ExtraBold). Maximum 4 font sizes and 3 weights per screen — hierarchy comes from weight and size, not color.
3. **Green is safety, yellow is action, red is emergency only.** Status colors carry meaning (Safe / Caution / High Risk) and must never be used decoratively.
4. **Dark floating navigation.** A near-black-green floating pill anchors the bottom of tab screens; the raised red SOS circle is the single most important element and always sits in the thumb zone.
5. **Imagery sells the place.** Photos are full-bleed, cropped, and always graded with a dark-green bottom scrim so white text stays legible.
6. **Offline-first, zero-cost.** Every asset ships inside the app (fonts, photos). No runtime network dependencies for rendering.

---

## 2. Color System

### 2.1 Light theme — "Sage" (default brand)

| Token | Hex | RGB | Role |
|---|---|---|---|
| `SagePale` | `#F3F8E6` | `rgb(243,248,230)` | Background gradient top / secondary containers |
| `SageLight` | `#E7F0D0` | `rgb(231,240,208)` | Background gradient middle / surface variants |
| `SageSoft` | `#D5E6B6` | `rgb(213,230,182)` | Background gradient bottom / primary containers |
| `SageMid` | `#BCD693` | `rgb(188,214,147)` | Gradient hero cards, glow blobs |
| `SageDeep` | `#92B868` | `rgb(146,184,104)` | Strongest sage accent |
| `Sage500` | `#7FA05B` | `rgb(127,160,91)` | Neutral sage |
| `Sage600` | `#64854A` | `rgb(100,133,74)` | Secondary / outlines (40% alpha) |
| `Sage700` | `#4C6A39` | `rgb(76,106,57)` | Neutral sage dark |
| `Ink` | `#1A2419` | `rgb(26,36,25)` | Primary text, icons, headings |
| `InkSoft` | `#5B6C5B` | `rgb(91,108,91)` | Secondary text, captions |
| `ForestDark` | `#17382B` | `rgb(23,56,43)` | Dark cards, selected pills, QR ink |
| `ForestDeep` | `#0D241B` | `rgb(13,36,27)` | Floating nav pill, scrims, dark bg (dark mode) |
| `SunYellow` | `#F7C81B` | `rgb(247,200,27)` | **The action accent** — FAB, selected nav, stars |
| `SunYellowSoft` | `#FFF6D6` | `rgb(255,246,214)` | Yellow container backgrounds |
| `LimeGlow` | `#C6F24E` | `rgb(198,242,78)` | Decorative glow only |

**Material roles (light):** primary `ForestDark` · onPrimary `#FFFFFF` · primaryContainer `SageSoft` · onPrimaryContainer `Ink` · secondary `Sage600` · secondaryContainer `SagePale` · tertiary `SunYellow` · onTertiary `Ink` · tertiaryContainer `SunYellowSoft` · background `SagePale` · surface `#FFFDF8` · surfaceVariant `SageLight` · onSurfaceVariant `InkSoft` · error `DangerRed`.

### 2.2 Status colors (semantic — never decorative)

| Token | Hex | RGB | Meaning |
|---|---|---|---|
| `SafeGreen` | `#2FBF71` | `rgb(47,191,113)` | Safe zone, success, checked-in |
| `CautionAmber` | `#F5A623` | `rgb(245,166,35)` | Caution band, warnings, risk 31–60 |
| `DangerRed` | `#F04438` | `rgb(240,68,56)` | High risk, SOS, errors |
| `MeshCyan` | `#17B7D6` | `rgb(23,183,214)` | Mesh / connectivity |

Status chip recipe: background = status color @ **14% alpha**, border = status color @ **45% alpha**, text = full status color. On dark surfaces: background @ 28%, border @ 60%, text white.

### 2.3 Dark theme — "Forest" (Android dark mode)

| Token | Value | Role |
|---|---|---|
| background | `#0D241B` (ForestDeep) | Page background |
| surface | `#17382B` (ForestDark) | Cards |
| surfaceVariant | `#22382D` | Subtle surfaces |
| onBackground / onSurface | `#F3F8E6` (SagePale) | Primary text |
| onSurfaceVariant | `#B7C8B0` | Secondary text |
| primary | `SageSoft` · onPrimary `ForestDeep` | CTAs |
| tertiaryContainer | `#4A3F00` · onTertiaryContainer `#FFF6D6` | Yellow accents |

Same glass rules apply — glass becomes dark-forest-tinted translucency.

### 2.4 Authority Dashboard variant — "Command Center"

The web **Authority Dashboard** keeps the same glass language on a **deep slate** base (matches the command-center brief):

- Background: `#090D16` (deep slate) with subtle radial sage/cyan glows
- Glass panels: `rgba(30, 41, 59, 0.7)` + 1px `rgba(255,255,255,0.08)` border + `backdrop-filter: blur(16px)`
- Accents: cyan `#06B6D4` (live data), amber `#F59E0B` (caution), crimson `#EF4444` (SOS), sage `#2FBF71` (safe)
- Text: white / `#94A3B8` secondary
- Everything else (radii, type, shadows, status pills, risk meter) is identical to this spec.

---

## 3. Typography — Sora

**Family:** [Sora](https://fonts.google.com/specimen/Sora) (geometric sans). Bundled offline in the Android app (`res/font/sora_{regular,semibold,bold,extrabold}.ttf`). Web: Google Fonts `Sora:wght@400;600;700;800`.

| Style | Size / Line | Weight | Tracking | Use |
|---|---|---|---|---|
| **displayLarge** | 42 / 46 | ExtraBold 800 | −1.2 | Hero ("Safe Passage") |
| **displayMedium** | 34 / 38 | ExtraBold 800 | −0.8 | Screen titles |
| **headlineLarge** | 27 / 32 | ExtraBold 800 | −0.4 | Card titles (featured zone) |
| **headlineMedium** | 22 / 27 | Bold 700 | 0 | Card titles (dark card) |
| **headlineSmall** | 18 / 23 | Bold 700 | 0 | Section titles |
| **titleLarge** | 17 / 22 | Bold 700 | 0 | Greeting, card names |
| **titleMedium** | 15 / 20 | SemiBold 600 | 0 | List item titles |
| **titleSmall** | 13 / 18 | SemiBold 600 | 0 | Nav labels, sub-titles |
| **bodyLarge** | 15 / 22 | Regular 400 | 0 | Primary body |
| **bodyMedium** | 13 / 19 | Regular 400 | 0 | Secondary body |
| **bodySmall** | 12 / 16 | Regular 400 | 0 | Captions |
| **labelLarge** | 13 / 18 | SemiBold 600 | +0.2 | Buttons, prominent labels |
| **labelMedium** | 11 / 15 | SemiBold 600 | +0.3 | Chips, meta |
| **labelSmall** | 10 / 14 | SemiBold 600 | +0.4 | Timestamps, badges, tags |

**Rules:** hero display text may break across two lines ("Safe\nPassage"). Emoji (📅 📍 📡 🚨 🟢 🛡️) are first-class glyphs for metadata and category icons — use them intentionally, never in long sentences. Use `sp`/`rem` units (never fixed px for text) so user font scaling works.

---

## 4. Spacing & Layout Grid

- **8-point grid.** All spacing is a multiple of 4 (8, 12, 16, 20, 24, 32).
- Screen horizontal padding: **20** (cards), **16–20** inside cards.
- Section rhythm: **18** vertical gaps between blocks; card internal padding **20–22** (compact cards 14–16).
- Hero title → category pills → featured card → section headers, in that order, with 18–24 gaps.
- Bottom content clearance above the floating nav: **≥ 120px**.
- Content area: **compact (phone) widths only**; on web, cap content columns at ~480px for phone-like panels, or use the command-center layout for the map.

---

## 5. Corner Radii

| Token | Value | Use |
|---|---|---|
| `radius-pill` | 999 (50dp) | Chips, pills, buttons, status badges |
| `radius-xl` | 32 | Featured cards, detail content card, hero cards |
| `radius-lg` | 28 | Default glass cards |
| `radius-md` | 24 | Dark cards, SOS card, small glass cards |
| `radius-sm` | 16–18 | Thumbnails, QR containers, buttons |
| `radius-circle` | 50% | Icon buttons, FABs, avatars |

**Rule:** never mix radii within one card cluster; a card and everything inside it shares one family (large card = lg/xl, compact card = sm/md).

---

## 6. Elevation & Shadows

**Soft, tinted, never gray.** Shadows are tinted with the page's dominant hue.

| Token | Spec | Use |
|---|---|---|
| Glass card | `0 20px 40px rgba(100,133,74,0.20)` | Glass cards on sage background |
| Dark card | `0 20px 40px rgba(13,36,27,0.35)` | ForestDark cards |
| FAB / SOS | `0 12px 24px rgba(13,36,27,0.30)` + glow | Floating actions |
| Nav pill | `0 16px 32px rgba(13,36,27,0.45)` | Bottom navigation |

**Rule:** elevation is conveyed by shadow + translucency together, not by darkening the card.

---

## 7. Liquid Glass System

The heart of the design. Glass = **translucent fill + hairline light border + backdrop blur + soft tinted shadow**.

### 7.1 Tokens (implemented in `theme/Color.kt`)

| Token | Value | Use |
|---|---|---|
| `GlassSurface` | `rgba(255,253,248,0.70)` | Default glass card |
| `GlassSurfaceStrong` | `rgba(255,253,248,0.85)` | Stacked cards, detail card, dumps |
| `GlassBorder` | `rgba(255,255,255,0.35)` | Glass card border (light context) |
| `GlassOnImage` | `rgba(13,36,27,0.55)` | Dark glass pills over photos |
| `GlassOnImageBorder` | `rgba(255,255,255,0.18)` | Dark glass border |
| `GlassSoftShadow` | `rgba(100,133,74,0.20)` | Tinted soft shadow |
| `GlassScrim` | `rgba(13,36,27,0.70)` | Bottom scrim on image cards |

### 7.2 Recipes

```css
/* Light glass (on sage background) */
.glass {
  background: rgba(255, 253, 248, 0.70);
  border: 1px solid rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  box-shadow: 0 20px 40px rgba(100, 133, 74, 0.20);
  border-radius: 28px;
}

/* Dark glass (over photos / dark surfaces) */
.glass-dark {
  background: rgba(13, 36, 27, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(14px);
  color: #fff;
}

/* Strong glass (stacked cards, dense surfaces) */
.glass-strong {
  background: rgba(255, 253, 248, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(20px);
  border-radius: 28px;
}
```

**Performance:** use smaller blur values (`blur(8–14px)`) on frequently-updating or large surfaces; avoid blur over full-viewport areas on low-end devices.

---

## 8. Gradients

| Name | Spec | Use |
|---|---|---|
| **Page background** | `linear-gradient(180deg, #F3F8E6, #E7F0D0, #D5E6B6)` | All tab screens |
| **Glow blobs** | radial, 240–320px: `rgba(247,200,27,0.45)` top-right · `rgba(188,214,147,0.50)` bottom-left · `rgba(198,242,78,0.30)` bottom-right | Decorative life under the glass |
| **Image scrim** | `linear-gradient(180deg, transparent 40%, rgba(13,36,27,0.70))` | Photo cards & banners for white-text legibility |
| **Hero pass card** | `linear-gradient(135deg, #D5E6B6, #BCD693)` | Tourist ID header |
| **Risk meter** | `linear-gradient(90deg, #2FBF71, #F5A623, #F04438)` | Live risk bar |
| **CTA on image** | solid `rgba(13,36,27,0.55)` + white border, **no gradient** | "Start Route" pill |

---

## 9. Status & Risk Semantics

Risk score bands (from the AEGIS plan — must stay consistent everywhere):

| Band | Score | Color | Label copy |
|---|---|---|---|
| Safe | 0–30 | `SafeGreen` | Safe · Normal |
| Caution | 31–60 | `CautionAmber` | Caution · Interactive prompt |
| High Risk | 61–100 | `DangerRed` | High Risk · Control room advised |

**Risk meter component:** horizontal 12px gradient bar (Safe→Caution→High), white circular marker with a 2px status-colored ring positioned at `score%`, three endpoint labels (Safe / Caution / High Risk), and a "LIVE RISK SCORE — n/100" header (score text colored by band).

**Status pill:** `emoji + label`, e.g. `🟢 Safe`, `🟡 Caution`, `🔴 High Risk` (recipe in §2.2).

---

## 10. Component Library

All components below are implemented in `aegis-android/.../ui/components/AegisComponents.kt` — read the code for exact spacing; specs here are the contract.

### 10.1 Buttons

| Variant | Fill | Text | Radius | Use |
|---|---|---|---|---|
| **Primary** | `ForestDark` | White | 16 | Main actions |
| **SOS** | `DangerRed` (full) | White, labelLarge, uppercase | 16 | "PRESS TO DISPATCH SOS", height 52–56 |
| **Dark glass pill** | `rgba(13,36,27,0.55)` + white 18% border | White | pill | CTA on photos ("Start Route") |
| **Yellow circle FAB** | `SunYellow` + white 60% border, 62px | `Ink` icon | circle | "I'm Safe" check-in |
| **SOS trigger** | `DangerRed` + 3px white 90% border, 58px, raised −14px over nav | 🚨 | circle | Nav center, thumb zone |
| **Outline (revoke)** | `rgba(240,68,56,0.08)` + 45% red border | `DangerRed` | pill | Destructive secondary |

Button min touch target: **48×48dp** (SOS 58–62dp).

### 10.2 Pills & Chips

- **Filter pill:** height 42, pill radius. Selected = `ForestDark` fill + white text; unselected = glass surface + `Ink` text. Optional leading emoji. Arranged in horizontally scrollable rows.
- **Region tag:** `🇮🇳 MEGHALAYA` — glass pill (or white 14% on dark), labelSmall.
- **Status pill:** see §9.
- **Guardian widget:** white glass pill = circular icon well (36px, status-tinted 16% fill + 55% border) + two-line label ("Guardian" / "🟢 Safe Zone").

### 10.3 Cards

- **Glass card** — §7 recipe, padding 20.
- **Dark forest card** — `ForestDark`, radius 32, padding 22, white text. Used for the active zone on the Zones screen and the SOS dispatch panel.
- **Image card (featured)** — radius 32, full-bleed photo + scrim (§8), content bottom-aligned: region+status row → title (headlineLarge, white) → tagline (bodyMedium white 85%) → meta row (📅 📍 📡) → dark-glass CTA pill.
- **Stacked peek cards** — strong glass, radius 26, height ~104, each successive card offset **+30px** and faded **12%** further, so only the header sliver of each peeks out behind the one above (mockup "Popular" stack).
- **Active zone card** — dark forest: region+status top row, bookmark top-right, headlineMedium title, meta row, then avatar stack + "peers in mesh" + circular white arrow button.
- **Detail content card** — strong glass, radius 32, overlaps the banner by **−28px**.

### 10.4 Bottom Navigation — "Floating SOS Pill"

- Dark pill: `rgba(13,36,27,0.94)`, radius 34, height 68, white 12% border, big soft shadow; floats with 10–12px bottom margin + system-bar inset.
- **5 slots:** Home · Zones · **SOS (raised, center)** · Activity · ID.
- Item anatomy: 22px icon + labelSmall; selected = `SunYellow` icon+label; unselected = white 55%.
- SOS center button is **raised −14px** above the pill, 58px `DangerRed` circle with 3px white ring.
- The SOS is an action (opens dispatch overlay), not a destination.

### 10.5 SOS Dispatch Overlay

Full-screen scrim `rgba(13,36,27,0.72)` + centered glass card (radius 32):
- 64px red circle with an **infinite pulse ring** (1.3s ease, scales 1→2.1, fades 50%→0)
- Title "EMERGENCY SOS" (headlineMedium, red) → "HELP EN ROUTE" (green) after dispatch
- Payload preview chip: `SOS:TST-8F29X4|25.141,91.261|BAT 82|12:04` + channel ticks (WebSockets ✓ · SMS fallback ✓ · Mesh relay ✓)
- Big red "DISPATCH SOS NOW" button → green confirmation banner
- Cancel / Close text button

### 10.6 Avatars (mesh peers)

Overlapping circles, 30px (2px white ring), initials on 4 sage/cyan/amber/violet gradients, overflow "+n" on frosted white. Palette: `#8FB565→#4C6A39`, `#17B7D6→#0E6E86`, `#F5A623→#C47B12`, `#9B6BC4→#6B3E96`.

### 10.7 QR / Voucher

QR is **drawn procedurally** (23×23 deterministic pattern with 3 finder squares) — no bitmap needed, offline-safe. Ink `#1A2419` on white glass (or white on `ForestDark` for dark contexts).

### 10.8 Meta items

`📅 7 days · 📍 1,965 m · 📡 2 peers` — emoji (13px) + labelMedium text. On dark surfaces text is white 85%.

---

## 11. Screens & Navigation

| Screen | Key elements | Nav bar |
|---|---|---|
| **Home — "Safe Passage"** | Greeting "Hi, Aryan 👏" + Guardian widget · 🇮🇳 MEGHALAYA tag · displayLarge hero · Scan-ID QR pill · category pills (🛡️ Guardian ID, 📍 Geofence, 📡 Mesh, 📊 Risk) · featured zone image card ("Start Route") · "Your Guardian ID" strip (mini QR + ID + active status) | Yes (Home selected) |
| **Safety Zones** | Back + star/menu glass buttons · displayMedium title · dark active-zone card · status filter pills (All / Safe / Caution / High Risk) · stacked peek cards | Yes (Zones selected) |
| **Zone Detail** | Full-bleed banner (400px) + glass back pill + Live status pill + Risk badge + title + peer avatars · overlapping content card: region, heading, description, meta row, risk meter, SOS dispatch card, nearest-rescue-post sub-card · floating yellow "I'm Safe" FAB | **No** |
| **Tourist ID** | Back + star/menu · displayMedium title · gradient hero pass (AEGIS SAFEPASS, ID, ACTIVE chip) · QR verification voucher · On-Chain Proof card (hash, contract, network, auto-expiry, privacy note) · Revoke outline button | Yes (ID selected) |

**Navigation rules:** tab switches replace the whole back stack; detail pushes on top; detail has no bottom nav (back arrow returns to the previous tab). Activity tab is a placeholder for the check-in log.

---

## 12. Motion

| Motion | Spec | Use |
|---|---|---|
| SOS pulse ring | 1.3s ease-in-out, infinite, scale 1→2.1, alpha 0.5→0 | SOS overlay |
| Screen transitions | 250–300ms, subtle fade+slide | Navigation |
| Check-in feedback | Instant green pill drop-in below the FAB: "✓ Checked in — guardian notified" | Detail screen |
| Animations scale | Respect system "remove animations" (set all to 0 in tests) | Global |

Keep motion calm and purposeful — trust, not spectacle.

---

## 13. Iconography & Imagery

- **Icons:** Material Symbols (core set) — `Home`, `Place`, `Notifications`, `Person`, `Star`, `ArrowBack/Forward`, `FavoriteBorder`, `CheckCircle`, `KeyboardArrowRight`. Filled for selected states, outlined for unselected (where available).
- **Emoji as UI glyphs** for categories & meta (mockup-faithful): 🛡️ 📍 📡 📊 📅 👟 🚨 🟢 🟡 🔴 ⚠️ 🛰 🇮🇳.
- **Photos:** bundled, offline, 1200px wide, `cover`-cropped. Always scrim-graded for white text. Meghalaya subjects: waterfalls, rainforest, rivers, living root bridges.
- **No pure-gray shadows, no stock-photo mixing** — imagery should carry the same warm-green grading.

---

## 14. Accessibility

- Text contrast ≥ 4.5:1 normal / 3:1 large (Ink on SagePale passes; white on ForestDark passes).
- Touch targets ≥ 48×48dp.
- Color never carries meaning alone — status pills pair color with emoji + label.
- All icons need content descriptions; decorative elements get `null`.
- Test at 200% font scale — layout must not clip.

---

## 15. Web Implementation (Authority Dashboard / React)

Same tokens, CSS-first. Put these in `aegis-dashboard/src/`:

```css
:root {
  /* Sage palette */
  --sage-pale: #F3F8E6; --sage-light: #E7F0D0; --sage-soft: #D5E6B6;
  --sage-mid: #BCD693; --sage-deep: #92B868;
  --sage-600: #64854A; --sage-700: #4C6A39;
  --ink: #1A2419; --ink-soft: #5B6C5B;
  --forest-dark: #17382B; --forest-deep: #0D241B;
  --sun-yellow: #F7C81B; --sun-yellow-soft: #FFF6D6; --lime-glow: #C6F24E;

  /* Status */
  --safe: #2FBF71; --caution: #F5A623; --danger: #F04438; --mesh: #17B7D6;

  /* Glass */
  --glass: rgba(255,253,248,0.70);
  --glass-strong: rgba(255,253,248,0.85);
  --glass-border: rgba(255,255,255,0.35);
  --glass-dark: rgba(13,36,27,0.55);
  --glass-dark-border: rgba(255,255,255,0.18);
  --glass-shadow: 0 20px 40px rgba(100,133,74,0.20);
  --scrim: linear-gradient(180deg, transparent 40%, rgba(13,36,27,0.70));

  /* Radius & type */
  --r-pill: 999px; --r-xl: 32px; --r-lg: 28px; --r-md: 24px; --r-sm: 16px;
  --font: 'Sora', system-ui, sans-serif;
}
```

Utility classes (mirror the Android components exactly):

```css
.aegis-bg { background: linear-gradient(180deg, var(--sage-pale), var(--sage-light), var(--sage-soft)); }
.glass-card { background: var(--glass); border: 1px solid var(--glass-border);
  backdrop-filter: blur(16px); border-radius: var(--r-lg); box-shadow: var(--glass-shadow); }
.glass-dark { background: var(--glass-dark); border: 1px solid var(--glass-dark-border);
  backdrop-filter: blur(14px); border-radius: var(--r-md); color: #fff; }
.btn-primary { background: var(--forest-dark); color: #fff; border-radius: 16px;
  font-family: var(--font); font-weight: 600; padding: 14px 22px; }
.btn-sos { background: var(--danger); color: #fff; border-radius: 16px;
  font-weight: 800; letter-spacing: 0.5px; text-transform: uppercase; }
.chip { height: 42px; padding: 0 16px; border-radius: var(--r-pill); font-weight: 600; }
.chip[data-selected="true"] { background: var(--forest-dark); color: #fff; }
.chip[data-selected="false"] { background: var(--glass); border: 1px solid var(--glass-border); color: var(--ink); }
.status-pill { display: inline-flex; align-items: center; gap: 5px; border-radius: var(--r-pill);
  padding: 5px 11px; font-size: 11px; font-weight: 600; }
.risk-bar { height: 12px; border-radius: var(--r-pill);
  background: linear-gradient(90deg, var(--safe), var(--caution), var(--danger)); }
/* Floating SOS nav: fixed bottom-center pill + raised red circle */
.sos-nav { position: fixed; bottom: 12px; left: 50%; transform: translateX(-50%);
  background: rgba(13,36,27,0.94); border: 1px solid rgba(255,255,255,0.12);
  border-radius: 34px; padding: 10px 12px; display: flex; gap: 8px; box-shadow: 0 16px 32px rgba(13,36,27,0.45); }
.sos-trigger { position: absolute; top: -14px; left: 50%; transform: translateX(-50%);
  width: 58px; height: 58px; border-radius: 50%; background: var(--danger);
  border: 3px solid rgba(255,255,255,0.9); display: grid; place-items: center; }
```

**Command-center variant:** swap the base colors to §2.4 (deep slate) and keep every class above — the glass language is identical.

---

## 16. Where This Lives in Code

```
aegis-android/app/src/main/java/com/example/aegis/
├── theme/Color.kt        ← all color + glass tokens
├── theme/Type.kt         ← Sora type scale
├── theme/Theme.kt        ← Material light/dark schemes
├── data/MockData.kt      ← zone/status/post mock data (risk bands live here)
└── ui/components/AegisComponents.kt  ← glass cards, pills, nav, SOS overlay,
                                        risk meter, QR, avatars (the reference impl)
```

**Rule for teammates:** if you change a token here, change it in `DESIGN.md` too — this document is the contract across Android, Web and Contracts.
