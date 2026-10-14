# 🛡️ Player Stats
A lightweight and highly configurable progression and RPG system for Minecraft

![Minecraft](https://img.shields.io/badge/Minecraft-1.21%20%7C%201.21.1-brightgreen)
![NeoForge](https://img.shields.io/badge/NeoForge-21.0%2B%20%7C%2021.1%2B-orange)
![Java](https://img.shields.io/badge/Java-21-blue)

> **Note:** A single unified build works seamlessly on both **Minecraft 1.21** and **Minecraft 1.21.1** (NeoForge).

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
- 🐾 **Companion & Mount Progression:** Inspect and improve the stats and special skills of tamed animals, mounts, or allied golems.
- 🌟 **Unique Abilities System:** Unlock passive and active abilities (e.g., life regeneration, follow/teleport to owner, patrol area, taunt enemies, scout vision, ice conjuring, and rebirth).
- 📜 **Items & Runes:** Upgrade runes, attribute boost scrolls with temporary buffs, ability crystals, soul stones, and temporary infinite ice.
- 🕯️ **Rebirth Ritual:** Revive fallen pets, mobs, or hardcore players using Soul Fragments and Soul Stones on top of a charged Respawn Anchor.
- 🔄 **Configurable Resets:** Reset allocated attributes anytime by paying configurable XP levels.
- ⚙️ **In-Depth Configuration:** Configure drop chances, XP costs, boost durations/multipliers, ignored attributes, and custom increments via config files.
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

---

## ✨ Unique Abilities
Abilities can be unlocked for players or entities using Ability Points:

| Ability | Applicable Entities | Description |
| :--- | :--- | :--- |
| **Life Regen** | Non-player entities | Gives permanent passive health regeneration. |
| **Follow Owner** | Horses, Donkeys, Mules, Camels, Iron Golems | Makes the creature follow its owner. |
| **Teleport to Owner** | Mounts, Iron Golems | Teleports the creature to the owner if it falls too far behind *(requires Follow Owner)*. |
| **Defend Owner** | Iron Golems | Causes the golem to prioritize and attack entities targeting its owner. |
| **Frost Walker** | Horses, Donkeys, Mules, Camels | Freezes water into ice under the mount's hooves when walking. |
| **Patrol** | Iron Golems, Wolves | Sets the entity to guard and patrol around its current position. |
| **Taunt** | Iron Golems, Wolves | Forces nearby hostile monsters to target them instead of the player. |
| **Howl Buff** | Wolves | Howls when entering combat, granting a strength buff to the wolf and nearby allies. |
| **Scout Vision** | Parrots | Highlights nearby hostile entities (glowing outline), either while perching nearby or on the player's shoulder. |
| **Milky Aura** | Cows, Mooshrooms | Clears potion effects from nearby entities within 2 blocks. |
| **Ice Conjurer** | Players | Allows activating temporary infinite ice placement by pressing `V`. |
| **Rebirth** | All creatures & Players | Drops a **Soul Fragment** upon death containing the entity's data, allowing resurrection. |

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
- **Minecraft Versions:** Compatible with both **1.21** and **1.21.1** within the same `.jar`.
- **Mod Loader:** Built for **NeoForge** (21.0.x / 21.1.x+).
- **Progressive Bosses:** Integrated support for modded Wither variants.
- **Custom Attributes & Modded Mobs:** Automatically recognizes attributes from mods like *Iron’s Spells ‘n Spellbooks*, *Epic Fight*, and any standard Forge/NeoForge mod.
- **Dedicated Servers:** Fully synchronized across client and server with low network footprint.

---

## 📜 License
This mod is licensed under the [MIT License](LICENSE).  
You are free to use it in modpacks, forks, and redistributions — credit to the original project is appreciated!
