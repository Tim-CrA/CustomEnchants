package dev.timca.customenchants.enchant;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public enum CustomEnchant {

    DRILL("drill", "Drill", 3),
    MAGNET("magnet", "Magnet", 1),
    SMELTER("smelter", "Smelter", 1),
    FARMER("farmer", "Farmer", 1),
    HARPOON("harpoon", "Harpoon", 3),
    VEINMINER("veinminer", "Veinminer", 1),
    LUMBERJACK("lumberjack", "Lumberjack", 1),
    EXCAVATOR("excavator", "Excavator", 3),
    INSIGHT("insight", "Insight", 2);

    private final String key;
    private final String displayName;
    private final int maxLevel;

    CustomEnchant(String key, String displayName, int maxLevel) {
        this.key = key;
        this.displayName = displayName;
        this.maxLevel = maxLevel;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public Enchantment getEnchantment() {
        return Enchantment.getByKey(NamespacedKey.fromString("customenchants:" + key));
    }

    public static boolean hasEnchant(ItemStack item, CustomEnchant enchant) {
        Enchantment ench = enchant.getEnchantment();
        return ench != null && item.containsEnchantment(ench);
    }

    public static int getLevel(ItemStack item, CustomEnchant enchant) {
        Enchantment ench = enchant.getEnchantment();
        return ench != null ? item.getEnchantmentLevel(ench) : 0;
    }

    public static CustomEnchant byKey(String key) {
        for (CustomEnchant e : values()) {
            if (e.key.equals(key)) return e;
        }
        return null;
    }

    public static ItemStack createBook(CustomEnchant enchant, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        var meta = book.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            Enchantment e = enchant.getEnchantment();
            if (e != null) {
                storageMeta.addStoredEnchant(e, level, true);
                book.setItemMeta(storageMeta);
            }
        }
        return book;
    }
}
