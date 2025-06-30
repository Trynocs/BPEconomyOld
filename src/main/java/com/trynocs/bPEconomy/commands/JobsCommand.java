package com.trynocs.bPEconomy.commands;

import com.trynocs.bPEconomy.Main;
import com.trynocs.bPEconomy.jobs.Job;
import com.trynocs.bPEconomy.jobs.JobManager;
import com.trynocs.bPEconomy.jobs.PlayerJob;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JobsCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final JobManager jobManager;

    public JobsCommand(Main plugin) {
        this.plugin = plugin;
        this.jobManager = plugin.getJobManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "join":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /jobs join <job_name>");
                    return true;
                }
                String jobToJoin = args[1];
                Job job = jobManager.getJob(jobToJoin);
                if (job == null) {
                    player.sendMessage(ChatColor.RED + "Job '" + jobToJoin + "' not found.");
                    return true;
                }
                if (jobManager.setPlayerJob(player, job.getName())) {
                    player.sendMessage(ChatColor.GREEN + "You have joined the " + job.getName() + " job!");
                } else {
                    player.sendMessage(ChatColor.RED + "Could not join job. It might not exist.");
                }
                break;
            case "leave":
                PlayerJob currentJob = jobManager.getPlayerJob(player);
                if (currentJob == null) {
                    player.sendMessage(ChatColor.RED + "You don't have a job to leave.");
                    return true;
                }
                if (jobManager.leaveJob(player)) {
                    player.sendMessage(ChatColor.GREEN + "You have left your job.");
                } else {
                    player.sendMessage(ChatColor.RED + "Could not leave job."); // Should not happen if they have one
                }
                break;
            case "info":
                if (args.length < 2) {
                    // Display info about current job if player has one
                    PlayerJob pJob = jobManager.getPlayerJob(player);
                    if (pJob != null) {
                        Job cJob = jobManager.getJob(pJob.getJobName());
                        if (cJob != null) {
                            sendJobInfo(player, cJob);
                        } else {
                             player.sendMessage(ChatColor.RED + "Your current job data is corrupted. Please rejoin a job.");
                        }
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "You currently don't have a job. Use '/jobs list' to see available jobs and '/jobs join <job_name>' to join one.");
                        player.sendMessage(ChatColor.YELLOW + "Or use '/jobs info <job_name>' to get info about a specific job.");
                    }
                    return true;
                }
                String jobToInfo = args[1];
                Job infoJob = jobManager.getJob(jobToInfo);
                if (infoJob == null) {
                    player.sendMessage(ChatColor.RED + "Job '" + jobToInfo + "' not found.");
                    return true;
                }
                sendJobInfo(player, infoJob);
                break;
            case "list":
                player.sendMessage(ChatColor.GOLD + "--- Available Jobs ---");
                Map<String, Job> allJobs = jobManager.getAllJobs();
                if (allJobs.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "No jobs are currently available.");
                } else {
                    for (Job j : allJobs.values()) {
                        player.sendMessage(ChatColor.AQUA + j.getName() + ChatColor.GRAY + " - " + ChatColor.WHITE + j.getDescription());
                    }
                }
                break;
            default:
                sendHelpMessage(player);
                break;
        }
        return true;
    }

    private void sendJobInfo(Player player, Job job) {
        player.sendMessage(ChatColor.GOLD + "--- Job Info: " + ChatColor.AQUA + job.getName() + ChatColor.GOLD + " ---");
        player.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + job.getDescription());
        player.sendMessage(ChatColor.YELLOW + "Actions & Rewards:");
        if (job.getActions().isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "  No specific actions defined for this job.");
        } else {
            for (Map.Entry<Material, Double> entry : job.getActions().entrySet()) {
                player.sendMessage(ChatColor.GREEN + "  - " + entry.getKey().toString() + ": " + ChatColor.WHITE + plugin.getEconomy().format(entry.getValue()));
            }
        }
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "--- Jobs Help ---");
        player.sendMessage(ChatColor.AQUA + "/jobs list" + ChatColor.GRAY + " - List available jobs.");
        player.sendMessage(ChatColor.AQUA + "/jobs join <job_name>" + ChatColor.GRAY + " - Join a job.");
        player.sendMessage(ChatColor.AQUA + "/jobs leave" + ChatColor.GRAY + " - Leave your current job.");
        player.sendMessage(ChatColor.AQUA + "/jobs info [job_name]" + ChatColor.GRAY + " - Get info about your current job or a specific job.");
        PlayerJob pJob = jobManager.getPlayerJob(player);
        if (pJob != null) {
             player.sendMessage(ChatColor.YELLOW + "Your current job: " + ChatColor.GREEN + pJob.getJobName());
        } else {
             player.sendMessage(ChatColor.YELLOW + "You currently don't have a job.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("join", "leave", "info", "list").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("info")) {
                return jobManager.getAllJobs().keySet().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}
