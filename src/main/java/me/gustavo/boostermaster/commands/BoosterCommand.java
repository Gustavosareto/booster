package me.gustavo.boostermaster.commands;

import me.gustavo.boostermaster.managers.BoosterItemManager;
import me.gustavo.boostermaster.managers.BoosterManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BoosterCommand implements CommandExecutor {

    private final org.bukkit.plugin.java.JavaPlugin plugin;
    private final BoosterManager manager;
    private final BoosterItemManager itemManager;

    public BoosterCommand(org.bukkit.plugin.java.JavaPlugin plugin, BoosterManager manager, BoosterItemManager itemManager) {
        this.plugin = plugin;
        this.manager = manager;
        this.itemManager = itemManager;
        plugin.getConfig().set("__booster_manager_instance", manager);
        plugin.saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar esse comando.");
            return true;
        }
        Player p = (Player) sender;

        if (args.length == 0) {
            // show info
            if (manager.isActive(p)) {
                long secs = manager.getRemainingSeconds(p);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.info", "&aSeu booster atual: &e{multiplier}x &a- Restam &e{time}").replace("{multiplier}", String.valueOf(manager.getMultiplier(p))).replace("{time}", formatTime(secs))));
            } else {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no_booster", "&7Você não possui um booster ativo.")));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!p.hasPermission("boostermaster.admin")) {
                p.sendMessage(ChatColor.RED + "Você não tem permissão.");
                return true;
            }
            if (args.length < 4) {
                p.sendMessage(ChatColor.RED + "Uso: /boosters give <player> <multiplier> <minutes>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                p.sendMessage(ChatColor.RED + "Jogador não encontrado.");
                return true;
            }
            double mult;
            int minutes;
            try {
                mult = Double.parseDouble(args[2].replace(',', '.'));
                minutes = Integer.parseInt(args[3]);
            } catch (Exception ex) {
                p.sendMessage(ChatColor.RED + "Argumentos inválidos.");
                return true;
            }

            String key = (Math.abs(mult - 1.3) < 0.001) ? "1.3x" : (Math.abs(mult - 1.5) < 0.001) ? "1.5x" : "2.0x";
            String display = plugin.getConfig().getString("boosters." + key + ".item.name", "&aBooster {multiplier}x ({minutes}m)");
            List<String> lore = plugin.getConfig().getStringList("boosters." + key + ".item.lore");
            Material mat;
            try {
                mat = Material.valueOf(plugin.getConfig().getString("boosters." + key + ".item.material", "EXP_BOTTLE"));
            } catch (Exception ex) {
                mat = Material.EXP_BOTTLE;
            }

            ItemStack item = itemManager.createBoosterItem(mult, minutes, display.replace("{multiplier}", String.valueOf(mult)).replace("{minutes}", String.valueOf(minutes)), lore, mat);
            target.getInventory().addItem(item);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.given", "&aVocê deu um booster para {player}").replace("{player}", target.getName()).replace("{multiplier}", String.valueOf(mult)).replace("{minutes}", String.valueOf(minutes))));
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.given", "&aVocê recebeu um booster!").replace("{player}", target.getName()).replace("{multiplier}", String.valueOf(mult)).replace("{minutes}", String.valueOf(minutes))));
            return true;
        }

        if (args[0].equalsIgnoreCase("ativar")) {
            ItemStack inHand = p.getItemInHand();
            if (inHand == null || !inHand.hasItemMeta() || inHand.getItemMeta().getDisplayName() == null) {
                p.sendMessage(ChatColor.RED + "Segure um booster para ativar.");
                return true;
            }
            String raw = inHand.getItemMeta().getDisplayName();
            double mult = 1.0;
            int minutes = 1;
            try {
                String lower = org.bukkit.ChatColor.stripColor(raw).toLowerCase();
                if (lower.contains("1.3")) mult = 1.3;
                else if (lower.contains("1.5")) mult = 1.5;
                else if (lower.contains("2.0") || lower.contains("2x")) mult = 2.0;
                int s = lower.indexOf("(");
                int e = lower.indexOf("m");
                if (s >= 0 && e > s) {
                    String num = lower.substring(s+1, e).replaceAll("[^0-9]", "");
                    if (!num.isEmpty()) minutes = Integer.parseInt(num);
                }
            } catch (Exception ex) {}
            manager.activate(p, mult, minutes);
            if (inHand.getAmount() <= 1) p.setItemInHand(null);
            else inHand.setAmount(inHand.getAmount()-1);
            return true;
        }

        p.sendMessage(ChatColor.RED + "Comando inválido.");
        return true;
    }

    private String formatTime(long seconds) {
        if (seconds <= 0) return "0s";
        if (seconds < 60) return seconds + "s";
        long m = seconds / 60;
        long s = seconds % 60;
        return m + "m " + s + "s";
    }
}
