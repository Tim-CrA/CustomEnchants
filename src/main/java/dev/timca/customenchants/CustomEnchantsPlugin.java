package dev.timca.customenchants;

import dev.timca.customenchants.command.GiveEnchantCommand;
import dev.timca.customenchants.enchant.CustomEnchant;
import dev.timca.customenchants.listener.*;
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

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        getLogger().info("");
        getLogger().info("  CustomEnchants v" + getDescription().getVersion() + "  ");
        getLogger().info("  by TimCA  ");
        getLogger().info("");

        for (CustomEnchant ce : CustomEnchant.values()) {
            Enchantment e = ce.getEnchantment();
            if (e == null) {
                getLogger().severe(ce.getKey() + " NOT registered! Check bootstrapper.");
            } else {
                getLogger().info(ce.getKey() + " registered: " + e.getKey());
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
        getServer().getPluginManager().registerEvents(magnetListener, this);
        getServer().getPluginManager().registerEvents(farmerListener, this);
        getServer().getPluginManager().registerEvents(harpoonListener, this);
        getServer().getPluginManager().registerEvents(lootListener, this);
        getServer().getPluginManager().registerEvents(veinMinerListener, this);
        getServer().getPluginManager().registerEvents(lumberjackListener, this);
        getServer().getPluginManager().registerEvents(excavatorListener, this);
        getServer().getPluginManager().registerEvents(insightListener, this);

        registerCommand("cenchant", new GiveEnchantCommand());

        dev.timca.customenchants.util.SmeltUtils.loadCustomRecipes(getConfig());

        getLogger().info("CustomEnchants v" + getDescription().getVersion() + " enabled");
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