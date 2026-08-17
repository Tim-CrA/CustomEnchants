package dev.timca.customenchants.listener;

import dev.timca.customenchants.CustomEnchantsPlugin;
import dev.timca.customenchants.enchant.CustomEnchant;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class InsightListener implements Listener {

    private boolean enabled;
    private Map<Integer, Double> multiplier;

    public InsightListener() {
        reload();
    }

    public void reload() {
        FileConfiguration config = CustomEnchantsPlugin.getInstance().getConfig();
        this.enabled = config.getBoolean("insight.enabled", true);
        this.multiplier = new HashMap<>();
        if (config.isConfigurationSection("insight.xp-multiplier")) {
            for (String key : config.getConfigurationSection("insight.xp-multiplier").getKeys(false)) {
                try {
                    multiplier.put(Integer.parseInt(key), config.getDouble("insight.xp-multiplier." + key));
                } catch (NumberFormatException ignored) {}
            }
        }
        if (!multiplier.containsKey(1)) multiplier.put(1, 1.5);
        if (!multiplier.containsKey(2)) multiplier.put(2, 2.0);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!enabled) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon == null) return;
        int level = CustomEnchant.getLevel(weapon, CustomEnchant.INSIGHT);
        if (level < 1) return;

        int base = event.getDroppedExp();
        if (base <= 0) return;

        double mult = multiplier.getOrDefault(level, 1.5);
        int boosted = (int) Math.round(base * mult);
        event.setDroppedExp(Math.max(boosted, base));
    }
}
