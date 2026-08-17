# CustomEnchants

## Description
A custom enchantments plugin for Paper Minecraft servers.

## Enchantments
- **Drill** (III) — mine 3x3 areas with a pickaxe
- **Magnet** (I) — automatically picks up drops (works on pickaxes, axes, shovels, hoes, and shears)
- **Smelter** (I) — automatically smelts mined drops (custom recipes supported!)
- **Farmer** (I) — auto-harvests and replants crops
- **Harpoon** (III) — pulls you to your trident's landing point (or the entity to you); the cooldown is shown as the vanilla overlay only on the enchanted trident
- **VeinMiner** (I) — mines an entire ore vein in one hit
- **Lumberjack** (I) — chops down entire trees with a single axe hit (optional leaf breaking)
- **Excavator** (III) — digs 3x3 areas with a shovel (only breaks blocks mineable with a shovel)
- **Insight** (II) — increases the experience dropped by mobs

Sneak (Shift) to temporarily disable Drill/Excavator/VeinMiner/Lumberjack and break a single block.

## Commands
- `/cenchant give <key> [level]` — give an enchanted book
- `/cenchant list` — list all enchantments
- `/cenchant reload` — reload the config (applies immediately)

Permission: `customenchants.admin` (granted to op by default)

## Build system
The project uses **Gradle** with the Kotlin DSL.

### Building the plugin
```bash
.\gradlew.bat clean build
```
(on Linux/Mac: `./gradlew clean build`)

The finished JAR will be at: `build/libs/CustomEnchants-{version}.jar`

## Requirements
- JDK 21
- Paper 1.21.11+ (or forks)

## Configuration
Configuration file: `src/main/resources/config.yml`

### Custom Smelter Recipes
In `config.yml`:
```yaml
smelter:
  custom-recipes:
    SAND: GLASS
    COBBLESTONE: STONE
    OAK_LOG: CHARCOAL
```

### VeinMiner settings
In `config.yml`:
```yaml
veinminer:
  enabled: true
  max-vein-size: 64
  allowed-materials:
    - IRON_ORE
    - DEEPSLATE_IRON_ORE
    # ... other blocks
```

### Insight settings
In `config.yml`:
```yaml
insight:
  enabled: true
  xp-multiplier:
    1: 1.5
    2: 2.0
```

## Project structure
```
CustomEnchants/
├── src/
│   └── main/
│       ├── java/dev/timca/customenchants/
│       │   ├── CustomEnchantsPlugin.java
│       │   ├── CustomEnchantsBootstrapper.java
│       │   ├── enchant/CustomEnchant.java
│       │   ├── listener/
│       │   │   ├── DrillListener.java
│       │   │   ├── MagnetListener.java
│       │   │   ├── VeinMinerListener.java
│       │   │   └── ...
│       │   ├── command/GiveEnchantCommand.java
│       │   └── util/
│       │       ├── SmeltUtils.java
│       │       └── MiningUtils.java
│       └── resources/
│           ├── paper-plugin.yml
│           ├── config.yml
│           └── data/
├── build.gradle.kts
└── gradle/
    └── wrapper/
```
