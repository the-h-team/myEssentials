package com.github.sanctum.myessentials.listeners;

import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.events.PlayerFeedEvent;
import com.github.sanctum.myessentials.util.events.PlayerHealEvent;
import com.github.sanctum.myessentials.util.events.PlayerPendingFeedEvent;
import com.github.sanctum.myessentials.util.events.PlayerPendingHealEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class HealingListener implements Listener {
    private final Plugin plugin = JavaPlugin.getProvidingPlugin(getClass());

    public HealingListener() {

    }

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
                Message.form(target).send(ConfiguredMessage.PLAYER_HEALED_YOU.replace(plugin, heal.getName()));
            } else {
                Message.form(target).send(ConfiguredMessage.CONSOLE_HEALED_YOU.replace(plugin));
            }
        } else {
            Message.form(target).send(ConfiguredMessage.HEALED.replace(plugin));
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPendingFeedEvent(PlayerPendingFeedEvent e) {
        Bukkit.getPluginManager().callEvent(new PlayerFeedEvent(e));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFinalHealEvent(PlayerFeedEvent e) {
        final Player target = e.getTarget();
        final CommandSender healer = e.getHealer();

        int food_level = target.getFoodLevel() + e.getAmountReal();

        food_level = Math.min(food_level, 20);
        e.getTarget().setFoodLevel(food_level);

        // Only set saturation if food is full
        if (food_level == 20){
            e.getTarget().setSaturation(20);
        }

        if (healer != null) {
            if (healer instanceof Player) {
                Player heal = (Player) healer;
                Message.form(target).send(ConfiguredMessage.PLAYER_FED_YOU.replace(plugin, heal.getName()));
            } else {
                Message.form(target).send(ConfiguredMessage.CONSOLE_FED_YOU.replace(plugin));
            }
        } else {
            Message.form(target).send(ConfiguredMessage.FED.replace(plugin));
        }
    }

}
