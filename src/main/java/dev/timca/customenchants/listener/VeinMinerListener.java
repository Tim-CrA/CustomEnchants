package dev.timca.customenchants.listener;

import dev.timca.customenchants.CustomEnchantsPlugin;
import dev.timca.customenchants.enchant.CustomEnchant;
import dev.timca.customenchants.util.MiningUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class VeinMinerListener implements Listener {

    private boolean enabled;
    private boolean sneakBypass;
    private int maxVeinSize;
    private final Set<Material> allowedMaterials = new HashSet<>();

    public VeinMinerListener() {
        reload();
    }

    public void reload() {
        FileConfiguration config = CustomEnchantsPlugin.getInstance().getConfig();
        this.enabled = config.getBoolean("veinminer.enabled", true);
        this.sneakBypass = config.getBoolean("veinminer.sneak-bypass", true);
        this.maxVeinSize = config.getInt("veinminer.max-vein-size", 64);
        this.allowedMaterials.clear();
        for (String s : config.getStringList("veinminer.allowed-materials")) {
            try {
                allowedMaterials.add(Material.valueOf(s.toUpperCase()));
            } catch (IllegalArgumentException ignore) {
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        if (sneakBypass && player.isSneaking()) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir() || !item.getType().name().endsWith("_PICKAXE")) return;
        if (!CustomEnchant.hasEnchant(item, CustomEnchant.VEINMINER)) return;

        Block origin = event.getBlock();
        Material type = origin.getType();

        if (!allowedMaterials.contains(type)) return;

        boolean hasSmelter = CustomEnchant.hasEnchant(item, CustomEnchant.SMELTER);
        boolean hasMagnet = CustomEnchant.hasEnchant(item, CustomEnchant.MAGNET);

        List<Block> vein = findVein(origin, type);
        int broken = 0;
        for (Block b : vein) {
            if (b.equals(origin)) continue;
            if (b.getType() != type) continue;

            ItemStack dropsTool = item.clone();
            if (hasSmelter)
                dropsTool.removeEnchantment(Enchantment.SILK_TOUCH);
            Collection<ItemStack> drops = b.getDrops(dropsTool);
            if (hasSmelter) drops = MiningUtils.smelt(drops);
            MiningUtils.dropExperience(b, player, hasSmelter);
            b.setType(Material.AIR);
            broken++;
            if (hasMagnet) {
                for (ItemStack d : drops) MiningUtils.addToInventory(player, d);
            } else {
                for (ItemStack d : drops)
                    player.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), d);
            }
        }
        if (broken > 0) item.damage(broken, player);
    }

    private List<Block> findVein(Block origin, Material type) {
        List<Block> vein = new ArrayList<>();
        Queue<Block> queue = new ArrayDeque<>();
        Set<Block> visited = new HashSet<>();
        queue.add(origin);
        visited.add(origin);

        // +1: the origin takes one spot in the limit but is broken by the regular event
        while (!queue.isEmpty() && vein.size() < maxVeinSize + 1) {
            Block current = queue.poll();
            vein.add(current);

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block neighbor = current.getRelative(x, y, z);
                        if (visited.contains(neighbor)) continue;
                        if (neighbor.getType() == type) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
        return vein;
    }
}
