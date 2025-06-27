# 🛡️ Player Stats

   A lightweight and highly configurable progression system for Minecraft players.  
   Gain upgrade points by defeating mobs — then choose which attributes you want to improve.  
   Fully supports modded mobs and attributes with a powerful configuration system.

---

## 📦 Overview

   Player Stats is a Forge mod that introduces a customizable RPG-like system where players receive upgrade points after killing mobs.  
   Unlike other mods of the same style, attributes are loaded dynamically. This means that attributes added by mods like Iron’s Spells ‘n Spellbooks and Epic Fight can be upgraded.
   Built with modded compatibility, performance, and configurability in mind.

---

## 🎯 Features

   - ✅ Gain upgrade points from killing mobs
   - 🛠️ Manually allocate points to any supported attribute
   - ⚙️ Configure which mobs give points and their drop chances
   - 🧠 Smart caching of config values for better performance
   - 🧪 Debug mode to help identify entity and attribute names
   - 🔁 Support for custom attributes (modded or vanilla)
   - 🧟 Fine-tuned chance for bosses like the Wither, Ender Dragon, Warden, etc.
   - 🚫 Ignore certain attributes from being shown
   - 🔧 Easy configuration via `playerstats-common.toml`

---


## 🎮 How It Works

   1. Kill a mob (configured mob or boss)→ a chance to receive 1 upgrade point
   2. Points are saved per player
   3. Press your configured key (default: `R`) to open the upgrade screen
   4. Choose the attribute you want to improve at cost of points and experience

---

## 🔧 Example Config

   ```toml
   [geral]
   debugMode = true
   highHealthAmount = 50
   customMobChances = ["entity.minecraft.chicken=0.15", "entity.mymod.bossmob=0.75"]
   customAttributeIncrement = ["attribute.name.generic.max_health=2.0"]
   ignoredAttributes = ["attribute.name.generic.armor"]

⚔️ Supported Entities
   Fully supports vanilla and modded mobs
   Add your own via customMobChances in the config
   Enable debugMode = true to see mob IDs when killing them (logged and shown in chat)

🧩 Modded Compatibility
   ✅ Works with:
      Custom mobs (as long as they use standard EntityType)
      Custom attributes from other mods (e.g. Epic Fight, Iron’s Spells ‘n Spellbooks and Epic Fight, etc.)
      Forge server environments

🧑‍💻 For Modpacks & Developers
   Mod ID: playerstats
   No mixins or coremod hacks
   Lightweight runtime
   Easy to configure and extend

📜 License
This mod is licensed under the MIT License.
You're free to use it in modpacks, forks, and redistribution — just credit the original project.



