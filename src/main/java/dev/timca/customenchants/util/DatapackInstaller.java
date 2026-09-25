package dev.timca.customenchants.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.lang.reflect.Method;

/**
 * Spigot/fork fallback: Paper registers the custom enchantments through the
 * bootstrapper, but other cores only see them as vanilla datapack registry data.
 * This extracts the bundled data/ files into <world>/datapacks/customenchants/
 * so they load on the next server start (registry data is read at startup).
 */
public final class DatapackInstaller {

    private DatapackInstaller() {
    }

    /**
     * @return true if anything was created or changed (a restart is then required
     * for the enchantment registry to pick it up)
     */
    public static boolean install(Plugin plugin) {
        File jarFile = pluginFile(plugin);
        if (jarFile == null) return false;

        // Registry datapacks live in <world-root>/datapacks — NOT in the per-dimension
        // folder that World#getWorldFolder() returns on newer versions (26.x).
        String worldName = Bukkit.getWorlds().isEmpty()
                ? "world"
                : Bukkit.getWorlds().get(0).getName();
        File worldRoot = new File(Bukkit.getWorldContainer(), worldName);
        File target = new File(worldRoot, "datapacks" + File.separator + "customenchants");
        boolean changed = false;

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory() || !name.startsWith("data/")) continue;
                File out = new File(target, name);
                if (out.isFile() && out.length() == entry.getSize()) continue;
                out.getParentFile().mkdirs();
                try (InputStream in = jar.getInputStream(entry);
                     FileOutputStream fos = new FileOutputStream(out)) {
                    in.transferTo(fos);
                }
                changed = true;
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not extract bundled datapack: " + e.getMessage());
            return false;
        }

        File mcmeta = new File(target, "pack.mcmeta");
        if (!mcmeta.isFile()) {
            int format = detectPackFormat();
            // supported_formats keeps the pack valid across many game versions
            String content = "{\"pack\":{\"pack_format\":" + format
                    + ",\"supported_formats\":{\"min_inclusive\":26,\"max_inclusive\":9999}"
                    + ",\"description\":\"CustomEnchants enchantments\"}}";
            try (Writer w = new OutputStreamWriter(new FileOutputStream(mcmeta), StandardCharsets.UTF_8)) {
                w.write(content);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not write pack.mcmeta: " + e.getMessage());
                return false;
            }
            changed = true;
        }

        if (changed) {
            plugin.getLogger().info("Installed enchantment datapack into " + target.getPath());
        }
        return changed;
    }

    private static int detectPackFormat() {
        try {
            Object unsafe = Bukkit.getUnsafe();
            for (Method m : unsafe.getClass().getMethods()) {
                if (m.getParameterCount() == 0
                        && (m.getReturnType() == int.class || m.getReturnType() == Integer.class)
                        && m.getName().toLowerCase().contains("datapack")) {
                    Object value = m.invoke(unsafe);
                    if (value instanceof Integer i && i > 0) return i;
                }
            }
        } catch (Throwable ignored) {
        }
        return 48; // 1.21 baseline; supported_formats widens the accepted range
    }

    private static File pluginFile(Plugin plugin) {
        try {
            Method m = org.bukkit.plugin.java.JavaPlugin.class.getDeclaredMethod("getFile");
            m.setAccessible(true);
            Object result = m.invoke(plugin);
            return result instanceof File f ? f : null;
        } catch (Throwable t) {
            return null;
        }
    }
}
