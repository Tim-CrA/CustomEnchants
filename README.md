# Custom Enchants

Custom Enchants is a Minecraft plugin that adds new enchantments beyond what vanilla Minecraft offers. Each enchantment is natively integrated — it works just like a built-in one, available through enchanting tables, anvils, and enchanted books.

The plugin is actively developed. New enchantments are added with each update.

## Compatibility

- Works on **Paper, Purpur, Spigot, Folia and forks** from **1.16.5** up to the latest release (26.3)
- On Paper (1.20.5+) the enchantments register natively; on Spigot and forks they install as a world datapack on first launch — restart the server once and they are live
- On **1.16.5–1.20.4** (legacy cores) the enchantments are stored per-item instead of the registry: apply them with `/cenchant apply` (no enchanting table), shown as a lore line. Magnet requires 1.17+, the Harpoon cooldown fog requires Paper
- Requires Java 21+ on modern cores (Minecraft 1.20.5+ already needs it); legacy cores need Java 17+

## Features

- New enchantments that feel native to the game
- Works with enchanting tables, anvils, and books
- Configurable via `config.yml`
- Admin command for giving enchanted items
- Enchantments can appear in structure loot chests (configurable)
- Sneak (Shift) to temporarily disable any area enchantment and break a single block
- Regularly updated with new enchantments

## Enchantments

| Enchantment | Applies to | Max level | Description |
|-------------|------------|-----------|-------------|
| Drill | Pickaxe | III | Mines a 3×3 area (level III digs 3 layers deep) |
| Magnet | Pickaxe, Axe, Shovel, Hoe, Shears | I | Automatically collects drops into your inventory |
| Smelter | Pickaxe | I | Automatically smelts ores on mining (custom recipes supported) |
| Farmer | Hoe | I | Auto-replants crops on harvest |
| Harpoon | Trident | III | Launches you toward the landing point, or pulls the hit entity to you; cooldown shows only on the enchanted trident |
| VeinMiner | Pickaxe | I | Mines an entire ore vein in one hit |
| Lumberjack | Axe | I | Chops down a whole tree in one hit (leaves break too, dropping saplings and apples) |
| Excavator | Shovel | III | Digs a 3×3 area (level III digs 3 layers deep) |
| Insight | Sword | II | Mobs killed with this sword drop more experience |

## Commands

- `/cenchant give <enchantment> [level]` — give an enchanted book
- `/cenchant list` — list all available enchantments
- `/cenchant reload` — reload the config

> Permission: `customenchants.admin` (default: op)

## Installation

1. Drop the `.jar` into your `plugins/` folder
2. Restart the server
3. On Spigot and forks, restart once more after the first launch (the enchantments install as a world datapack)
4. All enchantments are available immediately

## Requirements

- Paper, Purpur, Spigot or any fork — **1.16.5+**
- Modern cores (1.20.5+): Java 21+ · legacy cores (1.16.5–1.20.4): Java 17+
- Java 21+
