# Changelog

Each entry below is the exact text published on the corresponding version page
on Modrinth: https://modrinth.com/plugin/custom_enchants

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
