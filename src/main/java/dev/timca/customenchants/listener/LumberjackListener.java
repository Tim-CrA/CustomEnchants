package dev.timca.customenchants.listener;

import dev.timca.customenchants.CustomEnchantsPlugin;
import dev.timca.customenchants.enchant.CustomEnchant;
import dev.timca.customenchants.util.MiningUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
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

public class LumberjackListener implements Listener {

    private boolean enabled;
    private boolean sneakBypass;
    private boolean breakLeaves;
    private int leavesRadius;
    private int maxTreeSize;

    public LumberjackListener() {
        reload();
    }

    public void reload() {
        FileConfiguration config = CustomEnchantsPlugin.getInstance().getConfig();
        this.enabled = config.getBoolean("lumberjack.enabled", true);
        this.sneakBypass = config.getBoolean("lumberjack.sneak-bypass", true);
        this.breakLeaves = config.getBoolean("lumberjack.break-leaves", true);
        this.leavesRadius = config.getInt("lumberjack.leaves-radius", 5);
        this.maxTreeSize = config.getInt("lumberjack.max-tree-size", 256);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        if (sneakBypass && player.isSneaking()) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir() || !item.getType().name().endsWith("_AXE")) return;
        if (!CustomEnchant.hasEnchant(item, CustomEnchant.LUMBERJACK)) return;

        Block origin = event.getBlock();
        if (!isLog(origin.getType())) return;

        boolean hasMagnet = CustomEnchant.hasEnchant(item, CustomEnchant.MAGNET);

        List<Block> logs = findTree(origin);
        Set<Block> leaves = breakLeaves ? findLeaves(logs) : Set.of();

        int broken = 0;
        for (Block b : logs) {
            if (b.equals(origin)) continue;
            if (!isLog(b.getType())) continue;
            Collection<ItemStack> drops = b.getDrops(item);
            b.setType(Material.AIR);
            broken++;
            dropOrCollect(player, b, drops, hasMagnet);
        }
        for (Block b : leaves) {
            if (b.getType() == Material.AIR) continue;
            // Leaves don't drop themselves without shears, but their loot table
            // yields saplings and apples
            Collection<ItemStack> leafDrops = b.getDrops(item);
            b.setType(Material.AIR);
            dropOrCollect(player, b, leafDrops, hasMagnet);
        }
        if (broken > 0) item.damage(broken, player);
    }

    private boolean isLog(Material type) {
        String name = type.name();
        return name.endsWith("_LOG") || name.endsWith("_WOOD");
    }

    private boolean isLeaves(Material type) {
        return type.name().endsWith("_LEAVES");
    }

    private List<Block> findTree(Block origin) {
        List<Block> tree = new ArrayList<>();
        Queue<Block> queue = new ArrayDeque<>();
        Set<Block> visited = new HashSet<>();
        queue.add(origin);
        visited.add(origin);

        // +1: the origin is broken by the regular event
        while (!queue.isEmpty() && tree.size() < maxTreeSize + 1) {
            Block current = queue.poll();
            tree.add(current);
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block neighbor = current.getRelative(x, y, z);
                        if (visited.contains(neighbor)) continue;
                        if (isLog(neighbor.getType())) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
        return tree;
    }

    private Set<Block> findLeaves(List<Block> logs) {
        Set<Block> leaves = new HashSet<>();
        // BFS over leaves starting from the logs, but no further than leavesRadius steps from the trunk
        Queue<Block> queue = new ArrayDeque<>(logs);
        Set<Block> visited = new HashSet<>(logs);
        java.util.Map<Block, Integer> dist = new java.util.HashMap<>();
        for (Block log : logs) dist.put(log, 0);

        while (!queue.isEmpty() && leaves.size() < maxTreeSize * 2) {
            Block current = queue.poll();
            int d = dist.getOrDefault(current, 0);
            if (d >= leavesRadius) continue;
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block neighbor = current.getRelative(x, y, z);
                        if (visited.contains(neighbor)) continue;
                        if (!isLeaves(neighbor.getType())) continue;
                        visited.add(neighbor);
                        dist.put(neighbor, d + 1);
                        leaves.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
        return leaves;
    }

    private void dropOrCollect(Player player, Block block, Collection<ItemStack> drops, boolean magnet) {
        for (ItemStack d : drops) {
            if (magnet) MiningUtils.addToInventory(player, d);
            else player.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), d);
        }
    }
}
