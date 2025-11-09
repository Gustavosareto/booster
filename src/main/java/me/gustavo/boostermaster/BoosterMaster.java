package me.gustavo.boostermaster;

import me.gustavo.boostermaster.listeners.SellListener;
import me.gustavo.boostermaster.managers.BoosterItemManager;
import me.gustavo.boostermaster.managers.BoosterManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;

public class BoosterMaster extends JavaPlugin {

    private BoosterManager boosterManager;
    private BoosterItemManager itemManager;
    private Economy economy = null;
    private boolean useVault = true;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        useVault = getConfig().getBoolean("use-vault", true);

        // setup Vault if present
        if (useVault) {
            if (!setupVault()) {
                getLogger().warning("Vault wasn't found or couldn't hook - continuing without Vault.");
                useVault = false;
            } else {
                getLogger().info("Hooked into Vault.");
            }
        }

        boosterManager = new BoosterManager(this);
        itemManager = new BoosterItemManager(this);

        // register command
        if (getCommand("boosters") != null) {
            getCommand("boosters").setExecutor(new me.gustavo.boostermaster.commands.BoosterCommand(this, boosterManager, itemManager));
        }

        // listeners
        getServer().getPluginManager().registerEvents(new SellListener(this, boosterManager), this);
        getServer().getPluginManager().registerEvents(itemManager, this);

        getLogger().info("BoosterMaster enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("BoosterMaster disabled.");
    }

    public Economy getEconomy() {
        return economy;
    }

    private boolean setupVault() {
        try {
            if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) return false;
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) return false;
            economy = rsp.getProvider();
            return economy != null;
        } catch (Throwable t) {
            return false;
        }
    }
}
