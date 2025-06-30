package com.trynocs.bPEconomy;

import com.trynocs.bPEconomy.commands.Jobs;
import com.trynocs.bPEconomy.economy.VaultEconomy;
import com.trynocs.tryLibs.TryLibs;
import com.trynocs.tryLibs.utils.config.Configmanager;
import com.trynocs.tryLibs.utils.database.DatabaseHandler;
import com.trynocs.tryLibs.utils.economy.EconomyManager;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import co.aikar.commands.PaperCommandManager;

import java.util.Arrays;

public final class main extends JavaPlugin {

    // Statische Felder
    public static main plugin;
    public static LuckPerms luckPerms;

    public static String prefix;
    public static String beplayer;
    public static String noplayer;
    public static String discord;

    // Instanzfelder
    private DatabaseHandler databaseHandler;
    private Configmanager configManager;
    private PaperCommandManager commandManager;
    private PluginManager pluginManager;
    private EconomyManager economyManager;
    private VaultEconomy vaultEconomy;

    @Override
    public void onEnable() {
        plugin = this;

        if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        }

        databaseHandler = new DatabaseHandler();
        configManager = new Configmanager(this);
        configManager.saveDefaultConfig();
        pluginManager = getServer().getPluginManager();
        commandManager = new PaperCommandManager(this);

        // Setup Vault Economy
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            setupEconomy();
            getLogger().info("Vault Economy integration successful!");
        } else {
            getLogger().warning("Vault not found! Economy features may not work properly.");
        }

        getServer().getScheduler().runTask(this, () -> {
            economyManager = new EconomyManager(main.getPlugin().getDatabaseHandler());
        });

        loadConfigValues();
        register();

        getLogger().info("BPEconomy wurde aktiviert!");
    }

    private boolean setupEconomy() {
        vaultEconomy = new VaultEconomy();
        getServer().getServicesManager().register(Economy.class, vaultEconomy, this, ServicePriority.Highest);
        return true;
    }

    private void register() {
        Jobs jobs = new Jobs();

        // Currency Completion example
        commandManager.getCommandCompletions().registerAsyncCompletion("currencies", c -> {
            return Arrays.asList("coins", "gems");
        });

        commandManager.registerCommand(jobs);
        pluginManager.registerEvents(jobs, this);
        pluginManager.registerEvents(new com.trynocs.bPEconomy.listener.JobsListener(), this);
    }

    @Override
    public void onDisable() {
        if (databaseHandler != null) {
            databaseHandler.closeConnection();
        }

        // Unregister Vault services when disabling
        if (vaultEconomy != null) {
            getServer().getServicesManager().unregister(Economy.class, vaultEconomy);
        }

        getLogger().info("BPEconomy wurde deaktiviert!");
    }

    private void loadConfigValues() {
        String prefix2 = configManager.getConfig().getString("messages.prefix", "&b&lBlockEngine &8» &7");
        String beplayer2 = configManager.getConfig().getString("messages.not-player", "Du musst ein Spieler sein um diesen Command auszuführen.");
        String noplayer2 = configManager.getConfig().getString("messages.no-player", "Dieser Spieler ist offline oder existiert nicht.");
        String discord2 = configManager.getConfig().getString("messages.discord", "DEIN INVITE LINK");
        prefix = ChatColor.translateAlternateColorCodes('&', prefix2);
        beplayer = main.prefix + ChatColor.translateAlternateColorCodes('&', beplayer2);
        noplayer = main.prefix + ChatColor.translateAlternateColorCodes('&', noplayer2);
        discord = ChatColor.translateAlternateColorCodes('&', discord2);
    }

    public static main getPlugin() {
        return plugin;
    }

    public static LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public static String getPrefix() {
        return prefix;
    }

    public static String getBeplayer() {
        return beplayer;
    }

    public static String getNoplayer() {
        return noplayer;
    }

    public static String getDiscord() {
        return discord;
    }

    public DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }

    public Configmanager getConfigManager() {
        return configManager;
    }

    public PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public PluginManager getPluginManagerInstance() {
        return pluginManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
}
