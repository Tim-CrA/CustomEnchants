package dev.timca.customenchants.listener;

import dev.timca.customenchants.CustomEnchantsPlugin;
import dev.timca.customenchants.enchant.CustomEnchant;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseCooldown;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
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
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HarpoonListener implements Listener {

    // The Harpoon cooldown lives in its own cooldown group: the client draws the
    // overlay only on items with this cooldown_group (see ensureHarpoonCooldownGroup),
    // so regular tridents stay unblocked and throwable.
    public static final Key COOLDOWN_GROUP = Key.key("customenchants", "harpoon");

    private boolean enabled;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Location> tridentOrigins = new HashMap<>();
    private final Set<UUID> pullingPlayers = new HashSet<>();
    private Map<Integer, Integer> cooldownSeconds;
    private double maxDistance = 80;
    private static final double PULL_SPEED = 0.5;
    private static final long MAX_PULL_TICKS = 80;

    public HarpoonListener() {
        reload();
    }

    public void reload() {
        FileConfiguration config = CustomEnchantsPlugin.getInstance().getConfig();
        this.enabled = config.getBoolean("harpoon.enabled", true);
        this.maxDistance = config.getDouble("harpoon.max-distance", 80);
        this.cooldownSeconds = new HashMap<>();
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

    /**
     * Sets the use_cooldown component with a dedicated cooldown group on the trident
     * so the vanilla cooldown overlay only shows on Harpoon tridents.
     * The codec requires seconds > 0, so we use a minimal positive value:
     * ticks() == 0 and auto-applying the component on throw has no effect.
     */
    public static void ensureHarpoonCooldownGroup(ItemStack item) {
        if (item == null) return;
        UseCooldown existing = item.getData(DataComponentTypes.USE_COOLDOWN);
        if (existing != null && COOLDOWN_GROUP.equals(existing.cooldownGroup()) && existing.seconds() > 0f) {
            return;
        }
        item.setData(DataComponentTypes.USE_COOLDOWN,
                UseCooldown.useCooldown(0.0001f).cooldownGroup(COOLDOWN_GROUP));
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
        ensureHarpoonCooldownGroup(item);

        UUID pid = player.getUniqueId();
        long now = System.currentTimeMillis();
        int cd = cooldownSeconds.getOrDefault(level, 10);
        if (now - cooldowns.getOrDefault(pid, 0L) < cd * 1000L) {
            event.setCancelled(true);
            return;
        }
        tridentOrigins.put(trident.getUniqueId(), player.getLocation());
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
        ensureHarpoonCooldownGroup(item);

        UUID pid = player.getUniqueId();
        long now = System.currentTimeMillis();
        int cd = cooldownSeconds.getOrDefault(level, 10);
        if (now - cooldowns.getOrDefault(pid, 0L) < cd * 1000L) {
            return;
        }

        Location origin = tridentOrigins.remove(trident.getUniqueId());
        Entity hitEntity = event.getHitEntity();
        Block hitBlock = event.getHitBlock();
        Location hitLoc = null;
        if (hitEntity != null) hitLoc = hitEntity.getLocation();
        else if (hitBlock != null) hitLoc = hitBlock.getLocation();
        else return;

        if (origin != null && origin.distance(hitLoc) > maxDistance) {
            player.getInventory().addItem(item);
            trident.remove();
            player.sendMessage("\u00a7cToo far! Max " + (int) maxDistance + " blocks");
            return;
        }

        cooldowns.put(pid, now);
        player.setCooldown(COOLDOWN_GROUP, cd * 20);

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
        if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL
                && event.getEntity() instanceof Player player
                && pullingPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    private void pullEntityToPlayer(Player player, Entity entity) {
        UUID puid = player.getUniqueId();
        pullingPlayers.add(puid);
        Location origin = player.getLocation();
        BukkitTask[] holder = new BukkitTask[1];
        long[] ticks = {0};
        holder[0] = CustomEnchantsPlugin.getInstance().getServer().getScheduler().runTaskTimer(CustomEnchantsPlugin.getInstance(), () -> {
            if (!entity.isValid() || entity.getLocation().distance(origin) < 1.5 || ++ticks[0] > MAX_PULL_TICKS) {
                pullingPlayers.remove(puid);
                if (holder[0] != null) holder[0].cancel();
                return;
            }
            Vector dir = origin.toVector().subtract(entity.getLocation().toVector());
            double dist = dir.length();
            if (dist < 1.5) {
                pullingPlayers.remove(puid);
                if (holder[0] != null) holder[0].cancel();
                return;
            }
            entity.setVelocity(dir.normalize().multiply(Math.min(dist * PULL_SPEED, 2.0)));
        }, 0L, 1L);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
    }

    private void pullPlayerToLocation(Player player, Location target) {
        UUID puid = player.getUniqueId();
        pullingPlayers.add(puid);
        player.getWorld().playSound(target, Sound.ITEM_TRIDENT_RIPTIDE_3, 1, 1);
        BukkitTask[] holder = new BukkitTask[1];
        long[] ticks = {0};
        holder[0] = CustomEnchantsPlugin.getInstance().getServer().getScheduler().runTaskTimer(CustomEnchantsPlugin.getInstance(), () -> {
            if (!player.isOnline() || player.getLocation().distance(target) < 1.5 || ++ticks[0] > MAX_PULL_TICKS) {
                pullingPlayers.remove(puid);
                if (holder[0] != null) holder[0].cancel();
                return;
            }
            Vector dir = target.toVector().subtract(player.getLocation().toVector());
            double dist = dir.length();
            if (dist < 1.5) {
                pullingPlayers.remove(puid);
                if (holder[0] != null) holder[0].cancel();
                return;
            }
            player.setVelocity(dir.normalize().multiply(Math.min(dist * PULL_SPEED, 2.0)));
        }, 0L, 1L);
    }
}
