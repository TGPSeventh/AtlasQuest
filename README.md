# AtlasQuest 🛡️⚔️

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.10-blue.svg)]()
[![Compose](https://img.shields.io/badge/jetpack%20compose-Material%203-orange.svg)]()
[![Room](https://img.shields.io/badge/room-2.7.2-green.svg)]()

**AtlasQuest** is an offline-first RPG habit and side-quest tracker built in Kotlin and Jetpack Compose. It gamifies personal productivity and self-improvement through the philosophy of **"Side Quest Maxing"** — collating, organizing, and completing spontaneous or recurring quests while leveling up your real-life attributes.

---

## ✨ Core Product Mechanics & Features

### 1. Zero-Punishment Progression
* **No Penalties:** Missed days or skipped habits **never** subtract stats, XP, or player levels. Real life is demanding enough; AtlasQuest celebrates consistency without punishment.
* **Low-Energy Fallbacks:** Main quests feature optional low-energy fallback actions that still award reduced XP (**60%**, min **10 XP**) and **+1 stat point**, ensuring forward momentum even on low-energy days.

### 2. Effort Tiers (`EffortTier`)
* **Light:** +1 stat point, 15 base XP
* **Standard:** +2 stat points, 30 base XP
* **Heavy:** +3 stat points, 50 base XP
* *Daily Cap:* Enforces a daily full-reward cap of 12 completions to encourage sustainable habits.

### 3. 6-Axis Stat System & Dynamic Archetypes
* **6 Stat Axes (`StatAxis`):**
  * `STR` (Strength)
  * `VIT` (Vitality)
  * `INT` (Intellect)
  * `FOC` (Focus)
  * `DIS` (Discipline)
  * `EXP` (Exploration)
* **Custom Radial Spider Web Chart (`RadialStatWebChart`):** A custom Jetpack Compose `Canvas` radar chart visualizing your attribute distribution across 0–10 tier rating rings.
* **Dynamic Archetypes:** Automatically resolves titles like *Vanguard*, *Iron Sentinel*, *Arcane Scholar*, *Focus Weaver*, *Pathrunner*, *Wayfinder*, *Spellsword*, and *Atlas Polymath* based on your stat distribution.

### 4. Quest Formats (Single-Line & Checklists)
* **Single-Line Quests:** Quick tasks for spontaneous side quests.
* **Checklist Quests:** Multi-step routines with interactive checkboxes for epic habits and daily workflows.

---

## 🏗️ Architecture & Package Layout

AtlasQuest follows a clean, offline-first MVVM architecture:

```text
com.seventh.atlasquest/
│
├── data/                         # Data Layer (Room Database, DAOs, Repositories)
├── domain/                       # Pure Domain Layer (Models, Progression Engine)
└── ui/                           # Presentation Layer (Jetpack Compose, ViewModels, Screens)
    ├── board/                    # Active Quest Board & Fallbacks
    ├── backlog/                  # Side Quest Backlog & Creation Dialog
    ├── character/                # Character Sheet & Radial Spider Web Chart
    └── theme/                    # Material 3 Theme
```

---

## 🛠️ Tech Stack

* **Build System:** Android Gradle Plugin (AGP) `9.5.3` (native Kotlin compilation)
* **Language:** Kotlin `2.2.10`
* **UI Toolkit:** Jetpack Compose (Material 3) + Compose BOM `2026.02.01`
* **Persistence & Annotation Processing:** Room `2.7.2` + KSP `2.2.10-2.0.2`
* **Lifecycle & State:** Lifecycle Compose `2.9.0` (`StateFlow`)
* **SDK Targets:** `minSdk = 26`, `compileSdk = 37`, `targetSdk = 37`

---

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/TGPSeventh/AtlasQuest.git
   ```
2. Open the project in **Android Studio** (Koala / Ladybug or newer with AGP 9.0+ support).
3. Sync Gradle and run the app on an Android emulator or physical device.

---

## 📄 License

Distributed under the MIT License. See [LICENSE](LICENSE) for more information.
