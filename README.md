# 🛡️ Player Stats
A lightweight and highly configurable progression and RPG system for Minecraft

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20%7C%201.21%20%7C%201.21.1-brightgreen)
![Loaders](https://img.shields.io/badge/Loaders-Forge%20%7C%20NeoForge-orange)
![Java](https://img.shields.io/badge/Java-17%20%7C%2021-blue)

> **Note:** Available for **Minecraft 1.20.1** (Forge/NeoForge) and **Minecraft 1.21 / 1.21.1** (NeoForge). For 1.21.x, a single unified build works seamlessly on both 1.21 and 1.21.1.

Gain upgrade points by defeating mobs, customize player and companion stats, unlock unique abilities, use magical items, and even revive fallen companions or players.

Fully supports modded mobs and attributes through a flexible configuration system.

---

## 📦 Overview
**Player Stats** introduces a customizable RPG progression system where players earn upgrade points by killing mobs.  
Unlike traditional mods, attributes are loaded dynamically — meaning that custom attributes from mods such as *Iron’s Spells ‘n Spellbooks*, *Epic Fight*, and others are fully supported and can be upgraded.

Additionally, Player Stats features a **Companion/Entity progression system**, **Unique Abilities**, and **Special Items** to expand your gameplay.

---

## 🎯 Features
- ⚔️ **Mob-Based Progression:** Earn attribute points by defeating bosses, high-health mobs, or configured creatures. Milestone kill rewards at 100 kills and every 1,000 kills.
- 🛠️ **Dynamic Attribute Upgrades:** Allocate points directly into player or entity stats with live search and filter options in the UI.
- 📊 **Rich Informative Tooltips:** Hover over any attribute to see exact gain per point, next-level preview, natural base value, upgrade bonuses, active boost scrolls, and the origin mod name (*Minecraft*, *Iron's Spells*, etc.).
- 🐾 **Companion & Mount Progression:** Inspect and improve the stats and special skills of tamed animals, mounts, or allied golems.
- 🌟 **Unique Abilities & Leveled Skills:** Unlock and upgrade passive and active abilities (e.g., Vampirism life-steal, Howl buff, Scout Vision, Milky Aura, Rebirth, Ice Conjuring).
- 🎛️ **Ability Toggling & Tooltips:** Right-click unlocked abilities in the UI to toggle them On/Off anytime without losing invested levels. Informative tooltips display dynamic values and stats per level.
- 📜 **Items & Runes:** Upgrade runes, attribute boost scrolls with temporary buffs, ability crystals, soul stones, and temporary infinite ice.
- 🕯️ **Rebirth Ritual:** Revive fallen pets, mobs, or hardcore players using Soul Fragments and Soul Stones on top of a charged Respawn Anchor.
- 🔄 **Configurable Resets:** Reset allocated attributes anytime by paying configurable XP levels.
- ⚙️ **In-Game & File Configuration:** Full compatibility with the **Configured** mod for intuitive in-game settings, alongside detailed TOML configs for drop chances, XP costs, boost durations/multipliers, ignored attributes, and custom increments.
- 🧪 **Debug Mode & Slash Commands:** Easily inspect mob and attribute IDs, reload configs without restarting, or manage player points via commands.

---

## 🎮 How It Works

### 1. Earning Points & Upgrades
- Killing bosses (Wither, Ender Dragon, Warden, Elder Guardian) or mobs above the configured health threshold grants a chance to receive attribute points.
- Kill counter milestones (100 kills, 1000, 2000, etc.) grant extra points.
- Consumable items like **Upgrade Rune** and **Ability Crystal** grant points directly.

### 2. Default Keybinds
| Key | Action | Description |
| :---: | :--- | :--- |
| **`R`** | Open Player Stats Screen | Opens your character's upgrade and abilities GUI. |
| **`H`** | Open Entity Stats Screen | Opens stats GUI for the entity you are aiming at (or your current mount). |
| **`V`** | Activate Ice Ability | Activates the temporary Infinite Ice conjurer ability (once unlocked). |

*(All keybinds can be rebound under Minecraft's Controls menu).*

#### 🎬 Gameplay & Attribute Showcase:
[![Player Stats Demonstration](https://img.youtube.com/vi/Lh-G0W7TR-s/hqdefault.jpg)](https://youtu.be/Lh-G0W7TR-s)
*(Click above to watch the demonstration on YouTube)*

---

## ✨ Unique Abilities
Abilities can be unlocked for players or entities using Ability Points:

| Ability | Applicable Entities | Levels | Description |
| :--- | :--- | :---: | :--- |
| **Vampirism** | Players | 1 – 5 | Has a chance (30% to 50%) on hit to restore 10% to 18% of dealt damage as health. |
| **Life Regen** | Non-player entities | 1 | Gives permanent passive health regeneration. |
| **Follow Owner** | Horses, Donkeys, Mules, Camels, Iron Golems | 1 | Makes the creature follow its owner. |
| **Teleport to Owner** | Mounts, Iron Golems | 1 | Teleports the creature to the owner if it falls too far behind *(requires Follow Owner)*. |
| **Defend Owner** | Iron Golems | 1 | Causes the golem to prioritize and attack entities targeting its owner. |
| **Frost Walker** | Horses, Donkeys, Mules, Camels | 1 | Freezes water into ice under the mount's hooves when walking. |
| **Patrol** | Iron Golems, Wolves | 1 | Sets the entity to guard and patrol around its current position. |
| **Taunt** | Iron Golems, Wolves | 1 | Forces nearby hostile monsters to temporarily target them upon engaging in combat. |
| **Howl Buff** | Wolves | 1 – 3 | Howls when entering combat (5 min cooldown), granting Strength, Speed, and Resistance to allies in an 8 to 16-block radius for 30s to 60s (potency increases at Level 3). |
| **Scout Vision** | Parrots | 1 – 3 | Detects and highlights nearby monsters with a glowing outline in a 16, 24, or 32-block radius (works when perching or on player shoulders). |
| **Milky Aura** | Cows, Mooshrooms | 1 – 3 | Clears negative status effects from allies in a 3, 5, or 7-block radius around the cow. |
| **Ice Conjurer** | Players | 1 | Allows activating temporary infinite ice placement by pressing V. |
| **Rebirth** | All creatures & Players | 1 | Drops a **Soul Fragment** upon death containing the entity's data, allowing resurrection. |

> **Tip:** You can **Right-Click** any unlocked ability in the UI to toggle it **Active / Inactive** at will without losing upgrade levels. Unlocked but deactivated abilities and unpurchased abilities are clearly distinguished by color, with full details shown on hover tooltips.

#### 🐺 Wolf Howl Buff Preview:
[![Wolf Howl Buff Demonstration](https://img.youtube.com/vi/7Ek2acyz-_k/hqdefault.jpg)](https://youtu.be/7Ek2acyz-_k)
*(Click above to watch the Howl Buff in combat)*

---

## 🔮 Items & Rebirth Ritual

- **Upgrade Rune (`playerstats:upgrade_rune`):** Ancient rune that awards +1 attribute point on use.
- **Ability Crystal (`playerstats:ability_crystal`):** Imbued crystal that awards +1 ability point on use.
- **Attribute Boost Scroll (`playerstats:attribute_boost_scroll`):** Grants a temporary multiplier bonus to a random attribute for a limited duration.
- **Infinite Ice (`playerstats:infinite_ice`):** Summons temporary blocks of ice that evaporate without flooding water after several seconds.
- **Soul Fragment (`playerstats:soul_fragment`):** Dropped by an entity or player that has the Rebirth ability when they die. Stores their inventory and stats.
- **Soul Stone (`playerstats:soul_stone`):** Used in the ritual of rebirth.

### 🕯️ The Rebirth Ritual
To revive a fallen companion or player:
1. Charge a **Respawn Anchor** with glowstone.
2. Drop the **Soul Fragment** and a **Soul Stone** together on top of the anchor.
3. Bring a sacrifice mob within 1.5 blocks:
   - For **players** (Hardcore spectator recovery): requires a **Villager** sacrifice.
   - For **mobs / pets**: requires a mob of the **same entity type**.
4. ⚠️ *The ritual consumes vital energy from the caster, leaving them near death.*

#### 🕯️ Watch the Rebirth Ritual in Action:
[![Rebirth Ritual Demonstration](https://img.youtube.com/vi/dgLRhEgf-BU/hqdefault.jpg)](https://youtu.be/dgLRhEgf-BU)
*(Click above to watch the resurrection ritual on YouTube)*

---

## 💻 Commands
All commands require OP/permission level 2:

- `/playerstats addattributepoints <amount> [target]` — Adds attribute points to yourself or a target player.
- `/playerstats removeattributepoints <amount> [target]` — Removes attribute points.
- `/playerstats addabilitypoints <amount> [target]` — Adds ability points.
- `/playerstats removeabilitypoints <amount> [target]` — Removes ability points.
- `/playerstats set <attribute_id> <value> [target]` — Directly sets an attribute modifier value.
- `/playerstats reloadconfig` — Reloads custom mob drop chances, ignored attributes, and custom increments without restarting the game.

---

## 🔧 Configuration

> 💡 **In-Game Configuration:** Player Stats is fully integrated with **[Configured](https://www.curseforge.com/minecraft/mc-mods/configured)**! You can easily adjust all settings, drop rates, and custom attribute increments directly in-game through clean, translated menus with helpful tooltips and add buttons.

Configurations are split between:
- `playerstats-common.toml` (Client/Common settings):
  - `debugMode`: Enables logging and in-game debug messages for mob IDs and attribute registry keys.
- `playerstats-server.toml` (World/Server settings):
  - `resetOnDeath`: Whether attributes reset when a player dies.
  - `highHealthAmount`: Minimum mob health to qualify as a high-health target (default: `50`).
  - `witherChance`, `enderDragonChance`, `wardenChance`, `elderGuardianChance`, `highHealthChance`: Drop chances for points.
  - `customMobChances`: List of mob IDs and their point drop chances (`["minecraft:creeper=0.05", "mymod:boss=0.80"]`).
  - `customAttributeIncrement`: Custom upgrade step values per point (`["minecraft:generic.max_health=2.0"]`).
  - `ignoredAttributes`: Attributes hidden from the upgrade interface.
  - `consumeExperience`, `experienceCostIncrement`, `requiredXpForReset`, `requiredXpForAbility`: XP economy settings.
  - `boostAmountMinMultiplier`, `boostAmountMaxMultiplier`, `boostDurationMinMinutes`, `boostDurationMaxMinutes`: Boost scroll settings.

### Example Server Config (`playerstats-server.toml`):
```toml
[geral]
resetOnDeath = false
highHealthAmount = 50

[mob_chances]
witherChance = 0.5
enderDragonChance = 0.5
wardenChance = 0.3
elderGuardianChance = 0.5
highHealthChance = 0.05
customMobChances = [
    "minecraft:chicken=0.01",
    "mymod:dungeon_boss=0.75"
]

[attribute_settings]
customAttributeIncrement = [
    "attribute.name.generic.max_health=2.0",
    "attribute.name.generic.attack_damage=0.5"
]
ignoredAttributes = [
    "attribute.name.generic.armor",
    "forge.name_tag_distance",
    "forge.entity_gravity",
    "forge.step_height",
    "attribute.name.generic.armor_toughness"
]

[experience]
consumeExperience = true
experienceCostIncrement = 5
requiredXpForReset = 50
requiredXpForAbility = 30

[attribute_boost]
boostAmountMinMultiplier = 1
boostAmountMaxMultiplier = 3
boostDurationMinMinutes = 10
boostDurationMaxMinutes = 30
```

---

## 🧩 Compatibility
- **Minecraft Versions:** Available for **1.20.1**, **1.21**, and **1.21.1**.
- **Mod Loaders:** Compatible with **Forge** (1.20.1) and **NeoForge** (1.20.1 / 21.0.x / 21.1.x+).
- **In-Game Configs:** Full support for **Configured** and built-in Forge/NeoForge configuration menus.
- **Progressive Bosses:** Integrated support for modded Wither variants.
- **Custom Attributes & Modded Mobs:** Automatically recognizes attributes from mods like *Iron’s Spells ‘n Spellbooks*, *Epic Fight*, and any standard Forge/NeoForge mod.
- **Dedicated Servers:** Fully synchronized across client and server with low network footprint.

---

## 📜 License
This mod is licensed under the [MIT License](LICENSE).  
You are free to use it in modpacks, forks, and redistributions — credit to the original project is appreciated!
