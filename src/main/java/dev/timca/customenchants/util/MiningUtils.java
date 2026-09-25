package dev.timca.customenchants.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/** Shared logic for block-breaking enchantments (Drill, VeinMiner, Magnet). */
public final class MiningUtils {

    /** XP ranges for breaking blocks (vanilla values, no fortune). */
    private static final Map<Material, int[]> BLOCK_XP = new HashMap<>();

    static {
        BLOCK_XP.put(Material.COAL_ORE, new int[]{0, 2});
        BLOCK_XP.put(Material.DEEPSLATE_COAL_ORE, new int[]{0, 2});
        BLOCK_XP.put(Material.DIAMOND_ORE, new int[]{3, 7});
        BLOCK_XP.put(Material.DEEPSLATE_DIAMOND_ORE, new int[]{3, 7});
        BLOCK_XP.put(Material.EMERALD_ORE, new int[]{3, 7});
        BLOCK_XP.put(Material.DEEPSLATE_EMERALD_ORE, new int[]{3, 7});
        BLOCK_XP.put(Material.LAPIS_ORE, new int[]{2, 5});
        BLOCK_XP.put(Material.DEEPSLATE_LAPIS_ORE, new int[]{2, 5});
        BLOCK_XP.put(Material.REDSTONE_ORE, new int[]{1, 5});
        BLOCK_XP.put(Material.DEEPSLATE_REDSTONE_ORE, new int[]{1, 5});
        BLOCK_XP.put(Material.NETHER_QUARTZ_ORE, new int[]{2, 5});
        BLOCK_XP.put(Material.NETHER_GOLD_ORE, new int[]{0, 1});
        BLOCK_XP.put(Material.SPAWNER, new int[]{15, 43});
    }

    private MiningUtils() {
    }

    /** Tools that can receive Magnet (enchantable/mining tag). */
    public static boolean isMiningTool(Material type) {
        String name = type.name();
        return name.endsWith("_PICKAXE") || name.endsWith("_AXE")
                || name.endsWith("_SHOVEL") || name.endsWith("_HOE")
                || type == Material.SHEARS;
    }

    /**
     * Mining area blocks: level 3x3 layers starting at origin along the facing direction.
     * Used by Drill (pickaxe) and Excavator (shovel).
     */
    public static List<Block> getAreaBlocks(Block origin, BlockFace face, int level) {
        List<Block> list = new ArrayList<>();
        for (int i = 0; i < level; i++) {
            Block layer = origin.getRelative(face, i);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    Block relative;
                    if (face == BlockFace.UP || face == BlockFace.DOWN) relative = layer.getRelative(dx, 0, dy);
                    else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) relative = layer.getRelative(dx, dy, 0);
                    else relative = layer.getRelative(0, dy, dx);
                    if (!list.contains(relative)) list.add(relative);
                }
            }
        }
        return list;
    }

    /** Player facing direction, rounded to a block face. */
    public static BlockFace getFacing(Player player) {
        float pitch = player.getLocation().getPitch();
        if (pitch > 45) return BlockFace.DOWN;
        if (pitch < -45) return BlockFace.UP;
        return player.getFacing();
    }

    /**
     * Whether the block is mineable with this tool according to the vanilla
     * minecraft:mineable/* tags. Blocks outside the tag (stone for a shovel, etc.)
     * must not be broken by Drill/Excavator.
     */
    public static boolean isMineableWith(Material block, String mineableTagName) {
        Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS,
                NamespacedKey.minecraft(mineableTagName), Material.class);
        return tag != null && tag.isTagged(block);
    }

    // Marks the thread while a synthetic BlockBreakEvent is being dispatched,
    // so our own listeners skip it and don't recurse.
    private static final ThreadLocal<Boolean> SYNTHETIC = ThreadLocal.withInitial(() -> false);

    /** True while a plugin-fired synthetic BlockBreakEvent is being dispatched. */
    public static boolean isSyntheticBreak() {
        return SYNTHETIC.get();
    }

    /**
     * Fires a nested BlockBreakEvent so protection plugins (WorldGuard etc.) can
     * veto and logging plugins can see extra blocks broken by area enchantments.
     */
    public static boolean canBreakExtra(Block block, Player player) {
        BlockBreakEvent nested = new BlockBreakEvent(block, player);
        SYNTHETIC.set(true);
        try {
            Bukkit.getPluginManager().callEvent(nested);
        } finally {
            SYNTHETIC.set(false);
        }
        return !nested.isCancelled();
    }

    /** Simulates Unbreaking: each damage point is absorbed with level/(level+1) chance. */
    public static void damageTool(ItemStack tool, int amount, Player player) {
        int unbreaking = tool.getEnchantmentLevel(Enchantment.UNBREAKING);
        int toApply = 0;
        for (int i = 0; i < amount; i++) {
            if (ThreadLocalRandom.current().nextInt(unbreaking + 1) == 0) toApply++;
        }
        if (toApply > 0) tool.damage(toApply, player);
    }

    /**
     * Shared pipeline for extra blocks broken by area enchantments (Drill, Excavator,
     * VeinMiner): filter → region-protection check → drops (smelted if requested) →
     * XP → air → magnet pickup or natural drop → tool durability.
     */
    public static int breakExtraBlocks(Player player, ItemStack tool, List<Block> blocks,
                                       Set<Material> blacklist, Predicate<Material> filter,
                                       boolean smelt, boolean magnet) {
        int broken = 0;
        for (Block b : blocks) {
            Material type = b.getType();
            if (type.isAir() || blacklist.contains(type) || !filter.test(type)) continue;
            if (!canBreakExtra(b, player)) continue;

            ItemStack dropsTool = tool.clone();
            if (smelt) dropsTool.removeEnchantment(Enchantment.SILK_TOUCH);
            Collection<ItemStack> drops = b.getDrops(dropsTool);
            if (smelt) drops = smelt(drops);
            dropExperience(b, player, smelt);
            b.setType(Material.AIR);
            broken++;
            for (ItemStack d : drops) {
                if (magnet) addToInventory(player, d);
                else player.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), d);
            }
        }
        if (broken > 0) damageTool(tool, broken, player);
        return broken;
    }

    /** Smelts drops via SmeltUtils, honoring the blacklist and custom recipes. */
    public static Collection<ItemStack> smelt(Collection<ItemStack> drops) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack d : drops) {
            Material smelted = SmeltUtils.getSmelted(d.getType());
            if (smelted != null) result.add(new ItemStack(smelted, d.getAmount()));
            else result.add(d);
        }
        return result;
    }

    /** Puts an item into the player's inventory, dropping any excess nearby. */
    public static void addToInventory(Player player, ItemStack stack) {
        player.getInventory().addItem(stack).values().forEach(extra ->
                player.getWorld().dropItemNaturally(player.getLocation(), extra));
    }

    /**
     * Spawns XP for a block broken outside of BlockBreakEvent.
     * No XP for smelted drops or with Silk Touch — same as vanilla.
     */
    public static void dropExperience(Block block, Player player, boolean smelted) {
        if (smelted) return;
        int[] range = BLOCK_XP.get(block.getType());
        if (range == null) return;
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.containsEnchantment(Enchantment.SILK_TOUCH)) return;
        int xp = range[0] == range[1]
                ? range[0]
                : range[0] + ThreadLocalRandom.current().nextInt(range[1] - range[0] + 1);
        if (xp <= 0) return;
        block.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), ExperienceOrb.class,
                orb -> orb.setExperience(xp));
    }
}
