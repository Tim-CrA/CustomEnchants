package dev.timca.customenchants.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Paper (Brigadier) adapter for /cenchant.
 * Only class-loaded on Paper — Spigot uses {@link GiveEnchantCommandLegacy}.
 * Cross-platform behavior lives in {@link GiveEnchantLogic}.
 */
@SuppressWarnings("UnstableApiUsage")
public final class GiveEnchantCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        GiveEnchantLogic.execute(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("customenchants.admin");
    }

    @Override
    public List<String> suggest(CommandSourceStack source, String[] args) {
        return GiveEnchantLogic.suggest(args);
    }
}
