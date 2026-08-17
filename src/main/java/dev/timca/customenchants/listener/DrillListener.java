package dev.timca.customenchants.listener;

import dev.timca.customenchants.CustomEnchantsPlugin;
import dev.timca.customenchants.enchant.CustomEnchant;
import dev.timca.customenchants.util.MiningUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DrillListener implements Listener {

    private final Set<Material> blacklist = new HashSet<>();
    private boolean enabled;
    private boolean sneakBypass;

    public DrillListener() {
        reload();
    }

    public void reload() {
        FileConfiguration config = CustomEnchantsPlugin.getInstance().getConfig();
        this.enabled = config.getBoolean("drill.enabled", true);
        this.sneakBypass = config.getBoolean("drill.sneak-bypass", true);
        this.blacklist.clear();
        for (String s : config.getStringList("drill.blacklist")) {
            try {
                blacklist.add(Material.valueOf(s.toUpperCase()));
            } catch (IllegalArgumentException ignore) {}
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        if (sneakBypass && player.isSneaking()) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR || !item.getType().name().endsWith("_PICKAXE")) return;
        if (!CustomEnchant.hasEnchant(item, CustomEnchant.DRILL)) return;
        int level = CustomEnchant.getLevel(item, CustomEnchant.DRILL);
        if (level < 1) return;

        Block origin = event.getBlock();
        if (blacklist.contains(origin.getType())) return;
        // Pickaxes may only break blocks from minecraft:mineable/pickaxe
        if (!MiningUtils.isMineableWith(origin.getType(), "mineable/pickaxe")) return;
        boolean hasSmelter = CustomEnchant.hasEnchant(item, CustomEnchant.SMELTER);
        boolean hasMagnet = CustomEnchant.hasEnchant(item, CustomEnchant.MAGNET);

        BlockFace face = MiningUtils.getFacing(player);
        List<Block> toBreak = MiningUtils.getAreaBlocks(origin, face, level);
        int broken = 0;
        for (Block b : toBreak) {
            if (b.equals(origin)) continue;
            if (blacklist.contains(b.getType())) continue;
            if (b.getType() == Material.AIR) continue;
            if (!MiningUtils.isMineableWith(b.getType(), "mineable/pickaxe")) continue;

            ItemStack dropsTool = item.clone();
            if (hasSmelter) dropsTool.removeEnchantment(Enchantment.SILK_TOUCH);
            Collection<ItemStack> drops = b.getDrops(dropsTool);
            if (hasSmelter) drops = MiningUtils.smelt(drops);
            MiningUtils.dropExperience(b, player, hasSmelter);
            b.setType(Material.AIR);
            broken++;
            if (hasMagnet) {
                for (ItemStack d : drops) MiningUtils.addToInventory(player, d);
            } else {
                for (ItemStack d : drops) player.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), d);
            }
        }
        if (broken > 0) item.damage(broken, player);
    }
}
