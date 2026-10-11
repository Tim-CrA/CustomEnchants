package dev.timca.customenchants.listener;

import dev.timca.customenchants.CustomEnchantsPlugin;
import dev.timca.customenchants.enchant.CustomEnchant;
import dev.timca.customenchants.util.Compat;
import dev.timca.customenchants.util.Schedulers;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseCooldown;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

public class HarpoonListener implements Listener {

    // The Harpoon cooldown lives in its own cooldown group: the client draws the
    // overlay only on items with this cooldown_group (see ensureHarpoonCooldownGroup),
    // so regular tridents stay unblocked and throwable. The Paper-only driver lives
    // in HarpoonFog (lazily class-loaded; never touched on Spigot).
    private static final boolean FOG = Compat.harpoonFogSupported();

    private record Origin(Location loc, long created) {}

    private boolean enabled;
    // ConcurrentHashMap: touched from global + region scheduler threads on Folia
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Origin> tridentOrigins = new ConcurrentHashMap<>();
    private final Set<UUID> pullingPlayers = ConcurrentHashMap.newKeySet();
    private Map<Integer, Integer> cooldownSeconds;
    private double maxDistance = 80;
    private static final double PULL_SPEED = 0.5;
    private static final long MAX_PULL_TICKS = 80;
    private static final long ORIGIN_TTL_MS = 300_000;
    // Keep fall protection for 5s after the pull ends so the landing impact is covered
    private static final long FALL_GRACE_TICKS = 100;

    public HarpoonListener() {
        reload();
        // Safety net: stamp EVERY harpoon trident in every inventory each second, so
        // the cooldown fog is uniform no matter how the trident was obtained
        // (enchanting table, anvil, loot, pickup, another plugin's command...).
        // Global tick re-dispatches per player, so it is Folia-safe.
        Schedulers.runGlobalTimer(CustomEnchantsPlugin.getInstance(),
                this::stampAllHeldTridents, 40L, 20L);
    }

    public void reload() {
        FileConfiguration config = CustomEnchantsPlugin.getInstance().getConfig();
        this.enabled = config.getBoolean("harpoon.enabled", true);
        this.maxDistance = config.getDouble("harpoon.max-distance", 80);
        this.cooldownSeconds = new ConcurrentHashMap<>();
        if (config.isConfigurationSection("harpoon.cooldown-seconds")) {
            for (String key : config.getConfigurationSection("harpoon.cooldown-seconds").getKeys(false)) {
                try {
                    cooldownSeconds.put(Integer.parseInt(key), config.getInt("harpoon.cooldown-seconds." + key));
                } catch (NumberFormatException ignored) {}
            }
        }
        if (!cooldownSeconds.containsKey(1)) cooldownSeconds.put(1, 180);
        if (!cooldownSeconds.containsKey(2)) cooldownSeconds.put(2, 120);
        if (!cooldownSeconds.containsKey(3)) cooldownSeconds.put(3, 60);
    }

    private void stampAllHeldTridents() {
        if (!enabled || !FOG) return;
        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            Schedulers.runAtEntityLater(CustomEnchantsPlugin.getInstance(), p, () -> stampPlayer(p), 0);
        }
    }

    private void stampPlayer(Player player) {
        if (!enabled || !FOG) return;
        for (ItemStack it : player.getInventory().getContents()) {
            if (it == null || it.getType() != Material.TRIDENT) continue;
            if (CustomEnchant.getLevel(it, CustomEnchant.HARPOON) < 1) continue;
            ensureHarpoonCooldownGroup(it);
        }
    }

    /**
     * Sets the use_cooldown component with a dedicated cooldown group on the trident
     * so the vanilla cooldown overlay only shows on Harpoon tridents (Paper only).
     * The codec requires seconds > 0, so we use a minimal positive value:
     * ticks() == 0 and auto-applying the component on throw has no effect.
     */
    public static void ensureHarpoonCooldownGroup(ItemStack item) {
        if (!FOG) return;
        HarpoonFog.ensure(item);
    }

    /**
     * The component must be on the inventory item BEFORE the throw (at launch the
     * item is already consumed), so we stamp it on every right click with a
     * Harpoon trident in hand — this also covers self-enchanted tridents.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!enabled || !FOG) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
        if (hand.getType() != Material.TRIDENT) return;
        if (CustomEnchant.getLevel(hand, CustomEnchant.HARPOON) < 1) return;
        HarpoonFog.ensure(hand);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        cooldowns.remove(uuid);
        pullingPlayers.remove(uuid);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!enabled) return;
        if (!(event.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player player)) return;
        ItemStack item = trident.getItem();
        if (item == null) return;
        int level = CustomEnchant.getLevel(item, CustomEnchant.HARPOON);
        if (level < 1) return;
        // Keep the cooldown group on the flying entity's item so the trident
        // still has it when picked back up
        ensureHarpoonCooldownGroup(item);
        trident.setItem(item);

        UUID pid = player.getUniqueId();
        long now = System.currentTimeMillis();
        tridentOrigins.values().removeIf(o -> now - o.created() > ORIGIN_TTL_MS);
        int cd = cooldownSeconds.getOrDefault(level, 10);
        if (now - cooldowns.getOrDefault(pid, 0L) < cd * 1000L) {
            event.setCancelled(true);
            // Show the remaining cooldown on the trident right away — otherwise the
            // fog only appears after a landed hit and a blocked throw feels silent.
            if (FOG) {
                long remaining = (cooldowns.get(pid) + cd * 1000L - now) / 50L;
                HarpoonFog.apply(player, (int) Math.max(1, remaining));
            }
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            return;
        }
        tridentOrigins.put(trident.getUniqueId(), new Origin(player.getLocation(), now));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!enabled) return;
        if (!(event.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player player)) return;
        ItemStack item = trident.getItem();
        if (item == null) return;
        int level = CustomEnchant.getLevel(item, CustomEnchant.HARPOON);
        if (level < 1) return;

        UUID pid = player.getUniqueId();
        long now = System.currentTimeMillis();
        // Consume the origin entry even when the hit lands during cooldown
        Origin origin = tridentOrigins.remove(trident.getUniqueId());
        int cd = cooldownSeconds.getOrDefault(level, 10);
        if (now - cooldowns.getOrDefault(pid, 0L) < cd * 1000L) {
            return;
        }

        Entity hitEntity = event.getHitEntity();
        Block hitBlock = event.getHitBlock();
        Location hitLoc = null;
        if (hitEntity != null) hitLoc = hitEntity.getLocation();
        else if (hitBlock != null) hitLoc = hitBlock.getLocation();
        else return;

        if (origin != null && origin.loc().distance(hitLoc) > maxDistance) {
            player.getInventory().addItem(item);
            trident.remove();
            player.sendMessage("\u00a7cToo far! Max " + (int) maxDistance + " blocks");
            return;
        }

        cooldowns.put(pid, now);
        if (FOG) HarpoonFog.apply(player, cd * 20);

        if (hitEntity != null && !hitEntity.equals(player)) {
            pullEntityToPlayer(player, hitEntity);
        } else if (hitBlock != null) {
            BlockFace face = event.getHitBlockFace();
            Location target = hitLoc.clone().add(0.5, 0.5, 0.5);
            if (face != null) target.add(face.getDirection().multiply(0.5));
            pullPlayerToLocation(player, target);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player
                && pullingPlayers.contains(player.getUniqueId())) {
            org.bukkit.event.entity.EntityDamageEvent.DamageCause cause = event.getCause();
            // Pulled players smash into ground (FALL) or walls (FLY_INTO_WALL)
            if (cause == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL
                    || cause == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FLY_INTO_WALL) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Ends fall protection after a grace period: the landing impact registers a few
     * ticks AFTER the pull task finishes, so removing the flag immediately let the
     * fall damage through.
     */
    private void releaseFallProtection(Player player) {
        Schedulers.runAtEntityLater(CustomEnchantsPlugin.getInstance(), player,
                () -> pullingPlayers.remove(player.getUniqueId()), FALL_GRACE_TICKS);
    }

    private void pullEntityToPlayer(Player player, Entity entity) {
        // Only the PULLED player gets fall-damage protection — adding the shooter
        // here would grant them a few seconds of fall-damage immunity per throw.
        final Player[] pulledPlayer = {null};
        if (entity instanceof Player pulled) {
            pulledPlayer[0] = pulled;
            pullingPlayers.add(pulled.getUniqueId());
        }
        Location origin = player.getLocation();
        // The task returns false to stop itself
        BooleanSupplier pullTask = new BooleanSupplier() {
            private long ticks = 0;

            @Override
            public boolean getAsBoolean() {
                if (!entity.isValid() || entity.getLocation().distance(origin) < 1.5 || ++ticks > MAX_PULL_TICKS) {
                    if (pulledPlayer[0] != null) releaseFallProtection(pulledPlayer[0]);
                    return false;
                }
                Vector dir = origin.toVector().subtract(entity.getLocation().toVector());
                double dist = dir.length();
                if (dist < 1.5) {
                    if (pulledPlayer[0] != null) releaseFallProtection(pulledPlayer[0]);
                    return false;
                }
                entity.setVelocity(dir.normalize().multiply(Math.min(dist * PULL_SPEED, 2.0)));
                return true;
            }
        };
        if (Compat.cooldownKeyApi()) {
            entity.getScheduler().runAtFixedRate(CustomEnchantsPlugin.getInstance(),
                    t -> { if (!pullTask.getAsBoolean()) t.cancel(); }, null, 0L, 1L);
        } else {
            final org.bukkit.scheduler.BukkitTask[] holder = new org.bukkit.scheduler.BukkitTask[1];
            holder[0] = CustomEnchantsPlugin.getInstance().getServer().getScheduler()
                    .runTaskTimer(CustomEnchantsPlugin.getInstance(), () -> {
                        if (!pullTask.getAsBoolean() && holder[0] != null) holder[0].cancel();
                    }, 0L, 1L);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
    }

    private void pullPlayerToLocation(Player player, Location target) {
        UUID puid = player.getUniqueId();
        pullingPlayers.add(puid);
        player.getWorld().playSound(target, Sound.ITEM_TRIDENT_RIPTIDE_3, 1, 1);
        BooleanSupplier pullTask = new BooleanSupplier() {
            private long ticks = 0;

            @Override
            public boolean getAsBoolean() {
                if (!player.isOnline() || player.getLocation().distance(target) < 1.5 || ++ticks > MAX_PULL_TICKS) {
                    releaseFallProtection(player);
                    return false;
                }
                Vector dir = target.toVector().subtract(player.getLocation().toVector());
                double dist = dir.length();
                if (dist < 1.5) {
                    releaseFallProtection(player);
                    return false;
                }
                player.setVelocity(dir.normalize().multiply(Math.min(dist * PULL_SPEED, 2.0)));
                return true;
            }
        };
        if (Compat.cooldownKeyApi()) {
            player.getScheduler().runAtFixedRate(CustomEnchantsPlugin.getInstance(),
                    t -> { if (!pullTask.getAsBoolean()) t.cancel(); }, null, 0L, 1L);
        } else {
            final org.bukkit.scheduler.BukkitTask[] holder = new org.bukkit.scheduler.BukkitTask[1];
            holder[0] = CustomEnchantsPlugin.getInstance().getServer().getScheduler()
                    .runTaskTimer(CustomEnchantsPlugin.getInstance(), () -> {
                        if (!pullTask.getAsBoolean() && holder[0] != null) holder[0].cancel();
                    }, 0L, 1L);
        }
    }
}
