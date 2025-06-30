package com.trynocs.bPEconomy.listener;

import com.trynocs.bPEconomy.Main;
import com.trynocs.bPEconomy.jobs.Job;
import com.trynocs.bPEconomy.jobs.JobManager;
import com.trynocs.bPEconomy.jobs.PlayerJob;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class JobsListener implements Listener {

    private final Main plugin;
    private final JobManager jobManager;
    private final Economy economy;

    public JobsListener(Main plugin) {
        this.plugin = plugin;
        this.jobManager = plugin.getJobManager();
        this.economy = plugin.getEconomy();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerJob playerJob = jobManager.getPlayerJob(player);

        if (playerJob == null) {
            return; // Player doesn't have a job
        }

        Job currentJob = jobManager.getJob(playerJob.getJobName());
        if (currentJob == null) {
            plugin.getLogger().warning("Player " + player.getName() + " has an invalid job '" + playerJob.getJobName() + "' assigned in JobsListener.");
            return; // Job data is corrupted or job no longer exists
        }

        Material brokenBlockMaterial = event.getBlock().getType();
        double reward = currentJob.getReward(brokenBlockMaterial);

        if (reward > 0) {
            economy.depositPlayer(player, reward);
            // Optionally send a message to the player
            // player.sendMessage(ChatColor.GREEN + "+ " + economy.format(reward) + " for breaking " + brokenBlockMaterial.toString());
        }
    }
    // TODO: Add listeners for other job actions (e.g., killing mobs, fishing, crafting, etc.)
}
