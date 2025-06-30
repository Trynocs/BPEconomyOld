package com.trynocs.bPEconomy;

package com.trynocs.bPEconomy;

import com.trynocs.bPEconomy.commands.JobsCommand;
import com.trynocs.bPEconomy.economy.VaultEconomy;
import com.trynocs.bPEconomy.jobs.JobManager;
import com.trynocs.bPEconomy.listener.JobsListener;
import com.trynocs.trylibs.config.Config;
import com.trynocs.trylibs.config.ConfigManager;
import com.trynocs.trylibs.database.DatabaseHandler;
// import com.trynocs.trylibs.database.DatabaseType; // Assuming TryLibs handles type selection or defaults to SQLite
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    private Economy econ = null;
    private JobManager jobManager;
    private static Main instance;

    private ConfigManager configManager;
    private Config mainConfig;
    private Config jobsConfig;
    private DatabaseHandler databaseHandler;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize TryLibs ConfigManager
        configManager = new ConfigManager(this);
        mainConfig = configManager.load("config.yml");
        // mainConfig.saveDefaultConfig(); // Ensures config.yml is created with defaults if not present

        jobsConfig = configManager.load("jobs.yml");
        jobsConfig.saveDefaultConfig(); // Ensures jobs.yml is created from resources if not present

        // Initialize TryLibs DatabaseHandler
        // Assuming default constructor uses a pre-configured DB (e.g. SQLite in plugin folder)
        // or that configuration is handled within TryLibs if a central DB is used.
        databaseHandler = new DatabaseHandler(this);
        // It's good practice for DatabaseHandler to connect and prepare itself upon instantiation or via an init method.
        // If an explicit connect method is needed: databaseHandler.connect();

        if (!setupEconomy()) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            if (databaseHandler != null) {
                databaseHandler.closeConnection(); // Close DB if setup fails mid-way
            }
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        jobManager = new JobManager(this, jobsConfig, databaseHandler); // Pass dependencies

        // Register commands
        getCommand("jobs").setExecutor(new JobsCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new JobsListener(this), this);

        getLogger().info("BPEconomy enabled!");
    }

    @Override
    public void onDisable() {
        // The new savePlayerJobs might be deprecated or changed if we save on change.
        // For now, we keep it as per current JobManager structure, but it will be refactored.
        if (jobManager != null) {
            // jobManager.savePlayerJobs(); // This method in JobManager will be refactored/removed
        }

        // Close Database Connection
        if (databaseHandler != null) {
            databaseHandler.closeConnection();
            log.info("Database connection closed.");
        }

        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        econ = new VaultEconomy(); // This also loads balances.yml
        getServer().getServicesManager().register(Economy.class, econ, this, ServicePriority.Normal);
        return true;
    }

    public Economy getEconomy() {
        return econ;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public static Main getInstance() {
        return instance;
    }

    // Getter for DatabaseHandler if other parts of the plugin might need it
    // public DatabaseHandler getDatabaseHandler() {
    // return databaseHandler;
    // }
}
