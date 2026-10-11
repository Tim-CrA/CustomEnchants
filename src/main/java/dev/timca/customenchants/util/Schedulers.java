package dev.timca.customenchants.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Scheduler wrapper that works on Paper, Spigot AND Folia. On modern cores
 * (1.20+) it uses the Folia-aware Entity/GlobalRegion schedulers (on Paper they
 * simply run on the main thread); on legacy cores it falls back to the classic
 * Bukkit scheduler. Tasks touching a player must go through the entity variants.
 */
public final class Schedulers {

    private static final boolean MODERN = isModern();

    private Schedulers() {
    }

    private static boolean isModern() {
        String version = Bukkit.getBukkitVersion();
        String[] parts = version.split("[.\\-]");
        try {
            if (Integer.parseInt(parts[0]) != 1) return true; // future major — modern
            return Integer.parseInt(parts[1]) >= 20;
        } catch (Exception e) {
            return true;
        }
    }

    /** Delayed task bound to an entity (player) — safe on Folia. */
    public static void runAtEntityLater(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        if (MODERN) {
            entity.getScheduler().execute(plugin, task, null, delayTicks);
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    /** Global repeating task; the task itself must re-dispatch per player on Folia. */
    public static void runGlobalTimer(Plugin plugin, Runnable task, long initialDelayTicks, long periodTicks) {
        if (MODERN) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin,
                    t -> task.run(), initialDelayTicks, periodTicks);
            return;
        }
        Bukkit.getScheduler().runTaskTimer(plugin, task, initialDelayTicks, periodTicks);
    }
}
