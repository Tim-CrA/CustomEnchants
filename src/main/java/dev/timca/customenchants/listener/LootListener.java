package dev.timca.customenchants.listener;

import dev.timca.customenchants.CustomEnchantsPlugin;
import dev.timca.customenchants.enchant.CustomEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LootListener implements Listener {

    private boolean enabled;
    private List<LootEntry> entries;
    private final Random random = new Random();

    public LootListener() {
        reload();
    }

    public void reload() {
        FileConfiguration config = CustomEnchantsPlugin.getInstance().getConfig();
        this.enabled = config.getBoolean("loot.enabled", true);
        this.entries = new ArrayList<>();
        // loot.entries is a YAML list of maps, so it must be read via getMapList —
        // getConfigurationSection() returns null for lists and the feature died silently.
        for (Map<?, ?> map : config.getMapList("loot.entries")) {
            Object tableObj = map.get("table");
            if (tableObj == null) continue;
            NamespacedKey tableKey = NamespacedKey.fromString(String.valueOf(tableObj));
            if (tableKey == null) continue;
            double chance = map.get("chance") instanceof Number n ? n.doubleValue() : 0.05;
            int maxLevel = map.get("max-level") instanceof Number n ? n.intValue() : 1;
            List<CustomEnchant> enchants = new ArrayList<>();
            if (map.get("enchantments") instanceof List<?> list) {
                for (Object o : list) {
                    CustomEnchant ench = CustomEnchant.byKey(String.valueOf(o));
                    if (ench != null) enchants.add(ench);
                }
            }
            if (!enchants.isEmpty()) {
                entries.add(new LootEntry(tableKey, chance, maxLevel, enchants));
            }
        }
        if (enabled) {
            CustomEnchantsPlugin.getInstance().getLogger()
                    .info("Loot: loaded " + entries.size() + " loot table entries");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        if (!enabled) return;
        if (event.getLootTable() == null) return;
        NamespacedKey tableKey = event.getLootTable().getKey();
        for (LootEntry entry : entries) {
            if (!entry.table.equals(tableKey)) continue;
            if (random.nextDouble() >= entry.chance) continue;
            CustomEnchant enchant = entry.enchants.get(random.nextInt(entry.enchants.size()));
            int level = entry.maxLevel > 1 ? random.nextInt(entry.maxLevel) + 1 : 1;
            event.getLoot().add(CustomEnchant.createBook(enchant, level));
        }
    }

    private record LootEntry(NamespacedKey table, double chance, int maxLevel, List<CustomEnchant> enchants) {}
}