package dev.timca.customenchants.util;

import java.lang.reflect.Method;

/**
 * Runtime capability detection so a single jar runs on Paper, Spigot and forks.
 * Paper-only classes must never be referenced outside guards based on these flags.
 */
public final class Compat {

    private static final boolean COOLDOWN_KEY_API;

    static {
        boolean cooldownKey = false;
        try {
            for (Method m : org.bukkit.entity.HumanEntity.class.getMethods()) {
                Class<?>[] params = m.getParameterTypes();
                if (m.getName().equals("setCooldown") && params.length == 2
                        && params[0].getName().equals("net.kyori.adventure.key.Key")) {
                    cooldownKey = true;
                    break;
                }
            }
        } catch (Throwable ignored) {
        }
        COOLDOWN_KEY_API = cooldownKey;
    }

    private Compat() {
    }

    /** Per-key player cooldown API is present (Paper-family cores only). */
    public static boolean cooldownKeyApi() {
        return COOLDOWN_KEY_API;
    }

    /** The Harpoon vanilla cooldown overlay can be driven from this server. */
    public static boolean harpoonFogSupported() {
        return COOLDOWN_KEY_API;
    }

    /**
     * BlockDropItemEvent exists since Minecraft 1.17 — older cores never see it.
     * Parsed from Bukkit.getBukkitVersion() (e.g. "1.21.11-R0.1-SNAPSHOT").
     */
    public static boolean hasBlockDropItemEvent() {
        String version = org.bukkit.Bukkit.getBukkitVersion();
        String[] parts = version.split("[.\\-]");
        try {
            if (Integer.parseInt(parts[0]) != 1) return true; // future major — assume modern
            return Integer.parseInt(parts[1]) >= 17;
        } catch (Exception e) {
            return true;
        }
    }
}
