package dev.timca.customenchants.listener;

import dev.timca.customenchants.CustomEnchantsPlugin;
import dev.timca.customenchants.enchant.CustomEnchant;
import dev.timca.customenchants.util.MiningUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
        PLANT_MAP.put(Material.COCOA, Material.COCOA_BEANS);
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
        if (!enabled || MiningUtils.isSyntheticBreak()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir() || !item.getType().name().endsWith("_HOE")) return;
        if (!CustomEnchant.hasEnchant(item, CustomEnchant.FARMER)) return;

        Block block = event.getBlock();
        Material type = block.getType();
        Material seed = PLANT_MAP.get(type);
        if (seed == null) return;
        if (!(block.getBlockData() instanceof Ageable ageable)) return;
        if (ageable.getAge() < ageable.getMaximumAge()) return;

        boolean magnet = CustomEnchant.hasEnchant(item, CustomEnchant.MAGNET);

        event.setCancelled(true);
        // Keep the original block data (e.g. cocoa facing) to restore the crop after replanting
        BlockData before = block.getBlockData();
        List<ItemStack> drops = new java.util.ArrayList<>(block.getDrops(item));
        block.setType(Material.AIR);
        boolean seedFound = false;
        for (ItemStack drop : drops) {
            if (!seedFound && drop.getType() == seed && drop.getAmount() > 0) {
                drop.setAmount(drop.getAmount() - 1);
                seedFound = true;
            }
            if (drop.getAmount() > 0) {
                if (magnet) MiningUtils.addToInventory(player, drop);
                else player.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), drop);
            }
        }
        MiningUtils.damageTool(item, 1, player);
        if (seedFound && before instanceof Ageable a) {
            a.setAge(0);
            block.setBlockData(a, false);
        }
    }

    /**
     * Sweet berries are harvested by right-click in vanilla (no breaking, the bush
     * stays). With Farmer or Magnet on the hoe the berries go straight to the
     * inventory; otherwise vanilla behavior applies.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBerryInteract(PlayerInteractEvent event) {
        if (!enabled || MiningUtils.isSyntheticBreak()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.SWEET_BERRY_BUSH) return;
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir() || !item.getType().name().endsWith("_HOE")) return;
        boolean farmer = CustomEnchant.hasEnchant(item, CustomEnchant.FARMER);
        boolean magnet = CustomEnchant.hasEnchant(item, CustomEnchant.MAGNET);
        if (!farmer && !magnet) return;
        if (!(block.getBlockData() instanceof Ageable ageable)) return;
        if (ageable.getAge() < 1) return;

        event.setCancelled(true);
        java.util.Collection<ItemStack> drops = block.getDrops(item);
        ageable.setAge(1);
        block.setBlockData(ageable, false);
        for (ItemStack d : drops) {
            if (magnet) MiningUtils.addToInventory(player, d);
            else player.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), d);
        }
        MiningUtils.damageTool(item, 1, player);
    }
}