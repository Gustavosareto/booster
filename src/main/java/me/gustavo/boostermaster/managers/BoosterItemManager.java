package me.gustavo.boostermaster.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class BoosterItemManager implements Listener {

    private final JavaPlugin plugin;

    public BoosterItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createBoosterItem(double multiplier, int minutes, String displayName, List<String> lore, Material mat) {
        ItemStack item = new ItemStack(mat, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', displayName.replace("{multiplier}", String.valueOf(multiplier)).replace("{minutes}", String.valueOf(minutes))));
            // replace placeholders in lore
            java.util.List<String> newLore = new java.util.ArrayList<>();
            for (String l : lore) newLore.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', l.replace("{multiplier}", String.valueOf(multiplier)).replace("{minutes}", String.valueOf(minutes))));
            meta.setLore(newLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        ItemMeta meta = e.getItem().getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String name = org.bukkit.ChatColor.stripColor(meta.getDisplayName()).toLowerCase();
        if (!name.contains("booster")) return;

        e.setCancelled(true);
        Player p = e.getPlayer();
        // parse multiplier and minutes from display name if present
        double mult = 1.0;
        int minutes = 1;
        String raw = meta.getDisplayName();
        try {
            // try to find pattern like "1.5" or "2x"
            String lower = org.bukkit.ChatColor.stripColor(raw).toLowerCase();
            if (lower.contains("1.3")) mult = 1.3;
            else if (lower.contains("1.5")) mult = 1.5;
            else if (lower.contains("2.0") || lower.contains("2x")) mult = 2.0;
            // find number between parentheses e.g. (5m) or (5min)
            int s = lower.indexOf("(");
            int e = lower.indexOf("m");
            if (s >= 0 && e > s) {
                String num = lower.substring(s+1, e).replaceAll("[^0-9]", "");
                if (!num.isEmpty()) minutes = Integer.parseInt(num);
            }
        } catch (Exception ex) { /* ignore */ }

        // Activate
        Object bm = plugin.getConfig().get("__booster_manager_instance");
        if (bm instanceof BoosterManager) {
            ((BoosterManager) bm).activate(p, mult, minutes);
        } else {
            // fallback
            new BoosterManager(plugin).activate(p, mult, minutes);
        }

        // consume item
        ItemStack it = e.getItem();
        if (it.getAmount() <= 1) p.getInventory().remove(it);
        else it.setAmount(it.getAmount()-1);
    }
}
