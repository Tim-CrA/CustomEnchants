package dev.timca.customenchants.listener;

import dev.timca.customenchants.CustomEnchantsPlugin;
import dev.timca.customenchants.enchant.CustomEnchant;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FarmerListener implements Listener {

    private boolean enabled;
    private static final Map<Material, Material> PLANT_MAP = new HashMap<>();

    static {
        PLANT_MAP.put(Material.WHEAT, Material.WHEAT_SEEDS);
        PLANT_MAP.put(Material.CARROTS, Material.CARROT);
        PLANT_MAP.put(Material.POTATOES, Material.POTATO);
        PLANT_MAP.put(Material.BEETROOTS, Material.BEETROOT_SEEDS);
        PLANT_MAP.put(Material.NETHER_WART, Material.NETHER_WART);
    }

    public FarmerListener() {
        reload();
    }

    public void reload() {
        FileConfiguration config = CustomEnchantsPlugin.getInstance().getConfig();
        this.enabled = config.getBoolean("farmer.enabled", true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR || !item.getType().name().endsWith("_HOE")) return;
        if (!CustomEnchant.hasEnchant(item, CustomEnchant.FARMER)) return;

        Block block = event.getBlock();
        Material type = block.getType();
        Material seed = PLANT_MAP.get(type);
        if (seed == null) return;
        if (!(block.getBlockData() instanceof Ageable ageable)) return;
        if (ageable.getAge() < ageable.getMaximumAge()) return;

        event.setCancelled(true);
        List<ItemStack> drops = new java.util.ArrayList<>(block.getDrops(item));
        block.setType(Material.AIR);
        boolean seedFound = false;
        for (ItemStack drop : drops) {
            if (!seedFound && drop.getType() == seed && drop.getAmount() > 0) {
                drop.setAmount(drop.getAmount() - 1);
                seedFound = true;
            }
            if (drop.getAmount() > 0) {
                player.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), drop);
            }
        }
        item.damage(1, player);
        if (seedFound) {
            block.setType(type, false);
            if (block.getBlockData() instanceof Ageable a) {
                a.setAge(0);
                block.setBlockData(a, false);
            }
        }
    }
}