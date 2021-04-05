package com.github.sanctum.myessentials.listeners;

import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.myessentials.util.events.PlayerHealEvent;
import com.github.sanctum.myessentials.util.events.PlayerPendingHealEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class HealingListener implements Listener {
    private final Plugin plugin = JavaPlugin.getProvidingPlugin(getClass());

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPendingHealEvent(PlayerPendingHealEvent e) {
        Bukkit.getPluginManager().callEvent(new PlayerHealEvent(e));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFinalHealEvent(PlayerHealEvent e) {
        final Player target = e.getTarget();
        final CommandSender healer = e.getHealer();
        double s = target.getHealth() + e.getAmount();
        e.getTarget().setHealth(s < 20 ? s : 20);
        if (healer != null) {
            if (healer instanceof Player) {
                Player heal = (Player) healer;
                Message.form(target).send("&r[&2" + plugin + "&r] Player " + heal.getName() + " healed your wounds.");
            } else {
                Message.form(target).send("&r[&2" + plugin + "&r] &c&oConsole has healed your wounds.");
            }
        } else {
            Message.form(target).send("&r[&2" + plugin + "&r] Your wounds have been healed.");
        }
    }
}
