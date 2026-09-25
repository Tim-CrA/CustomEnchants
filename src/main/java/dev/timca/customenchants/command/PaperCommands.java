package dev.timca.customenchants.command;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Paper-only command registration. Referenced exclusively after
 * Compat#classExists("io.papermc.paper.command.brigadier.BasicCommand"),
 * so on Spigot this class (and its Paper API references) never loads.
 */
public final class PaperCommands {

    private PaperCommands() {
    }

    public static void register(JavaPlugin plugin, String name) {
        plugin.registerCommand(name, new GiveEnchantCommand());
    }
}
