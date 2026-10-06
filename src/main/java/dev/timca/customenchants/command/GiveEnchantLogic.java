package dev.timca.customenchants.command;

import dev.timca.customenchants.CustomEnchantsPlugin;
import dev.timca.customenchants.enchant.CustomEnchant;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Core /cenchant logic shared by the Paper (Brigadier) and Spigot (CommandExecutor)
 * adapters. Must only use cross-platform Bukkit API.
 */
public final class GiveEnchantLogic {

    private GiveEnchantLogic() {
    }

    public static void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sendUsage(sender);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "give" -> handleGive(sender, args);
            case "apply" -> handleApply(sender, args);
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            default -> sendUsage(sender);
        }
    }

    public static List<String> suggest(String[] args) {
        if (args.length == 1) {
            return List.of("give", "apply", "list", "reload");
        } else if (args.length == 2 && ("give".equalsIgnoreCase(args[0]) || "apply".equalsIgnoreCase(args[0]))) {
            List<String> keys = new ArrayList<>();
            for (CustomEnchant e : CustomEnchant.values()) keys.add(e.getKey());
            return keys;
        }
        return List.of();
    }

    /** Applies an enchantment straight to the item in the player's main hand. */
    private static void handleApply(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this!");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("\u00a7cUsage: /cenchant apply <key> [level]");
            return;
        }
        CustomEnchant ench = CustomEnchant.byKey(args[1].toLowerCase());
        if (ench == null) {
            player.sendMessage("\u00a7cUnknown enchantment. Use: " + keyList());
            return;
        }
        int level = 1;
        if (args.length >= 3) {
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("\u00a7cInvalid level.");
                return;
            }
        }
        level = Math.max(1, Math.min(level, ench.getMaxLevel()));
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("\u00a7cHold an item in your main hand first.");
            return;
        }
        CustomEnchant.applyTo(item, ench, level);
        player.sendMessage("\u00a7aApplied " + ench.getDisplayName() + " " + level + " to the item in your hand");
    }

    private static void handleGive(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this!");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("\u00a7cUsage: /cenchant give <key> [level]");
            return;
        }
        CustomEnchant ench = CustomEnchant.byKey(args[1].toLowerCase());
        if (ench == null) {
            player.sendMessage("\u00a7cUnknown enchantment. Use: " + keyList());
            return;
        }
        int level = 1;
        if (args.length >= 3) {
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("\u00a7cInvalid level.");
                return;
            }
        }
        level = Math.max(1, Math.min(level, ench.getMaxLevel()));
        ItemStack book = CustomEnchant.createBook(ench, level);
        player.getInventory().addItem(book).values().forEach(extra ->
                player.getWorld().dropItemNaturally(player.getLocation(), extra));
        player.sendMessage("\u00a7aGiven " + ench.getDisplayName() + " " + level + " book");
    }

    private static void handleList(CommandSender sender) {
        sender.sendMessage("\u00a76Custom Enchants:");
        for (CustomEnchant e : CustomEnchant.values()) {
            sender.sendMessage("\u00a77- \u00a7f" + e.getKey() + "\u00a77 (" + e.getDisplayName() + ", max " + e.getMaxLevel() + ")");
        }
    }

    private static void handleReload(CommandSender sender) {
        CustomEnchantsPlugin plugin = CustomEnchantsPlugin.getInstance();
        plugin.reloadConfig();
        plugin.getDrillListener().reload();
        plugin.getMagnetListener().reload();
        plugin.getFarmerListener().reload();
        plugin.getHarpoonListener().reload();
        plugin.getLootListener().reload();
        plugin.getVeinMinerListener().reload();
        plugin.getLumberjackListener().reload();
        plugin.getExcavatorListener().reload();
        plugin.getInsightListener().reload();
        dev.timca.customenchants.util.SmeltUtils.loadCustomRecipes(plugin.getConfig());
        sender.sendMessage("\u00a7aConfig reloaded");
    }

    private static String keyList() {
        StringBuilder sb = new StringBuilder();
        for (CustomEnchant e : CustomEnchant.values()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(e.getKey());
        }
        return sb.toString();
    }

    private static void sendUsage(CommandSender sender) {
        sender.sendMessage("\u00a7cUsage: /cenchant <give|apply|list|reload>");
    }
}
