package dev.timca.customenchants;

import dev.timca.customenchants.command.GiveEnchantCommandLegacy;
import dev.timca.customenchants.command.PaperCommands;
import dev.timca.customenchants.enchant.CustomEnchant;
import dev.timca.customenchants.listener.*;
import dev.timca.customenchants.util.Compat;
import dev.timca.customenchants.util.DatapackInstaller;
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomEnchantsPlugin extends JavaPlugin {

    private static CustomEnchantsPlugin instance;

    private DrillListener drillListener;
    private MagnetListener magnetListener;
    private FarmerListener farmerListener;
    private HarpoonListener harpoonListener;
    private LootListener lootListener;
    private VeinMinerListener veinMinerListener;
    private LumberjackListener lumberjackListener;
    private ExcavatorListener excavatorListener;
    private InsightListener insightListener;
    private boolean allEnchantsRegistered;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        getLogger().info("");
        getLogger().info("  CustomEnchants v" + getDescription().getVersion() + "  ");
        getLogger().info("  by TimCA  ");
        getLogger().info("");

        allEnchantsRegistered = true;
        for (CustomEnchant ce : CustomEnchant.values()) {
            Enchantment e = ce.getEnchantment();
            if (e == null) {
                allEnchantsRegistered = false;
                getLogger().warning(ce.getKey() + " is not registered yet");
            } else {
                getLogger().info(ce.getKey() + " registered: " + e.getKey());
            }
        }
        if (!allEnchantsRegistered) {
            // Paper registers via the bootstrapper; everyone else needs the bundled
            // datapack, and registry data only loads at server startup.
            boolean installed = DatapackInstaller.install(this);
            if (installed) {
                getLogger().severe("Enchantment datapack was just installed into the world. "
                        + "RESTART the server once — the enchantments activate after the restart.");
            } else {
                getLogger().severe("Enchantments are not registered. If this persists, check that "
                        + "the world datapack 'customenchants' is enabled.");
            }
        }

        drillListener = new DrillListener();
        magnetListener = new MagnetListener();
        farmerListener = new FarmerListener();
        harpoonListener = new HarpoonListener();
        lootListener = new LootListener();
        veinMinerListener = new VeinMinerListener();
        lumberjackListener = new LumberjackListener();
        excavatorListener = new ExcavatorListener();
        insightListener = new InsightListener();

        getServer().getPluginManager().registerEvents(drillListener, this);
        // BlockDropItemEvent exists since 1.17 — on older cores this listener must
        // never be class-loaded.
        if (Compat.classExists("org.bukkit.event.block.BlockDropItemEvent")) {
            getServer().getPluginManager().registerEvents(magnetListener, this);
        }
        getServer().getPluginManager().registerEvents(farmerListener, this);
        getServer().getPluginManager().registerEvents(harpoonListener, this);
        getServer().getPluginManager().registerEvents(lootListener, this);
        getServer().getPluginManager().registerEvents(veinMinerListener, this);
        getServer().getPluginManager().registerEvents(lumberjackListener, this);
        getServer().getPluginManager().registerEvents(excavatorListener, this);
        getServer().getPluginManager().registerEvents(insightListener, this);

        registerCommand("cenchant");

        dev.timca.customenchants.util.SmeltUtils.loadCustomRecipes(getConfig());

        getLogger().info("CustomEnchants v" + getDescription().getVersion() + " enabled");
    }

    private void registerCommand(String name) {
        if (Compat.classExists("io.papermc.paper.command.brigadier.BasicCommand")) {
            PaperCommands.register(this, name);
            return;
        }
        PluginCommand cmd = getCommand(name);
        if (cmd == null) {
            getLogger().severe("Command '" + name + "' missing from plugin.yml");
            return;
        }
        GiveEnchantCommandLegacy legacy = new GiveEnchantCommandLegacy();
        cmd.setExecutor(legacy);
        cmd.setTabCompleter(legacy);
    }

    public boolean isAllEnchantsRegistered() {
        return allEnchantsRegistered;
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomEnchants v" + getDescription().getVersion() + " disabled");
    }

    public static CustomEnchantsPlugin getInstance() {
        return instance;
    }

    public DrillListener getDrillListener() { return drillListener; }
    public MagnetListener getMagnetListener() { return magnetListener; }
    public FarmerListener getFarmerListener() { return farmerListener; }
    public HarpoonListener getHarpoonListener() { return harpoonListener; }
    public LootListener getLootListener() { return lootListener; }
    public VeinMinerListener getVeinMinerListener() { return veinMinerListener; }
    public LumberjackListener getLumberjackListener() { return lumberjackListener; }
    public ExcavatorListener getExcavatorListener() { return excavatorListener; }
    public InsightListener getInsightListener() { return insightListener; }
}