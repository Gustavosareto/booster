package me.gustavo.boostermaster.listeners;

import me.gustavo.boostermaster.BoosterMaster;
import me.gustavo.boostermaster.managers.BoosterManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;
import java.math.BigDecimal;

public class SellListener implements Listener {

    private final BoosterMaster plugin;
    private final BoosterManager manager;

    public SellListener(BoosterMaster plugin, BoosterManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onAny(Event ev) {
        String cn = ev.getClass().getName().toLowerCase();
        if (!(cn.contains("transaction") || cn.contains("sell") || cn.contains("money") || cn.contains("pay"))) return;

        try {
            Double amount = null;
            Method setAmount = null;

            for (Method m : ev.getClass().getMethods()) {
                String mn = m.getName().toLowerCase();
                if ((mn.contains("getamount") || mn.contains("getvalue") || mn.contains("getmoney")) && m.getParameterCount() == 0) {
                    Object ret = m.invoke(ev);
                    if (ret instanceof Number) amount = ((Number) ret).doubleValue();
                    else if (ret instanceof BigDecimal) amount = ((BigDecimal) ret).doubleValue();
                    else if (ret != null) {
                        try { amount = Double.parseDouble(ret.toString()); } catch (Exception ignored) {}
                    }
                }
                if ((mn.contains("setamount") || mn.contains("setvalue") || mn.contains("setmoney")) && m.getParameterCount() == 1) {
                    setAmount = m;
                }
            }

            if (amount == null) {
                try {
                    Method gt = ev.getClass().getMethod("getTransaction");
                    Object tx = gt.invoke(ev);
                    if (tx != null) {
                        for (Method m : tx.getClass().getMethods()) {
                            String mn = m.getName().toLowerCase();
                            if ((mn.contains("getamount") || mn.contains("getvalue") || mn.contains("getmoney")) && m.getParameterCount() == 0) {
                                Object ret = m.invoke(tx);
                                if (ret instanceof Number) amount = ((Number) ret).doubleValue();
                                else if (ret instanceof BigDecimal) amount = ((BigDecimal) ret).doubleValue();
                                else if (ret != null) {
                                    try { amount = Double.parseDouble(ret.toString()); } catch (Exception ignored) {}
                                }
                            }
                            if ((mn.contains("setamount") || mn.contains("setvalue") || mn.contains("setmoney")) && m.getParameterCount() == 1) {
                                setAmount = m;
                            }
                        }
                    }
                } catch (NoSuchMethodException ignored) {}
            }

            if (amount == null) return;

            Player player = null;
            for (Method m : ev.getClass().getMethods()) {
                if (m.getParameterCount() != 0) continue;
                String mn = m.getName().toLowerCase();
                if (mn.contains("getplayer") || mn.contains("getinitiator") || mn.contains("getowner") || mn.contains("gettarget")) {
                    Object ret = m.invoke(ev);
                    if (ret instanceof Player) { player = (Player) ret; break; }
                    if (ret instanceof OfflinePlayer) {
                        OfflinePlayer off = (OfflinePlayer) ret;
                        if (off.isOnline()) player = off.getPlayer();
                    }
                }
            }

            if (player == null) return;

            double mult = manager.getMultiplier(player);
            if (mult <= 1.000001) return;

            double newAmount = amount * mult;

            if (setAmount != null) {
                Class<?> pt = setAmount.getParameterTypes()[0];
                if (pt == double.class || pt == Double.class) setAmount.invoke(ev, newAmount);
                else if (pt == long.class || pt == Long.class) setAmount.invoke(ev, (long)Math.round(newAmount));
                else if (pt == float.class || pt == Float.class) setAmount.invoke(ev, (float)newAmount);
                else if (pt == BigDecimal.class) setAmount.invoke(ev, BigDecimal.valueOf(newAmount));
                else setAmount.invoke(ev, String.valueOf(newAmount));
            }

            player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.activated", "&aSeu booster foi aplicado!").replace("{multiplier}", String.valueOf(mult))));
        } catch (Throwable t) {
            plugin.getLogger().fine("BoosterMaster: failed to apply multiplier for event " + ev.getClass().getName() + " -> " + t.getMessage());
        }
    }
}
