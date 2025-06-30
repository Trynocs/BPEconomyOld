package com.trynocs.bPEconomy.jobs;

import com.trynocs.bPEconomy.Main;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class JobManager {

    private final Main plugin;
    private final Map<String, Job> jobs = new HashMap<>();
    private final Map<UUID, PlayerJob> playerJobs = new HashMap<>(); // Player UUID -> PlayerJob
    private File playerJobsFile;
    private FileConfiguration playerJobsConfig;

    public JobManager(Main plugin) {
        this.plugin = plugin;
        loadJobs();
        loadPlayerJobs();
    }

    private void loadJobs() {
        File jobsFile = new File(plugin.getDataFolder(), "jobs.yml");
        if (!jobsFile.exists()) {
            plugin.saveResource("jobs.yml", false);
        }
        FileConfiguration jobsConfig = YamlConfiguration.loadConfiguration(jobsFile);
        ConfigurationSection jobsSection = jobsConfig.getConfigurationSection("");
        if (jobsSection == null) {
            plugin.getLogger().warning("No jobs defined in jobs.yml!");
            return;
        }

        for (String jobName : jobsSection.getKeys(false)) {
            String description = jobsSection.getString(jobName + ".description", "No description provided.");
            ConfigurationSection actionsSection = jobsSection.getConfigurationSection(jobName + ".actions");
            Map<Material, Double> actions = new HashMap<>();
            if (actionsSection != null) {
                for (String materialName : actionsSection.getKeys(false)) {
                    try {
                        Material material = Material.valueOf(materialName.toUpperCase());
                        double reward = actionsSection.getDouble(materialName);
                        actions.put(material, reward);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material '" + materialName + "' in job '" + jobName + "'. Skipping.");
                    }
                }
            }
            Job job = new Job(jobName, description, actions);
            jobs.put(jobName.toLowerCase(), job); // Store job names in lowercase for case-insensitive matching
            plugin.getLogger().info("Loaded job: " + jobName);
        }
    }

    private void loadPlayerJobs() {
        playerJobsFile = new File(plugin.getDataFolder(), "player_jobs.yml");
        if (!playerJobsFile.exists()) {
            try {
                playerJobsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create player_jobs.yml!", e);
            }
        }
        playerJobsConfig = YamlConfiguration.loadConfiguration(playerJobsFile);
        ConfigurationSection playersSection = playerJobsConfig.getConfigurationSection("players");
        if (playersSection != null) {
            for (String uuidString : playersSection.getKeys(false)) {
                try {
                    UUID playerUUID = UUID.fromString(uuidString);
                    String jobName = playersSection.getString(uuidString + ".job");
                    if (jobName != null && jobs.containsKey(jobName.toLowerCase())) {
                        playerJobs.put(playerUUID, new PlayerJob(playerUUID, jobName));
                    } else if (jobName != null) {
                        plugin.getLogger().warning("Player " + uuidString + " has an invalid job '" + jobName + "' assigned. Ignoring.");
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID string '" + uuidString + "' in player_jobs.yml. Skipping.");
                }
            }
        }
    }

    public void savePlayerJobs() {
        ConfigurationSection playersSection = playerJobsConfig.createSection("players");
        for (Map.Entry<UUID, PlayerJob> entry : playerJobs.entrySet()) {
            playersSection.set(entry.getKey().toString() + ".job", entry.getValue().getJobName());
        }
        try {
            playerJobsConfig.save(playerJobsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save player_jobs.yml!", e);
        }
    }

    public Job getJob(String jobName) {
        return jobs.get(jobName.toLowerCase());
    }

    public Map<String, Job> getAllJobs() {
        return new HashMap<>(jobs); // Return a copy
    }

    public PlayerJob getPlayerJob(Player player) {
        return playerJobs.get(player.getUniqueId());
    }

    public boolean setPlayerJob(Player player, String jobName) {
        jobName = jobName.toLowerCase();
        if (!jobs.containsKey(jobName)) {
            return false; // Job does not exist
        }
        PlayerJob playerJob = playerJobs.get(player.getUniqueId());
        if (playerJob != null) {
            playerJob.setJobName(jobName);
        } else {
            playerJobs.put(player.getUniqueId(), new PlayerJob(player.getUniqueId(), jobName));
        }
        savePlayerJobs();
        return true;
    }

    public boolean leaveJob(Player player) {
        PlayerJob playerJob = playerJobs.remove(player.getUniqueId());
        if (playerJob != null) {
            savePlayerJobs();
            return true;
        }
        return false;
    }
}
