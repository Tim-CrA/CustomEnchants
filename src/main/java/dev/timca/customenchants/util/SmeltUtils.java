package dev.timca.customenchants.util;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

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
        // Referenced by name, not by Material constants: newer materials
        // (cherry/mangrove) don't exist on older cores and would crash the class.
        put("RAW_IRON", "IRON_INGOT");
        put("RAW_COPPER", "COPPER_INGOT");
        put("RAW_GOLD", "GOLD_INGOT");
        put("ANCIENT_DEBRIS", "NETHERITE_SCRAP");
        put("COBBLESTONE", "STONE");
        put("STONE", "SMOOTH_STONE");
        put("SAND", "GLASS");
        put("RED_SAND", "GLASS");
        put("CLAY", "TERRACOTTA");
        put("NETHERRACK", "NETHER_BRICK");
        put("OAK_LOG", "CHARCOAL");
        put("SPRUCE_LOG", "CHARCOAL");
        put("BIRCH_LOG", "CHARCOAL");
        put("JUNGLE_LOG", "CHARCOAL");
        put("ACACIA_LOG", "CHARCOAL");
        put("DARK_OAK_LOG", "CHARCOAL");
        put("MANGROVE_LOG", "CHARCOAL");
        put("CHERRY_LOG", "CHARCOAL");
        put("OAK_WOOD", "CHARCOAL");
        put("SPRUCE_WOOD", "CHARCOAL");
        put("BIRCH_WOOD", "CHARCOAL");
        put("JUNGLE_WOOD", "CHARCOAL");
        put("ACACIA_WOOD", "CHARCOAL");
        put("DARK_OAK_WOOD", "CHARCOAL");
        put("MANGROVE_WOOD", "CHARCOAL");
        put("CHERRY_WOOD", "CHARCOAL");
        put("SMOOTH_BASALT", "BASALT");
        put("BASALT", "SMOOTH_BASALT");
    }

    private static void put(String source, String result) {
        Material s = Material.matchMaterial(source);
        Material r = Material.matchMaterial(result);
        if (s != null && r != null) {
            DEFAULT_RECIPE.put(s, r);
        }
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

    public static Material getSmelted(Material source) {
        if (!enabled || BLACKLIST.contains(source)) return null;
        Material custom = CUSTOM_RECIPE.get(source);
        if (custom != null) return custom;
        return DEFAULT_RECIPE.get(source);
    }
}
