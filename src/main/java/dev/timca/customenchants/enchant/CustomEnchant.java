package dev.timca.customenchants.enchant;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

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

    /** Persistent-data key used by the legacy (no registry) storage mode. */
    public NamespacedKey pdcKey() {
        return NamespacedKey.fromString("customenchants:" + key);
    }

    public static boolean hasEnchant(ItemStack item, CustomEnchant enchant) {
        return getLevel(item, enchant) > 0;
    }

    /**
     * Registry enchantment first; if the core has no custom-enchant registry
     * (pre-1.20.5, or the datapack was not loaded yet) fall back to the level
     * stored in the item's PersistentDataContainer.
     */
    public static int getLevel(ItemStack item, CustomEnchant enchant) {
        Enchantment e = enchant.getEnchantment();
        if (e != null) {
            int level = item.getEnchantmentLevel(e);
            if (level > 0) return level;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        return meta.getPersistentDataContainer()
                .getOrDefault(enchant.pdcKey(), PersistentDataType.INTEGER, 0);
    }

    public static CustomEnchant byKey(String key) {
        for (CustomEnchant e : values()) {
            if (e.key.equals(key)) return e;
        }
        return null;
    }

    /**
     * Applies the enchantment to an item: native registry enchantment when
     * available, otherwise PersistentDataContainer + lore line.
     */
    public static void applyTo(ItemStack item, CustomEnchant enchant, int level) {
        Enchantment e = enchant.getEnchantment();
        if (e != null) {
            item.addUnsafeEnchantment(e, level);
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(enchant.pdcKey(), PersistentDataType.INTEGER, level);
        String prefix = "\u00a75" + displayNameOf(enchant) + " ";
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : new ArrayList<>(meta.getLore());
        lore.removeIf(line -> line.startsWith(prefix));
        lore.add(prefix + roman(level));
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static ItemStack createBook(CustomEnchant enchant, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta == null) return book;
        Enchantment e = enchant.getEnchantment();
        if (e != null && meta instanceof EnchantmentStorageMeta storageMeta) {
            storageMeta.addStoredEnchant(e, level, true);
            book.setItemMeta(storageMeta);
            return book;
        }
        // Legacy fallback: pseudo book with the level in PDC and a lore hint
        meta.getPersistentDataContainer().set(enchant.pdcKey(), PersistentDataType.INTEGER, level);
        List<String> lore = new ArrayList<>();
        lore.add("\u00a75" + displayNameOf(enchant) + " " + roman(level));
        lore.add("\u00a78Apply with: /cenchant apply " + enchant.getKey() + " " + level);
        meta.setLore(lore);
        book.setItemMeta(meta);
        return book;
    }

    private static String displayNameOf(CustomEnchant enchant) {
        return enchant.displayName;
    }

    private static String roman(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(level);
        };
    }
}
