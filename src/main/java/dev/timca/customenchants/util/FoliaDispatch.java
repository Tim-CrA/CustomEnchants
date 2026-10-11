package dev.timca.customenchants.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.function.BooleanSupplier;

/**
 * Paper-only scheduler dispatch. Referenced exclusively after
 * Compat#cooldownKeyApi() (the Key overload of setCooldown exists only on
 * Paper-family cores), so on Spigot this class never loads and its Paper
 * scheduler types are harmless.
 */
public final class FoliaDispatch {

    private FoliaDispatch() {
    }

    /** Global fixed-rate tick; the task re-dispatches per player itself if needed. */
    public static void runGlobalFixedRate(Plugin plugin, Runnable task, long initialDelayTicks, long periodTicks) {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin,
                t -> task.run(), initialDelayTicks, periodTicks);
    }

    /** Delayed task bound to an entity (player) — safe on Folia region threads. */
    public static void runAtEntityLater(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        entity.getScheduler().execute(plugin, task, null, delayTicks);
    }

    /**
     * Fixed-rate task bound to an entity; the task returns false to stop itself.
     * The returned Runnable cancels nothing — cancellation happens via the
     * scheduler task handle inside this method.
     */
    public static void runAtEntityFixedRate(Plugin plugin, Entity entity, BooleanSupplier task,
                                            long initialDelayTicks, long periodTicks) {
        entity.getScheduler().runAtFixedRate(plugin,
                t -> { if (!task.getAsBoolean()) t.cancel(); }, null, initialDelayTicks, periodTicks);
    }
}
