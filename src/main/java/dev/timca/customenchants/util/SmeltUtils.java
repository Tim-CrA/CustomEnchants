package dev.timca.customenchants.util;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SmeltUtils {
    private static final Map<Material, Material> DEFAULT_RECIPE = new HashMap<>();
    private static final Map<Material, Material> CUSTOM_RECIPE = new HashMap<>();
    private static final Set<Material> BLACKLIST = new HashSet<>();
    private static boolean enabled = true;

    static {
        DEFAULT_RECIPE.put(Material.RAW_IRON, Material.IRON_INGOT);
        DEFAULT_RECIPE.put(Material.RAW_COPPER, Material.COPPER_INGOT);
        DEFAULT_RECIPE.put(Material.RAW_GOLD, Material.GOLD_INGOT);
        DEFAULT_RECIPE.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
        DEFAULT_RECIPE.put(Material.COBBLESTONE, Material.STONE);
        DEFAULT_RECIPE.put(Material.STONE, Material.SMOOTH_STONE);
        DEFAULT_RECIPE.put(Material.SAND, Material.GLASS);
        DEFAULT_RECIPE.put(Material.RED_SAND, Material.GLASS);
        DEFAULT_RECIPE.put(Material.CLAY, Material.TERRACOTTA);
        DEFAULT_RECIPE.put(Material.NETHERRACK, Material.NETHER_BRICK);
        DEFAULT_RECIPE.put(Material.OAK_LOG, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.SPRUCE_LOG, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.BIRCH_LOG, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.JUNGLE_LOG, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.ACACIA_LOG, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.DARK_OAK_LOG, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.MANGROVE_LOG, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.CHERRY_LOG, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.OAK_WOOD, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.SPRUCE_WOOD, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.BIRCH_WOOD, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.JUNGLE_WOOD, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.ACACIA_WOOD, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.DARK_OAK_WOOD, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.MANGROVE_WOOD, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.CHERRY_WOOD, Material.CHARCOAL);
        DEFAULT_RECIPE.put(Material.SMOOTH_BASALT, Material.BASALT);
        DEFAULT_RECIPE.put(Material.BASALT, Material.SMOOTH_BASALT);
    }

    private SmeltUtils() {
    }

    public static void loadCustomRecipes(FileConfiguration config) {
        enabled = config.getBoolean("smelter.enabled", true);

        BLACKLIST.clear();
        for (String s : config.getStringList("smelter.blacklist")) {
            try {
                BLACKLIST.add(Material.valueOf(s.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        CUSTOM_RECIPE.clear();
        ConfigurationSection section = config.getConfigurationSection("smelter.custom-recipes");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String sourceStr = key.toUpperCase();
            String resultStr = section.getString(key);
            if (resultStr == null) continue;
            try {
                Material source = Material.valueOf(sourceStr);
                Material result = Material.valueOf(resultStr.toUpperCase());
                CUSTOM_RECIPE.put(source, result);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public static @Nullable Material getSmelted(Material source) {
        if (!enabled || BLACKLIST.contains(source)) return null;
        Material custom = CUSTOM_RECIPE.get(source);
        if (custom != null) return custom;
        return DEFAULT_RECIPE.get(source);
    }
}
