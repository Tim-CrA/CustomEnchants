package dev.timca.customenchants;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.List;

public class CustomEnchantsBootstrapper implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        var manager = context.getLifecycleManager();

        var drillKey = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("customenchants:drill"));
        var magnetKey = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("customenchants:magnet"));
        var smelterKey = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("customenchants:smelter"));
        var farmerKey = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("customenchants:farmer"));
        var harpoonKey = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("customenchants:harpoon"));
        var veinminerKey = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("customenchants:veinminer"));
        var lumberjackKey = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("customenchants:lumberjack"));
        var excavatorKey = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("customenchants:excavator"));
        var insightKey = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("customenchants:insight"));

        manager.registerEventHandler(
                RegistryEvents.ENCHANTMENT.compose().newHandler(event -> {
                    var registry = event.registry();

                    registry.register(drillKey, builder -> {
                        builder.description(Component.text("Drill"));
                        builder.supportedItems(event.getOrCreateTag(ItemTypeTagKeys.PICKAXES));
                        builder.weight(1);
                        builder.maxLevel(3);
                        builder.minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(20, 10));
                        builder.maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(60, 20));
                        builder.anvilCost(1);
                        builder.activeSlots(EquipmentSlotGroup.MAINHAND);
                    });

                    registry.register(magnetKey, builder -> {
                        builder.description(Component.text("Magnet"));
                        builder.supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_MINING));
                        builder.weight(1);
                        builder.maxLevel(1);
                        builder.minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 0));
                        builder.maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(65, 0));
                        builder.anvilCost(1);
                        builder.activeSlots(EquipmentSlotGroup.MAINHAND);
                    });

                    var silkTouchKey = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("minecraft:silk_touch"));

                    registry.register(smelterKey, builder -> {
                        builder.description(Component.text("Smelter"));
                        builder.supportedItems(event.getOrCreateTag(ItemTypeTagKeys.PICKAXES));
                        builder.weight(1);
                        builder.maxLevel(1);
                        builder.minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 0));
                        builder.maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(65, 0));
                        builder.anvilCost(1);
                        builder.activeSlots(EquipmentSlotGroup.MAINHAND);
                        builder.exclusiveWith(RegistrySet.keySet(RegistryKey.ENCHANTMENT, silkTouchKey));
                    });

                    registry.register(farmerKey, builder -> {
                        builder.description(Component.text("Farmer"));
                        builder.supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HOES));
                        builder.weight(1);
                        builder.maxLevel(1);
                        builder.minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 0));
                        builder.maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(50, 0));
                        builder.anvilCost(1);
                        builder.activeSlots(EquipmentSlotGroup.MAINHAND);
                    });

                    var channelingKey = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("minecraft:channeling"));
                    var riptideKey = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("minecraft:riptide"));

                    registry.register(harpoonKey, builder -> {
                        builder.description(Component.text("Harpoon"));
                        builder.supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_TRIDENT));
                        builder.weight(1);
                        builder.maxLevel(3);
                        builder.minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10));
                        builder.maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(30, 20));
                        builder.anvilCost(1);
                        builder.activeSlots(EquipmentSlotGroup.MAINHAND);
                        builder.exclusiveWith(RegistrySet.keySet(RegistryKey.ENCHANTMENT, channelingKey, riptideKey));
                    });

                    registry.register(veinminerKey, builder -> {
                        builder.description(Component.text("Veinminer"));
                        builder.supportedItems(event.getOrCreateTag(ItemTypeTagKeys.PICKAXES));
                        builder.weight(1);
                        builder.maxLevel(1);
                        builder.minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 0));
                        builder.maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(65, 0));
                        builder.anvilCost(1);
                        builder.activeSlots(EquipmentSlotGroup.MAINHAND);
                    });

                    registry.register(lumberjackKey, builder -> {
                        builder.description(Component.text("Lumberjack"));
                        builder.supportedItems(event.getOrCreateTag(ItemTypeTagKeys.AXES));
                        builder.weight(1);
                        builder.maxLevel(1);
                        builder.minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 0));
                        builder.maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(65, 0));
                        builder.anvilCost(1);
                        builder.activeSlots(EquipmentSlotGroup.MAINHAND);
                    });

                    registry.register(excavatorKey, builder -> {
                        builder.description(Component.text("Excavator"));
                        builder.supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SHOVELS));
                        builder.weight(1);
                        builder.maxLevel(3);
                        builder.minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 10));
                        builder.maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(55, 20));
                        builder.anvilCost(1);
                        builder.activeSlots(EquipmentSlotGroup.MAINHAND);
                    });

                    registry.register(insightKey, builder -> {
                        builder.description(Component.text("Insight"));
                        builder.supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS));
                        builder.weight(1);
                        builder.maxLevel(2);
                        builder.minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10));
                        builder.maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(40, 20));
                        builder.anvilCost(1);
                        builder.activeSlots(EquipmentSlotGroup.MAINHAND);
                    });
                })
        );

        var allEnchants = List.of(drillKey, magnetKey, smelterKey, farmerKey, harpoonKey,
                veinminerKey, lumberjackKey, excavatorKey, insightKey);
        manager.registerEventHandler(
                LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT).newHandler(event -> {
                    if (!(event instanceof PostFlattenTagRegistrar<?> registrar)) return;
                    @SuppressWarnings("unchecked")
                    var enchRegistrar = (PostFlattenTagRegistrar<org.bukkit.enchantments.Enchantment>) registrar;
                    enchRegistrar.addToTag(EnchantmentTagKeys.NON_TREASURE, allEnchants);
                    enchRegistrar.addToTag(EnchantmentTagKeys.TRADEABLE, allEnchants);
                })
        );
    }
}
