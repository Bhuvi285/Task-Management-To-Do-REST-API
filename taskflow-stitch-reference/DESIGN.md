---
name: TaskFlow Fluid Precision
colors:
  surface: '#0f131d'
  surface-dim: '#0f131d'
  surface-bright: '#353944'
  surface-container-lowest: '#0a0e18'
  surface-container-low: '#171b26'
  surface-container: '#1c1f2a'
  surface-container-high: '#262a35'
  surface-container-highest: '#313540'
  on-surface: '#dfe2f1'
  on-surface-variant: '#c7c4d7'
  inverse-surface: '#dfe2f1'
  inverse-on-surface: '#2c303b'
  outline: '#908fa0'
  outline-variant: '#464554'
  surface-tint: '#c0c1ff'
  primary: '#c0c1ff'
  on-primary: '#1000a9'
  primary-container: '#8083ff'
  on-primary-container: '#0d0096'
  inverse-primary: '#494bd6'
  secondary: '#adc6ff'
  on-secondary: '#002e6a'
  secondary-container: '#0566d9'
  on-secondary-container: '#e6ecff'
  tertiary: '#ddb7ff'
  on-tertiary: '#490080'
  tertiary-container: '#b76dff'
  on-tertiary-container: '#400071'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e1e0ff'
  primary-fixed-dim: '#c0c1ff'
  on-primary-fixed: '#07006c'
  on-primary-fixed-variant: '#2f2ebe'
  secondary-fixed: '#d8e2ff'
  secondary-fixed-dim: '#adc6ff'
  on-secondary-fixed: '#001a42'
  on-secondary-fixed-variant: '#004395'
  tertiary-fixed: '#f0dbff'
  tertiary-fixed-dim: '#ddb7ff'
  on-tertiary-fixed: '#2c0051'
  on-tertiary-fixed-variant: '#6900b3'
  background: '#0f131d'
  on-background: '#dfe2f1'
  surface-variant: '#313540'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.03em
  display-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-xl:
    fontFamily: Plus Jakarta Sans
    fontSize: 36px
    fontWeight: '600'
    lineHeight: 44px
    letterSpacing: -0.025em
  headline-xl-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: -0.005em
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0em
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.005em
  label-md:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.04em
  code-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-md: 1.5rem
  gutter-lg: 2rem
  margin: 1rem
  margin-md: 1.5rem
  margin-lg: 2.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style
The design system embodies the standard of elite modern productivity software: hyper-responsive, authoritative, and optically pristine. Designed for product-led engineering teams, technical leaders, and operators who live in their workflows, it balances technical density with effortless visual calm. 

The aesthetic synthesizes modern minimalism, subtle atmospheric glassmorphism, and structural utility. Every interaction communicates instantaneous speed and tactile predictability:
- **Tone & Demeanor:** Focused, frictionless, hyper-crafted, and quietly powerful.
- **Visual Tension:** Dense structural data grids paired with generous, atmospheric spatial buffers.
- **Signature Detailing:** Ultra-fine 1px borders, localized glowing state indicators, balanced luminous depth, and micro-interactions tuned for high-velocity keyboard navigation.

## Colors
The color architecture relies on a luminous dark foundation designed to minimize ocular fatigue during extended use, complemented by an equally calibrated light canvas.

### Palette Architecture
- **Primary (Violet/Indigo):** `#6366F1` (Interactive focus, active states, key commands) stepping down to `#4F46E5` on pressed interactions.
- **Secondary (Electric Blue):** `#3B82F6` and `#2563EB` (Informational cues, secondary task actions, links).
- **Accents:** Soft Purple (`#A855F7`) for AI/automation indicators; Vivid Cyan (`#06B6D4`) for live telemetries and real-time multiplayer presences.
- **Semantic Feedback:** Emerald (`#10B981`) for completed statuses; Amber (`#F59E0B`) for blocked or delayed workflows; Coral Red (`#EF4444`) for critical defects and breaking events.

### Theme Surface Rules
- **Dark Mode (Default):** Canvas rests on `#0B0F19`. Card surfaces leverage `#111827` (Base Layer) and `#1E293B` (Elevated Layer). Structural borders maintain an exact `rgba(255, 255, 255, 0.08)`.
- **Light Mode:** Canvas shifts to crisp off-white `#F8FAFC`. Card surfaces use pure `#FFFFFF` with structural borders set to `#E2E8F0`.

## Typography
The system couples the architectural geometry of Plus Jakarta Sans for titles and headers with the neutral utilitarian legibility of Inter for operational data.

### Structural Directives
- **Tabular Figures:** Always apply OpenType tabular numbers (`tnum`) across all tabular views, sprint estimations, task identifiers, and timestamp metadata to ensure absolute column alignment.
- **Hierarchy Separation:** Section anchors and modal titles require `Plus Jakarta Sans` semi-bold (`600`) or bold (`700`) weights. Dense context data, metadata pills, task trees, and table payloads must run purely on `Inter`.
- **Micro-Scale Legibility:** The `label-sm` scale is optimized for uppercase status counters, keyboard shortcut combinations (`kbd`), and sprint tags with expanded tracking (`0.04em`).

## Layout & Spacing
TaskFlow employs a dynamic 12-column responsive fluid grid anchored by strict operational density. 

### Grid Configuration
- **Desktop (>= 1280px):** 12-column layout, `margin-lg` (40px) outer edge margin, `gutter-lg` (32px) intra-column distribution. Persistent collapsible primary navigation bar (240px wide).
- **Tablet (768px - 1279px):** 8-column layout, `margin-md` (24px) outer margin, `gutter-md` (24px) gutters. Nav bar converts to a narrow icon dock (64px wide).
- **Mobile (< 768px):** 4-column layout, `margin` (16px) outer margin, `gutter` (16px) gutters. Modals convert to responsive bottom sheets, and Kanban columns convert to swipeable segmented views.

### Spacing Density Principles
- **Macro Boundaries:** Use `space-xl` and `space-lg` exclusively for main workspace view segmentation and container separation.
- **Component Geometry:** Use `space-sm` (8px) and `space-md` (16px) for intra-card content, stack layouts, and grouped controls. Use `space-xs` (4px) for inline metadata, avatar stacks, and badge icon groupings.

## Elevation & Depth
Depth in this system avoids heavy opaque drop shadows, prioritizing translucent layer separation, specular hairline borders, and ambient light bleeds.

### Surface Tiers & Blurring
- **Layer 0 (Canvas):** `#0B0F19` base workspace floor.
- **Layer 1 (Cards & Lists):** Semi-translucent `#111827` backed with `backdrop-filter: blur(16px)` and a subtle border stroke of `1px solid rgba(255, 255, 255, 0.08)`.
- **Layer 2 (Floating Overlays & Popovers):** Elevated `#1E293B` at 92% opacity, complemented by an ambient shadow: `0 12px 32px -4px rgba(0, 0, 0, 0.45)`.
- **Layer 3 (Modals & Command Menus):** Top-level containment utilizing `backdrop-filter: blur(24px)` with a secondary inset rim light: `inset 0 1px 0 0 rgba(255, 255, 255, 0.12)` to establish sharp surface edges.

### Chromatic Glows
Interactive active states (such as active sprint indicators or focused inputs) project a subtle primary ambient glow: `0 0 16px 2px rgba(99, 102, 241, 0.25)`.

## Shapes
A unified rounded geometric aesthetic brings a modern, approachable feel to technical software.

### Corner Radius Mapping
- **Base (0.5rem / 8px):** Checkboxes, form fields, action buttons, table row highlights, and dropdown menu options.
- **Containers (`rounded-lg` / 1rem / 16px):** Kanban lane columns, task overview cards, dashboard graph widgets, and popover dialogs.
- **Full Shells (`rounded-xl` / 1.5rem / 24px):** Workspace modals, main command palette surfaces (`⌘K`), and floating contextual action panels.
- **Full Pill (`rounded-full` / 9999px):** Status badges, user avatar masks, filter chips, and circular icon triggers.

## Components

### Buttons
- **Primary:** Filled `#6366F1` with white text, transitioning to `#4F46E5` on hover. Features an inner top-edge highlight (`inset 0 1px 0 rgba(255,255,255,0.2)`). Height: 36px (default), 32px (compact).
- **Secondary / Ghost:** Transparent background with subtle border `rgba(255,255,255,0.08)`. Hover transitions to `rgba(255,255,255,0.04)`.
- **Icon Buttons:** Fixed aspect ratio (32x32px or 36x36px), rounded 8px (`rounded-base`), centered SVG icon with interactive opacity shift from 0.7 to 1.0 on hover.

### Chips & Badges
- **Status Pills:** Pill-shaped (`rounded-full`), padding `2px 10px`. Rendered with 10% opacity tinted backgrounds of their semantic color, paired with a bright solid 6px dot and solid high-contrast text label (e.g., `#10B981` Emerald for "Completed").
- **Priority Tags:** Minimalist border, subtle neutral badge displaying an iconography indicator (Urgent, High, Normal, Low) accompanied by strict `label-sm` typography.

### Input Fields
- **Default State:** Height 36px, `rounded-base`, `#111827` surface background with `1px solid rgba(255, 255, 255, 0.08)`.
- **Focus State:** Border color shifts smoothly to `#6366F1`, supported by an outer ring glow: `0 0 0 3px rgba(99, 102, 241, 0.2)`. Placeholder text in muted slate (`#64748B`).

### Checkboxes & Radios
- **Checkboxes:** 16x16px square with 4px border radius. Unchecked: `1px solid rgba(255,255,255,0.2)`. Checked: `#6366F1` background with a crisp white check icon and gentle spring micro-animation.
- **Radios:** 16x16px circular shape with matching active fills and inner circular punch-outs.

### Cards & Kanban Containers
- **Task Cards:** Surface `#111827`, border `1px solid rgba(255,255,255,0.08)`, radius `rounded-lg`. Hover creates a subtle elevation transform: `-1px` vertical translate with border brightness brightening to `rgba(255,255,255,0.16)`.
- **Kanban Column Trays:** Surface `#0E1420`, radius `rounded-xl`, padding `space-sm`, continuous structural height, dynamic item count badge pinned to column headers.

### Specialized Workflow Elements
- **Command Palette (`⌘K`):** Floating modal centered on the screen, 640px maximum width, radius `rounded-xl`, high backdrop blur, integrated search input with instantaneous keyboard navigation shortcuts (`↑`, `↓`, `↵`).
- **Keyboard Shortcuts (`kbd`):** Monospaced pill element, `11px`, styled with background `rgba(255, 255, 255, 0.06)`, border `1px solid rgba(255, 255, 255, 0.12)`, and text color `#94A3B8`.