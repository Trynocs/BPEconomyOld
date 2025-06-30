package com.trynocs.bPEconomy.listener;

<<<<<<< HEAD
import com.trynocs.bPEconomy.main;
import com.trynocs.tryLibs.TryLibs;
import com.trynocs.tryLibs.utils.database.DatabaseHandler;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;

public class JobsListener implements Listener {

    public DatabaseHandler databaseHandler = main.getPlugin().getDatabaseHandler();

    // Basic Blöcke laut Vorgabe
    private static final Set<Material> BASIC_BLOCKS = Set.of(
            Material.CLAY, Material.STONE, Material.COBBLESTONE, Material.DEEPSLATE, Material.COBBLED_DEEPSLATE,
            Material.GRANITE, Material.DIORITE, Material.ANDESITE, Material.CALCITE, Material.TUFF,
            Material.DRIPSTONE_BLOCK, Material.POINTED_DRIPSTONE, Material.SMOOTH_BASALT
    );

    // Erz-Multiplikatoren
    private static final Map<Material, Double> ORE_MULTIPLIERS = Map.ofEntries(
            Map.entry(Material.COAL_ORE, 1.5), Map.entry(Material.DEEPSLATE_COAL_ORE, 1.5),
            Map.entry(Material.IRON_ORE, 2.0), Map.entry(Material.DEEPSLATE_IRON_ORE, 2.0),
            Map.entry(Material.AMETHYST_BLOCK, 4.0), Map.entry(Material.BUDDING_AMETHYST, 4.0),
            Map.entry(Material.GOLD_ORE, 5.0), Map.entry(Material.DEEPSLATE_GOLD_ORE, 5.0),
            Map.entry(Material.COPPER_ORE, 6.0), Map.entry(Material.DEEPSLATE_COPPER_ORE, 6.0),
            Map.entry(Material.LAPIS_ORE, 8.0), Map.entry(Material.DEEPSLATE_LAPIS_ORE, 8.0),
            Map.entry(Material.REDSTONE_ORE, 10.0), Map.entry(Material.DEEPSLATE_REDSTONE_ORE, 10.0),
            Map.entry(Material.DIAMOND_ORE, 14.0), Map.entry(Material.DEEPSLATE_DIAMOND_ORE, 14.0),
            Map.entry(Material.EMERALD_ORE, 16.0), Map.entry(Material.DEEPSLATE_EMERALD_ORE, 16.0),
            Map.entry(Material.NETHER_QUARTZ_ORE, 20.0)
    );

    // XP-Kurven für Levelaufstieg
    private static int getRequiredXP(int level) {
        if (level == 1) return 100;
        if (level == 2) return 250;
        if (level == 3) return 400;
        if (level == 4) return 550;
        if (level == 5) return 700;
        if (level == 6) return 850;
        if (level == 7) return 1000;
        if (level == 8) return 1500;
        if (level == 9) return 2000;
        if (level >= 10 && level < 20) return (int) (getRequiredXP(level - 1) * 1.5);
        if (level >= 20 && level < 30) return (int) (getRequiredXP(level - 1) * 1.75);
        if (level >= 30 && level < 40) return (int) (getRequiredXP(level - 1) * 2.0);
        if (level >= 40 && level < 50) return (int) (getRequiredXP(level - 1) * 2.25);
        if (level >= 50 && level < 60) return (int) (getRequiredXP(level - 1) * 2.5);
        if (level >= 60 && level < 70) return (int) (getRequiredXP(level - 1) * 2.75);
        if (level >= 70 && level < 80) return (int) (getRequiredXP(level - 1) * 3.0);
        if (level >= 80 && level < 90) return (int) (getRequiredXP(level - 1) * 3.5);
        if (level >= 90 && level < 100) return (int) (getRequiredXP(level - 1) * 4.0);
        if (level >= 100 && level < 150) return (int) (getRequiredXP(level - 1) * 5.0);
        if (level >= 150 && level < 200) return (int) (getRequiredXP(level - 1) * 6.0);
        return Integer.MAX_VALUE;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!(databaseHandler.tableExists(TryLibs.getPlugin().getConfig().getString("database.economytable", "economy")))) {
            databaseHandler.createTable(TryLibs.getPlugin().getConfig().getString("database.economytable", "economy"));
            main.getPlugin().getLogger().info("Die Economy-Tabelle wurde erstellt.");
        }

        if (!databaseHandler.loadData(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "job", "none").equals("miner")) return;

        Material block = event.getBlock().getType();
        int level = Math.min(databaseHandler.loadInt(player.getUniqueId(), "joblevel", 1), 200);

        double money = 0.0;
        int xp = 0;

        if (BASIC_BLOCKS.contains(block)) {
            money = 0.1 * level;
            xp = 1 * level;
        } else if (ORE_MULTIPLIERS.containsKey(block)) {
            double multi = ORE_MULTIPLIERS.get(block);
            money = 0.1 * level * multi;
            xp = (int) Math.round(1 * level * multi);
        } else {
            return;
        }

        addMoney(player, money);
        addJobXP(player, xp);

        // Actionbar: Zeige Vergütung und XP
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent("§a+ " + String.format("%.2f", money) + " $ §7| §b+ " + xp + " XP"));
        sendXPProgress(player);
    }

    private void addMoney(Player player, double amount) {
        if (!(databaseHandler.tableExists(TryLibs.getPlugin().getConfig().getString("database.economytable", "economy")))) {
            databaseHandler.createTable(TryLibs.getPlugin().getConfig().getString("database.economytable", "economy"));
            main.getPlugin().getLogger().info("Die Economy-Tabelle wurde erstellt.");
        }
        double currentMoney = databaseHandler.loadDouble(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "money", 0.0);
        databaseHandler.saveDouble(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "money", currentMoney + amount);
    }

    private void addJobXP(Player player, int xpAmount) {
        if (!(databaseHandler.tableExists(TryLibs.getPlugin().getConfig().getString("database.economytable", "economy")))) {
            databaseHandler.createTable(TryLibs.getPlugin().getConfig().getString("database.economytable", "economy"));
            main.getPlugin().getLogger().info("Die Economy-Tabelle wurde erstellt.");
        }
        int currentXP = databaseHandler.loadInt(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "jobxp", 0);
        int currentLevel = Math.min(databaseHandler.loadInt(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "joblevel", 1), 200);
        int newXP = currentXP + xpAmount;

        int requiredXP = getRequiredXP(currentLevel);

        if (currentLevel < 200 && newXP >= requiredXP) {
            currentLevel++;
            newXP -= requiredXP;
            databaseHandler.saveInt(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "joblevel", currentLevel);
            player.sendMessage(main.prefix + "§aLevel Up! Du bist jetzt Level §e" + currentLevel + "§a!");
            player.sendMessage(main.prefix + "§7Nächstes Level in: §e" + getRequiredXP(currentLevel) + " XP");
        }

        databaseHandler.saveInt(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "jobxp", newXP);
    }

    private void sendXPProgress(Player player) {
        if (!(databaseHandler.tableExists(TryLibs.getPlugin().getConfig().getString("database.economytable", "economy")))) {
            databaseHandler.createTable(TryLibs.getPlugin().getConfig().getString("database.economytable", "economy"));
            main.getPlugin().getLogger().info("Die Economy-Tabelle wurde erstellt.");
        }
        int currentXP = databaseHandler.loadInt(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "jobxp", 0);
        int currentLevel = Math.min(databaseHandler.loadInt(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "joblevel", 1), 200);
        int requiredXP = getRequiredXP(currentLevel);

        int progressBarLength = 20;
        float progress = Math.min(1f, (float) currentXP / requiredXP);
        int filledBars = (int) (progressBarLength * progress);

        StringBuilder progressBar = new StringBuilder("§8[");
        for (int i = 0; i < progressBarLength; i++) {
            if (i < filledBars) {
                progressBar.append("§a■");
            } else {
                progressBar.append("§7■");
            }
        }
        progressBar.append("§8]");

        int percent = (int) (progress * 100);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent("§7XP: " + progressBar + " §e" + currentXP + "§7/§e" + requiredXP + " §7(§e" + percent + "%§7)"));
    }
=======
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
>>>>>>> origin/feature/initial-economy-and-jobs
}
