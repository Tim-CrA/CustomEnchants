# Changelog

Each entry below is the exact text published on the corresponding version page
on Modrinth: https://modrinth.com/plugin/custom_enchants

## What's new in 2.1.0

### New
- **`/cenchant apply <enchantment> [level]`** — apply an enchantment straight to the item in your main hand (no book needed)
- **PlaceholderAPI support**: `%customenchants_level_<enchantment>%`, `%customenchants_has_<enchantment>%` and `%customenchants_list%` — the levels of the enchantments on the item in your hand
- **Legacy server support (1.16.5–1.20.4)**: on cores without the custom-enchantment registry the levels are stored in each item's persistent data (shown as a lore line). Apply enchantments with `/cenchant apply`. Notes: Magnet requires 1.17+, the Harpoon cooldown fog requires Paper

## What's new in 2.0.0 — the global update

### One jar for every modern server
- Works on **Paper, Purpur, Spigot and forks** from **1.20.5 up to 26.2** (the latest release)
- On Paper the enchantments register natively; on Spigot and forks they install as a world datapack on first launch — restart the server once and they are live
- Same features on every core: no more Paper-only builds

### New enchantments
- **Lumberjack** — chops down a whole tree with one axe hit (leaves are broken along the way and drop saplings/apples via loot tables)
- **Excavator** — digs 3×3 areas with a shovel (level III digs 3 layers deep)
- **Insight** — mobs killed with an Insight sword drop more experience (multiplier per level, configurable)

### Fixes
- Structure loot chests now actually contain enchanted books — the loot config section was never read, so the feature was silently dead
- Harpoon tridents enchanted by players (enchanting table, anvil, books) now get the vanilla cooldown overlay too, not only tridents given via commands
- Every Harpoon trident now receives the cooldown automatically, so the fog works no matter where the trident came from
- Pulled players no longer take fall or wall-smash damage — protection now covers the landing as well
- Blocked Harpoon throws now show the remaining cooldown on the trident immediately (plus a soft sound) instead of failing silently
- Area enchantments (Drill, Excavator, VeinMiner, Lumberjack) now respect region protection: plugins like WorldGuard can veto the extra blocks, and logging plugins see them
- No more free fall-damage immunity after Harpooning a mob — only the pulled player is protected
- Unbreaking now works on extra blocks broken by area enchantments
- Harpoon no longer leaks memory when a trident misses or a player logs out

### Improvements
- Drill and Excavator now only break blocks mineable with their tool (a shovel can no longer break stone)
- Drill and VeinMiner now drop proper experience for mined ores
- Magnet now works on all mining tools (axe, shovel, hoe, shears) and no longer pulls items into armor slots
- Farmer now supports sweet berries (right-click harvest, like vanilla) and cocoa; replanting keeps cocoa facing
- Farmer drops can be collected by Magnet on the same tool
- Drill, Excavator and VeinMiner now share one mining pipeline (consistent drops, XP, smelting and durability)
- Sneaking (Shift) temporarily disables any area enchantment (Drill, Excavator, VeinMiner, Lumberjack)
- Enchantments can appear in structure loot chests (configurable)
- Everything is configurable via `config.yml`, `/cenchant reload` applies changes immediately

## What's new in 1.2.1

### Fixes
- Structure loot chests now actually contain enchanted books — the loot config section was never read, so the feature was silently dead
- Harpoon tridents enchanted by players (enchanting table, anvil, books) now get the vanilla cooldown overlay too, not only tridents given via commands
- Area enchantments (Drill, Excavator, VeinMiner, Lumberjack) now respect region protection: plugins like WorldGuard can veto the extra blocks, and logging plugins see them
- No more free fall-damage immunity after Harpooning a mob — only the pulled player is protected
- Unbreaking now works on extra blocks broken by area enchantments
- Harpoon no longer leaks memory when a trident misses or a player logs out

### Improvements
- Farmer now supports sweet berries and cocoa; replanting keeps cocoa facing
- Farmer drops can be collected by Magnet on the same tool
- Drill, Excavator and VeinMiner now share one mining pipeline (consistent drops, XP, smelting and durability)

## What's new in 1.2.0

### New enchantments
- **Lumberjack** — chops down a whole tree with one axe hit (leaves are broken along the way and drop saplings/apples via loot tables)
- **Excavator** — digs 3×3 areas with a shovel (level III digs 3 layers deep)
- **Insight** — mobs killed with an Insight sword drop more experience (multiplier per level, configurable)

### Improvements
- Harpoon cooldown now shows as the vanilla cooldown overlay **only on the enchanted trident** — regular tridents are no longer affected and stay throwable
- Drill and Excavator now only break blocks mineable with their tool (a shovel can no longer break stone)
- Drill and VeinMiner now drop proper experience for mined ores
- Magnet now works on all mining tools (axe, shovel, hoe, shears)
- Sneaking (Shift) temporarily disables any area enchantment (Drill, Excavator, VeinMiner, Lumberjack)

### Config
- New sections: `lumberjack`, `excavator`, `insight`
- Harpoon cooldown per level via `harpoon.cooldown-seconds`

### Bug fixes
- Fixed enchantment tags so all enchants appear in the enchanting table and villager trades
- Fixed dead loot table reference (`mineshaft` → `abandoned_mineshaft`)
- Fixed VeinMiner vein size limit (off-by-one)
- Fixed `/cenchant reload` not reloading every enchantment listener

## What's new in 1.1.0

### New enchantments
- **Smelter** — automatically smelts ores on mining (iron, gold, copper, ancient debris)
- **Farmer** — auto-replants crops when harvesting with a hoe
- **Harpoon** — launches you toward your trident's landing point; pulls entities toward you

### Improvements
- Magnet now works on axe, shovel, and hoe in addition to pickaxe
- Drill now integrates with Magnet and Smelter
- Enchantments can now be found in structure loot chests (configurable chances)

### Config
- All enchantments are fully configurable via `config.yml`
- Loot table entries configurable per structure

## 1.0.0

Initial release: Drill, Magnet, VeinMiner.
