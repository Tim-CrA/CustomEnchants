package dev.timca.customenchants.command;

import dev.timca.customenchants.CustomEnchantsPlugin;
import dev.timca.customenchants.enchant.CustomEnchant;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class GiveEnchantCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (args.length < 1) {
            sendUsage(source.getSender());
            return;
        }
        switch (args[0].toLowerCase()) {
            case "give" -> handleGive(source.getSender(), args);
            case "list" -> handleList(source.getSender());
            case "reload" -> handleReload(source.getSender());
            default -> sendUsage(source.getSender());
        }
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("customenchants.admin");
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length == 1) {
            return List.of("give", "list", "reload");
        } else if (args.length == 2 && "give".equalsIgnoreCase(args[0])) {
            List<String> keys = new ArrayList<>();
            for (CustomEnchant e : CustomEnchant.values()) keys.add(e.getKey());
            return keys;
        }
        return List.of();
    }

    private void handleGive(CommandSender sender, String[] args) {
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
            try { level = Integer.parseInt(args[2]); } catch (NumberFormatException e) {
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

    private void handleList(CommandSender sender) {
        sender.sendMessage("\u00a76Custom Enchants:");
        for (CustomEnchant e : CustomEnchant.values()) {
            sender.sendMessage("\u00a77- \u00a7f" + e.getKey() + "\u00a77 (" + e.getDisplayName() + ", max " + e.getMaxLevel() + ")");
        }
    }

    private void handleReload(CommandSender sender) {
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

    private String keyList() {
        StringBuilder sb = new StringBuilder();
        for (CustomEnchant e : CustomEnchant.values()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(e.getKey());
        }
        return sb.toString();
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("\u00a7cUsage: /cenchant <give|list|reload>");
    }
}
