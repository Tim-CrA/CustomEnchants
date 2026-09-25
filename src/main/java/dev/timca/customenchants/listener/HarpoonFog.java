package dev.timca.customenchants.listener;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseCooldown;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Paper-only driver of the Harpoon vanilla cooldown overlay.
 * This class must only be referenced after Compat#harpoonFogSupported() —
 * on Spigot it is never class-loaded, so the missing Paper classes are harmless.
 */
public final class HarpoonFog {

    public static final Key COOLDOWN_GROUP = Key.key("customenchants", "harpoon");

    private HarpoonFog() {
    }

    public static void ensure(ItemStack item) {
        if (item == null) return;
        UseCooldown existing = item.getData(DataComponentTypes.USE_COOLDOWN);
        if (existing != null && COOLDOWN_GROUP.equals(existing.cooldownGroup()) && existing.seconds() > 0f) {
            return;
        }
        item.setData(DataComponentTypes.USE_COOLDOWN,
                UseCooldown.useCooldown(0.0001f).cooldownGroup(COOLDOWN_GROUP));
    }

    public static void apply(Player player, int ticks) {
        player.setCooldown(COOLDOWN_GROUP, ticks);
    }
}
