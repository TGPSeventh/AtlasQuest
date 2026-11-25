# AtlasQuest - Change Log & Architecture Overview

This document summarizes the overall architecture, core features, packages, and code changes implemented for **AtlasQuest** (`com.seventh.atlasquest`), an offline-first RPG habit and side-quest tracker built in Kotlin and Jetpack Compose.

---

## 🏗️ Architectural Overview & Package Layout

AtlasQuest follows a clean, offline-first layered architecture combining Model-View-ViewModel (MVVM) with a Feature-First UI structure:

```text
com.seventh.atlasquest/
│
├── data/                         # Data Layer
│   ├── local/                    # Room Database, DAOs, Entities & Converters
│   │   ├── AtlasQuestEntities.kt # PlayerProfileEntity, QuestEntity, QuestLogEntity
│   │   ├── AtlasQuestDaos.kt     # PlayerProfileDao, QuestDao, QuestLogDao
│   │   ├── AtlasQuestDatabase.kt # Room Database & Type Converters
│   │   └── StarterQuestSeeder.kt # Starter quests & profile seeding
│   └── repository/
│       └── AtlasQuestRepository.kt # Single source of truth repository & completion logic
│
├── domain/                       # Pure Domain Layer (No Android/Compose/Room dependencies)
│   ├── model/
│   │   └── QuestEnums.kt         # QuestKind, QuestStatus, StatAxis, EffortTier
│   └── progression/
│       └── ProgressionEngine.kt  # XP curves, daily caps (12), non-punitive streaks & Archetype resolver
│
├── ui/                           # Presentation Layer (Jetpack Compose & ViewModels)
│   ├── AtlasQuestViewModel.kt    # Unified StateFlow UI state holder
│   ├── board/
│   │   └── QuestBoardScreen.kt   # Active quest board & zero-punishment fallback actions
│   ├── backlog/
│   │   └── QuestBacklogScreen.kt # Side quest backlog, archive, and creation dialog
│   ├── character/
│   │   ├── CharacterSheetScreen.kt # Character sheet with level, XP bar, and stat breakdown
│   │   └── RadialStatWebChart.kt # Custom Canvas 6-axis spider web stat chart (0-10 scale rings)
│   └── theme/                    # Material 3 Theme (Color.kt, Theme.kt, Type.kt)
│
└── MainActivity.kt               # Entry Activity with Bottom Navigation wiring
```

---

## ✨ Core Features & Mechanics Implemented

### 1. Zero-Punishment Progression & Effort Tiers
- **Effort Tiers (`EffortTier`):**
  - `LIGHT`: +1 stat point, 15 base XP
  - `STANDARD`: +2 stat points, 30 base XP
  - `HEAVY`: +3 stat points, 50 base XP
- **Non-Punitive Fallbacks:** Missed days or skipped quests never subtract stats, XP, or levels. Main quests include fallback actions that award reduced XP (60%, min 10 XP) and `+1` stat point.
- **Daily Cap:** Enforces a daily full-reward cap of 12 completions with graceful dimishing rewards beyond.

### 2. 6-Axis Stat System & Archetype Resolver
- **6 Axes (`StatAxis`):** `STR` (Strength), `VIT` (Vitality), `INT` (Intellect), `FOC` (Focus), `DIS` (Discipline), and `EXP` (Exploration).
- **Dynamic Archetypes:** Automatically resolves player titles (e.g., *Vanguard*, *Iron Sentinel*, *Arcane Scholar*, *Focus Weaver*, *Pathrunner*, *Wayfinder*, *Spellsword*, *Atlas Polymath*, *Wandering Novice*) based on stat axis distribution.
- **Radial Spider Web Chart:** Custom Jetpack Compose `Canvas` renderer rendering concentric rating rings (`0–10` tier scale) and filled stat polygons.

### 3. Quest Management
- **Side Quests:** Spontaneous or one-off quests flowing from `BACKLOG` → `ACTIVE` → `COMPLETED`.
- **Main Quests:** Recurring habits and routines with trigger cues and low-energy fallback actions.

---

## 🛠️ Tech Stack & Build Configuration

- **Build Toolchain:** AGP `9.5.3` with native Kotlin compilation (built-in Kotlin support enabled) & KSP `2.2.10-2.0.2`.
- **Kotlin & Compose:** Kotlin `2.2.10`, Compose BOM `2026.02.01`, Lifecycle Compose `2.9.0`.
- **Persistence:** Room `2.7.2` with type converters.
- **SDK Targets:** `minSdk = 26`, `compileSdk = 37`, `targetSdk = 37`.
- **Gradle Properties:** Configured `android.disallowKotlinSourceSets=false` to ensure KSP generated source sets integrate cleanly with AGP 9.x built-in Kotlin.
