package com.trynocs.bPEconomy.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.trynocs.bPEconomy.main;
import com.trynocs.tryLibs.TryLibs;
import com.trynocs.tryLibs.utils.database.DatabaseHandler;
import com.trynocs.tryLibs.utils.gui.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

@CommandAlias("job|jobs|berufe|beruf")
@CommandPermission("be.jobs")
public class Jobs extends BaseCommand implements Listener {

    public DatabaseHandler databaseHandler = main.getPlugin().getDatabaseHandler();

    @Default
    public void onExecute(CommandSender sender) {
        if (sender instanceof Player player) {
            Inventory inventory = main.getPlugin().getServer().createInventory(null, 9*6, main.prefix + "§aJobs");
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, TryLibs.getPlugin().getPlacerholderItem());
            }

            inventory.setItem(19, new ItemBuilder(Material.ENCHANTING_TABLE)
                    .setName("§5Enchanter")
                    .setLore("§8»", "§aErhält Geld für das Verzaubern von Items.", "§8»")
                    .setLocalizedName("enchanter")
                    .build());

            inventory.setItem(20, new ItemBuilder(Material.CRAFTING_TABLE)
                    .setName("§6Crafter")
                    .setLore("§8»", "§aErhält Geld für das Herstellen von Gegenständen.", "§8»")
                    .setLocalizedName("crafter")
                    .build());

            inventory.setItem(22, new ItemBuilder(Material.DIAMOND_PICKAXE)
                    .setName("§bMiner")
                    .setLore("§8»", "§aErhält Geld für das Abbauen von Erzen.", "§8»")
                    .setLocalizedName("miner")
                    .build());

            inventory.setItem(24, new ItemBuilder(Material.BRICKS)
                    .setName("§cBuilder")
                    .setLore("§8»", "§aErhält Geld für das Platzieren von Blöcken.", "§8»")
                    .setLocalizedName("builder")
                    .build());

            inventory.setItem(25, new ItemBuilder(Material.ANVIL)
                    .setName("§8Handwerker")
                    .setLore("§8»", "§aErhält Geld für das Reparieren von Items.", "§8»")
                    .setLocalizedName("handwerker")
                    .build());

            // Status-Anzeige mit typisierten Datenbankzugriffen
            if (!(databaseHandler.tableExists(TryLibs.getPlugin().getConfig().getString("database.economytable", "economy")))) {
                databaseHandler.createTable(TryLibs.getPlugin().getConfig().getString("database.economytable", "economy"));
                main.getPlugin().getLogger().info("Die Economy-Tabelle wurde erstellt.");
            }
            String currentJob = databaseHandler.loadData(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "job", "none");
            int jobLevel = databaseHandler.loadInt(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "joblevel", 1);

            String jobName = switch (currentJob) {
                case "enchanter" -> "§5Enchanter";
                case "crafter" -> "§6Crafter";
                case "miner" -> "§bMiner";
                case "builder" -> "§cBuilder";
                case "handwerker" -> "§8Handwerker";
                default -> "§7Arbeitslos";
            };

            inventory.setItem(49, new ItemBuilder(Material.NAME_TAG)
                    .setName("§eDein aktueller Job")
                    .setLore("§8»",
                            "§aDu bist zurzeit: " + jobName,
                            "§aLevel: §e" + jobLevel,
                            "§8»")
                    .build());

            player.openInventory(inventory);
        } else sender.sendMessage(main.beplayer);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(main.prefix + "§aJobs")) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null &&
                    event.getCurrentItem().getItemMeta().getLocalizedName() != null) {
                Player player = (Player) event.getWhoClicked();
                String newJobName = event.getCurrentItem().getItemMeta().getDisplayName();

                if (!(databaseHandler.tableExists(TryLibs.getPlugin().getConfig().getString("database.economytable", "economy")))) {
                    databaseHandler.createTable(TryLibs.getPlugin().getConfig().getString("database.economytable", "economy"));
                    main.getPlugin().getLogger().info("Die Economy-Tabelle wurde erstellt.");
                }
                String currentJob = databaseHandler.loadData(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "job", "none");

                String newJob = switch (newJobName) {
                    case "§5Enchanter" -> "enchanter";
                    case "§6Crafter" -> "crafter";
                    case "§bMiner" -> "miner";
                    case "§cBuilder" -> "builder";
                    case "§8Handwerker" -> "handwerker";
                    default -> "error";
                };

                if (currentJob.equals(newJob)) {
                    player.sendMessage(main.prefix + "§cDu hast diesen Job bereits ausgewählt.");
                    return;
                }

                // Typspezifische Methoden verwenden
                databaseHandler.saveInt(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "joblevel", 1);
                databaseHandler.saveInt(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "jobxp", 0);
                databaseHandler.saveData(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "job", newJob);

                String jobMessage = main.prefix + "§aDu hast den %s-Job §aausgewählt.";
                switch (newJob) {
                    case "enchanter" -> player.sendMessage(String.format(jobMessage, "§5Enchanter"));
                    case "crafter" -> player.sendMessage(String.format(jobMessage, "§6Crafter"));
                    case "miner" -> player.sendMessage(String.format(jobMessage, "§bMiner"));
                    case "builder" -> player.sendMessage(String.format(jobMessage, "§cBuilder"));
                    case "handwerker" -> player.sendMessage(String.format(jobMessage, "§8Handwerker"));
                }
                player.closeInventory();
            }
        }
    }

    public void increaseJobLevel(Player player) {
        // Check if table exists before performing database operations
        if (!(databaseHandler.tableExists(TryLibs.getPlugin().getConfig().getString("database.economytable", "economy")))) {
            databaseHandler.createTable(TryLibs.getPlugin().getConfig().getString("database.economytable", "economy"));
            main.getPlugin().getLogger().info("Die Economy-Tabelle wurde erstellt.");
        }
        // Direkte Inkrementierung mit typisierten Methoden
        int currentLevel = databaseHandler.loadInt(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "joblevel", 1);
        databaseHandler.saveInt(TryLibs.getPlugin().getConfig().getString("database.economytable"), player.getUniqueId(), "joblevel", currentLevel + 1);
    }

    @CatchUnknown
    public void unknown(CommandSender sender) {
        sender.sendMessage(main.prefix + "§cUsage: /jobs");
    }
}
