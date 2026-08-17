package dev.timca.customenchants.listener;

import dev.timca.customenchants.CustomEnchantsPlugin;
import dev.timca.customenchants.enchant.CustomEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;

import java.util.ArrayList;
import java.util.List;
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
        ConfigurationSection entriesSection = config.getConfigurationSection("loot.entries");
        if (entriesSection == null) return;
        for (String key : entriesSection.getKeys(false)) {
            ConfigurationSection entry = entriesSection.getConfigurationSection(key);
            if (entry == null) continue;
            String tableStr = entry.getString("table");
            if (tableStr == null) continue;
            NamespacedKey tableKey = NamespacedKey.fromString(tableStr);
            if (tableKey == null) continue;
            double chance = entry.getDouble("chance", 0.05);
            int maxLevel = entry.getInt("max-level", 1);
            List<CustomEnchant> enchants = new ArrayList<>();
            for (String enchName : entry.getStringList("enchantments")) {
                CustomEnchant ench = CustomEnchant.byKey(enchName);
                if (ench != null) enchants.add(ench);
            }
            if (!enchants.isEmpty()) {
                entries.add(new LootEntry(tableKey, chance, maxLevel, enchants));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        if (!enabled) return;
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