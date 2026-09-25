package dev.timca.customenchants.util;

import java.lang.reflect.Method;

/**
 * Runtime capability detection so a single jar runs on Paper, Spigot and forks.
 * Paper-only classes must never be referenced outside guards based on these flags.
 */
public final class Compat {

    private static final boolean PAPER_COMPONENTS;
    private static final boolean COOLDOWN_KEY_API;

    static {
        boolean components = false;
        try {
            Class.forName("io.papermc.paper.datacomponent.DataComponentTypes");
            components = true;
        } catch (Throwable ignored) {
        }
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
        PAPER_COMPONENTS = components;
        COOLDOWN_KEY_API = cooldownKey;
    }

    private Compat() {
    }

    /** Paper data-component API is present (Paper 1.20.5+). */
    public static boolean paperComponents() {
        return PAPER_COMPONENTS;
    }

    /** Per-key player cooldown API is present (Paper). */
    public static boolean cooldownKeyApi() {
        return COOLDOWN_KEY_API;
    }

    /** The Harpoon vanilla cooldown overlay can be driven from this server. */
    public static boolean harpoonFogSupported() {
        return PAPER_COMPONENTS && COOLDOWN_KEY_API;
    }

    /** True if the class is present on this server (lazy — callers must not reference it otherwise). */
    public static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
