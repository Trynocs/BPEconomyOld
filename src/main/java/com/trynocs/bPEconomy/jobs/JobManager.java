package com.trynocs.bPEconomy.jobs;

import com.trynocs.bPEconomy.Main;
import com.trynocs.trylibs.config.Config; // TryLibs Config
import org.bukkit.Material;
// import org.bukkit.configuration.ConfigurationSection; // No longer needed
// import org.bukkit.configuration.file.FileConfiguration; // No longer needed
// import org.bukkit.configuration.file.YamlConfiguration; // No longer needed
import com.trynocs.trylibs.database.DatabaseHandler;
import org.bukkit.entity.Player;

// Remove File and IOException once fully refactored
// import java.io.File;
// import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class JobManager {

    private final Main plugin;
    private final Config jobsConfig;
    private final DatabaseHandler dbHandler; // TryLibs DatabaseHandler
    private final Map<String, Job> jobs = new HashMap<>();
    private final Map<UUID, PlayerJob> playerJobs = new HashMap<>(); // Player UUID -> PlayerJob
    // private File playerJobsFile; // Removed
    // private org.bukkit.configuration.file.FileConfiguration playerJobsConfig; // Removed

    public JobManager(Main plugin, Config jobsConfig, DatabaseHandler dbHandler) {
        this.plugin = plugin;
        this.jobsConfig = jobsConfig;
        this.dbHandler = dbHandler;
        initDatabaseTable();
        loadJobs();
        loadPlayerJobs();
    }

    private void initDatabaseTable() {
        // Use DatabaseHandler to execute a CREATE TABLE IF NOT EXISTS statement.
        // The exact table name and column names should be consistent.
        String createTableSQL = "CREATE TABLE IF NOT EXISTS bpeconomy_player_jobs (" +
                                "player_uuid VARCHAR(36) PRIMARY KEY NOT NULL," +
                                "job_name VARCHAR(255) NOT NULL" +
                                ");";
        try {
            dbHandler.executeUpdate(createTableSQL);
            plugin.getLogger().info("Player jobs database table initialized successfully.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not initialize player jobs database table!", e);
        }
    }

    private void loadJobs() {
        // jobsConfig is already loaded and passed by Main.java
        // It also handles creating the default jobs.yml from resources

        Set<String> jobKeys = jobsConfig.getKeys(false); // Get top-level keys (job names)
        if (jobKeys == null || jobKeys.isEmpty()) {
            plugin.getLogger().warning("No jobs defined in jobs.yml!");
            return;
        }

        for (String jobName : jobKeys) {
            String description = jobsConfig.getString(jobName + ".description", "No description provided.");
            // TryLibs might use getSection or direct path access for nested sections
            // Assuming getKeys can be used on a path for sub-keys or direct access to nested values
            Map<Material, Double> actions = new HashMap<>();
            Config actionsConfigSection = jobsConfig.getSection(jobName + ".actions");

            if (actionsConfigSection != null) {
                 Set<String> materialKeys = actionsConfigSection.getKeys(false);
                 if (materialKeys != null) {
                    for (String materialName : materialKeys) {
                        try {
                            Material material = Material.valueOf(materialName.toUpperCase());
                            // Use getDouble from the specific section for clarity
                            double reward = actionsConfigSection.getDouble(materialName, 0.0);
                            if (reward > 0.0) { // Only add if reward is specified
                                actions.put(material, reward);
                            }
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid material '" + materialName + "' in job '" + jobName + "'. Skipping.");
                        }
                    }
                }
            }
            Job job = new Job(jobName, description, actions);
            jobs.put(jobName.toLowerCase(), job); // Store job names in lowercase for case-insensitive matching
            plugin.getLogger().info("Loaded job: " + jobName);
        }
    }

    private void loadPlayerJobs() {
        playerJobs.clear(); // Clear in-memory cache before loading
        String selectSQL = "SELECT player_uuid, job_name FROM bpeconomy_player_jobs;";
        try (PreparedStatement pstmt = dbHandler.getConnection().prepareStatement(selectSQL); // Assuming getConnection() exists
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                try {
                    UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                    String jobName = rs.getString("job_name");
                    if (jobName != null && jobs.containsKey(jobName.toLowerCase())) {
                        playerJobs.put(playerUUID, new PlayerJob(playerUUID, jobName));
                    } else if (jobName != null) {
                        plugin.getLogger().warning("Player " + playerUUID + " has an invalid job '" + jobName + "' assigned in DB. Ignoring.");
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID string found in database. Skipping entry: " + rs.getString("player_uuid"));
                }
            }
            plugin.getLogger().info("Loaded " + playerJobs.size() + " player jobs from database.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load player jobs from database!", e);
        }
    }

    /**
     * This method is now largely obsolete as data is saved on change.
     * It could be kept for a manual full sync if necessary, but typically not called.
     * For this refactor, we will effectively disable its old functionality.
     */
    public void savePlayerJobs() {
        plugin.getLogger().info("savePlayerJobs() called. Player data is now saved on change. This method is deprecated for YAML saving.");
        // Old YAML saving logic removed.
        // If a batch DB save was desired (e.g. for players not online but data changed by admin):
        // for (Map.Entry<UUID, PlayerJob> entry : playerJobs.entrySet()) {
        //     updatePlayerJobInDb(entry.getKey(), entry.getValue().getJobName());
        // }
    }

    private void updatePlayerJobInDb(UUID playerUUID, String jobName) {
        // Uses INSERT OR REPLACE for SQLite, or similar logic for other DBs via TryLibs
        // TryLibs DatabaseHandler might have a specific method for this (e.g., dbHandler.insertOrUpdate)
        // For now, assuming a generic executeUpdate that handles common SQL.
        // For SQLite:
        String upsertSQL = "INSERT OR REPLACE INTO bpeconomy_player_jobs (player_uuid, job_name) VALUES (?, ?);";
        // For MySQL:
        // String upsertSQL = "INSERT INTO bpeconomy_player_jobs (player_uuid, job_name) VALUES (?, ?) ON DUPLICATE KEY UPDATE job_name = VALUES(job_name);";
        // The actual SQL might depend on how TryLibs DatabaseHandler is implemented or configured (e.g. which DB type it's using)

        try (PreparedStatement pstmt = dbHandler.getConnection().prepareStatement(upsertSQL)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, jobName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not update player job in database for " + playerUUID, e);
        }
    }

    private void removePlayerJobFromDb(UUID playerUUID) {
        String deleteSQL = "DELETE FROM bpeconomy_player_jobs WHERE player_uuid = ?;";
        try (PreparedStatement pstmt = dbHandler.getConnection().prepareStatement(deleteSQL)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not remove player job from database for " + playerUUID, e);
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
            playerJob.setJobName(jobName); // Update in-memory cache
        } else {
            playerJobs.put(player.getUniqueId(), new PlayerJob(player.getUniqueId(), jobName)); // Add to in-memory cache
        }
        updatePlayerJobInDb(player.getUniqueId(), jobName); // Persist change to DB
        // savePlayerJobs(); // Removed, data saved on change
        return true;
    }

    public boolean leaveJob(Player player) {
        PlayerJob playerJob = playerJobs.remove(player.getUniqueId()); // Remove from in-memory cache
        if (playerJob != null) {
            removePlayerJobFromDb(player.getUniqueId()); // Remove from DB
            // savePlayerJobs(); // Removed, data saved on change
            return true;
        }
        return false;
    }
}
