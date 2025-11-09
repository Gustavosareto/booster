package me.gustavo.boostermaster.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BoosterManager {

    private final Map<UUID, Double> activeMultiplier = new ConcurrentHashMap<>();
    private final Map<UUID, Long> expiry = new ConcurrentHashMap<>();
    private final org.bukkit.plugin.java.JavaPlugin plugin;

    public BoosterManager(org.bukkit.plugin.java.JavaPlugin plugin) {
        this.plugin = plugin;
        startExpiryTask();
    }

    public boolean isActive(Player p) {
        cleanup(p.getUniqueId());
        return activeMultiplier.containsKey(p.getUniqueId());
    }

    public double getMultiplier(Player p) {
        cleanup(p.getUniqueId());
        return activeMultiplier.getOrDefault(p.getUniqueId(), 1.0);
    }

    public long getRemainingSeconds(Player p) {
        cleanup(p.getUniqueId());
        Long e = expiry.get(p.getUniqueId());
        if (e == null) return 0;
        long remaining = (e - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    public void activate(Player p, double multiplier, int minutes) {
        activeMultiplier.put(p.getUniqueId(), multiplier);
        expiry.put(p.getUniqueId(), System.currentTimeMillis() + minutes * 60L * 1000L);
        p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.activated", "&aBooster ativado!").replace("{multiplier}", String.valueOf(multiplier)).replace("{minutes}", String.valueOf(minutes))));
    }

    public void deactivate(Player p) {
        activeMultiplier.remove(p.getUniqueId());
        expiry.remove(p.getUniqueId());
        p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.expired", "&cBooster expirou!")));
    }

    private void cleanup(UUID uuid) {
        Long e = expiry.get(uuid);
        if (e != null && e <= System.currentTimeMillis()) {
            expiry.remove(uuid);
            activeMultiplier.remove(uuid);
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.expired", "&cBooster expirou!")));
            }
        }
    }

    private void startExpiryTask() {
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                for (UUID u : expiry.keySet().toArray(new UUID[0])) {
                    cleanup(u);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
