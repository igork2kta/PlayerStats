## 📝 Player Stats - Changelog 1.13.0 - 2026-11-19

### 🌟 Added
- **GUI Ability Toggles & Badges**: Added an on/off toggle button to the stats screen and an "acquired" badge to visually highlight unlocked abilities.
- **In-Game Chat Feedback**: Added informative chat feedback messages when attributes are upgraded, reset, or points are granted/adjusted.
- **Native NeoForge Configuration**: Integrated a native configuration screen and backend spec for NeoForge users.

### 🏗️ Architecture & Platform
- **MultiLoader Template Migration**: Refactored and modernized the codebase into a standard MultiLoader project structure (`common`, `fabric`, `neoforge`), supporting both **Fabric** (Fabric Loader 0.16.9+) and **NeoForge** (21.1.203+) on Minecraft 1.21.1.

### 🐛 Fixed
- **Per-Entity Stat Isolation**: Fixed attribute points and spent levels tracking so that data is strictly isolated per entity, preventing desync or accidental stat sharing between players and tamed pets/mounts.
- **Infinite Ice / Ice Conjurer**:
  - Fixed a critical bug where triggering the Ice Conjurer ability could delete or replace the item held in the player's main hand.
  - Resolved inventory interactions, slot desyncs, and general stability issues around Infinite Ice.
- **Pet & Entity AI**:
  - Fixed the `Defend Owner` AI goal interfering with standard mob/pet behavior routines.
  - Fixed the Wolf `Howl Buff` ability activation and mechanics.
- **Server Shutdown Crash**: Prevented a potential error thrown when unloading server configurations during shutdown.

### 🎨 UI & Localization
- **Visual Improvements**: Polished the stats screen dark background texture for better readability and contrast.
- **Localization**: Added missing translation keys for both English (`en_us`) and Brazilian Portuguese (`pt_br`).

### ⚙️ Build & System
- **Fabric 1.21 Support**: Expanded Fabric dependency metadata (`~1.21`) to officially support both Minecraft 1.21 and 1.21.1.
- Fixed archive artifact naming to prevent duplicated Minecraft versions in compiled JAR files.
- Fixed the Gradle Maven publishing task when `local_maven_url` is undefined.

