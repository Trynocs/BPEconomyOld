package com.trynocs.bPEconomy.economy;

<<<<<<< HEAD
import com.trynocs.bPEconomy.main;
import com.trynocs.tryLibs.TryLibs;
import com.trynocs.tryLibs.utils.database.DatabaseHandler;
=======
import com.trynocs.bPEconomy.Main;
>>>>>>> origin/feature/initial-economy-and-jobs
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
<<<<<<< HEAD

import java.util.ArrayList;
import java.util.List;

public class VaultEconomy implements Economy {

    private final DatabaseHandler databaseHandler;
    private final String tableName;

    public VaultEconomy() {
        this.databaseHandler = main.getPlugin().getDatabaseHandler();
        this.tableName = TryLibs.getPlugin().getConfig().getString("database.economytable", "economy");

        // Ensure the table exists
        if (!databaseHandler.tableExists(tableName)) {
            databaseHandler.createTable(tableName);
=======
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class VaultEconomy implements Economy {

    private final Main plugin = Main.getInstance();
    private File dataFile;
    private FileConfiguration dataConfig;

    public VaultEconomy() {
        setupStorage();
    }

    private void setupStorage() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        dataFile = new File(plugin.getDataFolder(), "balances.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create balances.yml!");
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save balances.yml!");
            e.printStackTrace();
>>>>>>> origin/feature/initial-economy-and-jobs
        }
    }

    @Override
    public boolean isEnabled() {
<<<<<<< HEAD
        return main.getPlugin() != null && main.getPlugin().isEnabled();
=======
        return plugin != null && plugin.isEnabled();
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public String getName() {
        return "BPEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return String.format("%.2f", amount);
    }

    @Override
    public String currencyNamePlural() {
<<<<<<< HEAD
        return "Dollars";
=======
        return "dollars";
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public String currencyNameSingular() {
<<<<<<< HEAD
        return "Dollar";
=======
        return "dollar";
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public boolean hasAccount(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return hasAccount(player);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
<<<<<<< HEAD
        return databaseHandler.hasData(tableName, player.getUniqueId(), "money");
=======
        return dataConfig.contains(player.getUniqueId().toString());
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return getBalance(player);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
<<<<<<< HEAD
        return databaseHandler.loadDouble(tableName, player.getUniqueId(), "money", 0.0);
=======
        if (!hasAccount(player)) {
            createPlayerAccount(player); // Create account if it doesn't exist with default 0 balance
        }
        return dataConfig.getDouble(player.getUniqueId().toString(), 0.0);
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
<<<<<<< HEAD
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amounts");
        }

        if (!has(player, amount)) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }

        double currentBalance = getBalance(player);
        double newBalance = currentBalance - amount;
        databaseHandler.saveDouble(tableName, player.getUniqueId(), "money", newBalance);

        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
=======
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
        }
        if (!hasAccount(player)) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Account does not exist");
        }
        if (!has(player, amount)) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }
        double balance = getBalance(player);
        dataConfig.set(player.getUniqueId().toString(), balance - amount);
        saveData();
        return new EconomyResponse(amount, balance - amount, EconomyResponse.ResponseType.SUCCESS, null);
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
<<<<<<< HEAD
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amounts");
        }

        double currentBalance = getBalance(player);
        double newBalance = currentBalance + amount;
        databaseHandler.saveDouble(tableName, player.getUniqueId(), "money", newBalance);

        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
=======
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds");
        }
        if (!hasAccount(player)) {
            createPlayerAccount(player); // Create account if it doesn't exist
        }
        double balance = getBalance(player);
        dataConfig.set(player.getUniqueId().toString(), balance + amount);
        saveData();
        return new EconomyResponse(amount, balance + amount, EconomyResponse.ResponseType.SUCCESS, null);
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
<<<<<<< HEAD
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
=======
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
<<<<<<< HEAD
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
=======
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public EconomyResponse deleteBank(String name) {
<<<<<<< HEAD
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
=======
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public EconomyResponse bankBalance(String name) {
<<<<<<< HEAD
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
=======
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
<<<<<<< HEAD
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
=======
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
<<<<<<< HEAD
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
=======
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
<<<<<<< HEAD
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
=======
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
<<<<<<< HEAD
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
=======
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
<<<<<<< HEAD
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
=======
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
<<<<<<< HEAD
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
=======
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
<<<<<<< HEAD
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
=======
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support is not enabled.");
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public List<String> getBanks() {
<<<<<<< HEAD
        return new ArrayList<>();
=======
        return Collections.emptyList();
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
<<<<<<< HEAD
        if (!hasAccount(player)) {
            databaseHandler.saveDouble(tableName, player.getUniqueId(), "money", 0.0);
            return true;
        }
        return false;
=======
        if (hasAccount(player)) {
            return false;
        }
        dataConfig.set(player.getUniqueId().toString(), 0.0); // Default balance to 0
        saveData();
        return true;
>>>>>>> origin/feature/initial-economy-and-jobs
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }
}

