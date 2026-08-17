package dev.timca.customenchants.listener;

import dev.timca.customenchants.CustomEnchantsPlugin;
import dev.timca.customenchants.enchant.CustomEnchant;
import dev.timca.customenchants.util.MiningUtils;
import dev.timca.customenchants.util.SmeltUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class MagnetListener implements Listener {

    private static final int STORAGE_SLOTS = 36;

    private boolean enabled;
    private boolean particles;

    public MagnetListener() {
        reload();
    }

    public void reload() {
        FileConfiguration config = CustomEnchantsPlugin.getInstance().getConfig();
        this.enabled = config.getBoolean("magnet.enabled", true);
        this.particles = config.getBoolean("magnet.particles", true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir() || !MiningUtils.isMiningTool(item.getType())) return;

        boolean hasMagnet = CustomEnchant.hasEnchant(item, CustomEnchant.MAGNET);
        boolean hasSmelter = CustomEnchant.hasEnchant(item, CustomEnchant.SMELTER);
        if (!hasMagnet && !hasSmelter) return;

        if (hasSmelter) {
            for (Item drop : event.getItems()) {
                Material smelted = SmeltUtils.getSmelted(drop.getItemStack().getType());
                if (smelted != null) {
                    drop.getItemStack().setType(smelted);
                }
            }
        }

        if (!hasMagnet) return;

        PlayerInventory inv = player.getInventory();
        List<Item> toRemove = new ArrayList<>();
        boolean changed = false;
        for (Item drop : event.getItems()) {
            ItemStack stack = drop.getItemStack();
            int originalAmount = stack.getAmount();
            int remaining = addToStorage(inv, stack, originalAmount);
            if (remaining <= 0) {
                toRemove.add(drop);
                changed = true;
            } else if (remaining < originalAmount) {
                stack.setAmount(remaining);
                changed = true;
            }
        }
        for (Item d : toRemove) event.getItems().remove(d);
        if (changed && particles) {
            player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0);
        }
    }

    // Collect only into the 36 storage slots — armor and offhand are untouched
    private int addToStorage(PlayerInventory inv, ItemStack stack, int amount) {
        int max = stack.getMaxStackSize();
        int remaining = amount;
        for (int i = 0; i < STORAGE_SLOTS && remaining > 0; i++) {
            ItemStack current = inv.getItem(i);
            if (current == null || current.getType().isAir()) {
                int add = Math.min(remaining, max);
                ItemStack copy = stack.clone();
                copy.setAmount(add);
                inv.setItem(i, copy);
                remaining -= add;
            } else if (current.isSimilar(stack)) {
                int space = max - current.getAmount();
                if (space > 0) {
                    int add = Math.min(remaining, space);
                    current.setAmount(current.getAmount() + add);
                    remaining -= add;
                }
            }
        }
        return remaining;
    }
}
