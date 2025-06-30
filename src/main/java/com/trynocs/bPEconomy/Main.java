package com.trynocs.bPEconomy;

import com.trynocs.bPEconomy.commands.JobsCommand;
import com.trynocs.bPEconomy.economy.VaultEconomy;
import com.trynocs.bPEconomy.jobs.JobManager;
import com.trynocs.bPEconomy.listener.JobsListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    private Economy econ = null;
    private JobManager jobManager;
    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config if not exists
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Save jobs.yml if not exists
        saveResource("jobs.yml", false);


        if (!setupEconomy()) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        jobManager = new JobManager(this);

        // Register commands
        getCommand("jobs").setExecutor(new JobsCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new JobsListener(this), this);

        getLogger().info("BPEconomy enabled!");
    }

    @Override
    public void onDisable() {
        if (jobManager != null) {
            jobManager.savePlayerJobs();
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
}
