package dev.timca.customenchants.hook;

import dev.timca.customenchants.enchant.CustomEnchant;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * PlaceholderAPI placeholders. Only class-loaded when PlaceholderAPI is
 * installed on the server.
 *
 * %customenchants_level_<key>% — level of an enchantment on the main-hand item
 * %customenchants_has_<key>%  — "true"/"false"
 * %customenchants_list%       — "drill:2, insight:1" (enchantments on the main-hand item)
 */
public class CustomEnchantsExpansion extends PlaceholderExpansion {

    private final Plugin plugin;

    public CustomEnchantsExpansion(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "customenchants";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TimCA";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (!(player instanceof Player online)) return null;
        String param = params.toLowerCase();
        ItemStack hand = online.getInventory().getItemInMainHand();
        if (hand == null) hand = new ItemStack(org.bukkit.Material.AIR);

        if (param.equals("list")) {
            List<String> parts = new ArrayList<>();
            for (CustomEnchant e : CustomEnchant.values()) {
                int level = CustomEnchant.getLevel(hand, e);
                if (level > 0) parts.add(e.getKey() + ":" + level);
            }
            return String.join(", ", parts);
        }
        if (param.startsWith("level_")) {
            CustomEnchant e = CustomEnchant.byKey(param.substring("level_".length()));
            return e == null ? null : String.valueOf(CustomEnchant.getLevel(hand, e));
        }
        if (param.startsWith("has_")) {
            CustomEnchant e = CustomEnchant.byKey(param.substring("has_".length()));
            return e == null ? null : String.valueOf(CustomEnchant.getLevel(hand, e) > 0);
        }
        return null;
    }
}
