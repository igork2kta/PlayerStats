#🛡️ Player Stats
A lightweight and highly configurable progression system for Minecraft players.
Gain upgrade points by defeating mobs — then choose which attributes you want to improve.
Fully supports modded mobs and attributes through a powerful configuration system.

📦 Overview
Player Stats is a Forge mod that introduces a customizable RPG-like system where players earn upgrade points by killing mobs.
Unlike other mods of the same style, attributes are loaded dynamically — meaning that attributes from mods like Iron’s Spells ‘n Spellbooks and Epic Fight can also be upgraded.
Built with compatibility, performance, and configurability in mind.

🎯 Features
✅ Gain upgrade points from killing mobs

🛠️ Manually allocate points to any supported attribute

⚙️ Configure which mobs give points and their drop chances

🧠 Smart caching of config values for better performance

🧪 Debug mode to help identify entity and attribute names

🔁 Supports custom attributes (modded or vanilla)

🧟 Customizable chance for bosses like the Wither, Ender Dragon, Warden, and more

🚫 Ignore specific attributes from appearing in the UI

🔧 Easy configuration via playerstats-common.toml

🎮 How It Works
Kill a mob (configured mob or boss) → chance to earn 1 upgrade point

Points are saved per player

Press your configured key (default: R) to open the upgrade screen

Choose the attribute you want to improve — spending points and XP

🔧 Example Config
toml
Copy
Edit
[geral]
debugMode = true
highHealthAmount = 50
customMobChances = ["entity.minecraft.chicken=0.15", "entity.mymod.bossmob=0.75"]
customAttributeIncrement = ["attribute.name.generic.max_health=2.0"]
ignoredAttributes = ["attribute.name.generic.armor"]
⚔️ Supported Entities
Fully supports both vanilla and modded mobs

Add your own mobs using customMobChances in the config

Enable debugMode = true to print mob IDs to chat and logs when killed

🧩 Modded Compatibility
✅ Works with:

Custom mobs (using standard EntityType)

Custom attributes from other mods (e.g., Epic Fight, Iron’s Spells ‘n Spellbooks)

All standard Forge-based server/client environments

🧑‍💻 For Modpacks & Developers
Mod ID: playerstats

No mixins or coremod hacks

Lightweight and runtime-friendly

Easy to configure and extend

📜 License
This mod is licensed under the MIT License.
You're free to use it in modpacks, forks, and redistribution — just credit the original project.
