# Changelog

## 1.2.0

### New enchantments
- **Lumberjack** — chop down entire trees in one hit with an axe (optional leaf breaking, configurable radius)
- **Excavator** — dig 3x3 areas with a shovel (level III digs 3 layers deep)
- **Insight** — mobs drop more experience when killed (configurable multiplier per level)

### Improvements
- Harpoon cooldown now shows as the vanilla cooldown overlay **only on the enchanted trident** — regular tridents stay throwable
- Drill and Excavator now only break blocks mineable with their tool (a shovel can no longer break stone)
- Drill and VeinMiner now drop proper experience for mined ores
- Magnet now works on all mining tools (axe, shovel, hoe, shears) and no longer pulls items into armor slots
- Smelter blacklist is now actually respected
- `/cenchant reload` now reloads every enchantment listener

### Config
- New sections: `lumberjack`, `excavator`, `insight`
- Harpoon cooldown per level via `harpoon.cooldown-seconds`

### Bug fixes
- Fixed enchantment tags so all enchants appear in the enchanting table and villager trades
- Fixed dead loot table reference (`mineshaft` → `abandoned_mineshaft`)
- Fixed VeinMiner vein size limit (off-by-one)

## 1.1.0

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

- Initial release: Drill, Magnet, VeinMiner
