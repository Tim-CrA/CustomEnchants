package dev.timca.customenchants.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

/**
 * Spigot (CommandExecutor) adapter for /cenchant.
 * Cross-platform behavior lives in {@link GiveEnchantLogic}.
 */
public final class GiveEnchantCommandLegacy implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GiveEnchantLogic.execute(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return GiveEnchantLogic.suggest(args);
    }
}
