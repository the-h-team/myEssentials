/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.util.teleportation;

import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.events.PendingTeleportToLocationEvent;
import com.github.sanctum.myessentials.util.events.PendingTeleportToPlayerEvent;
import com.github.sanctum.myessentials.util.events.TeleportEvent;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Teleportation utility registration.
 */
public final class TeleportationManager {
    private static TeleportationManager instance;
    protected final PluginManager pluginManager;
    protected final JavaPlugin plugin;
    private final List<Listener> listeners = new ArrayList<>();

    private TeleportationManager(Essentials essentials) {
        if (instance != null) throw new IllegalStateException("Already initialized!");
        instance = this;
        pluginManager = essentials.getServer().getPluginManager();
        plugin = essentials;
        registerListeners();
    }

    private void registerListeners() {
        listeners.add(new PendingTeleportListener());
        listeners.add(new TeleportListener());
        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    private class PendingTeleportListener implements Listener {
        // Prepare teleport to a Location
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPendingPlayerTeleportToLocation(PendingTeleportToLocationEvent e) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (e.getRequest().isPresent()) {
                        if (e.getRequest().get().getStatus() != TeleportRequest.Status.CANCELLED && e.getRequest().get().getStatus() != TeleportRequest.Status.REJECTED) {
                            pluginManager.callEvent(new TeleportEvent(e.getPlayerToTeleport(), e.getPlayerToTeleport().getLocation(), e.getDestination().toLocation(), e.getRequest().orElse(null)));
                        }
                    } else {
                        pluginManager.callEvent(new TeleportEvent(e.getPlayerToTeleport(), e.getPlayerToTeleport().getLocation(), e.getDestination().toLocation(), null));
                    }
                }
            }.runTaskLater(plugin, e.getDelay());
        }

        // Prepare teleport to a target Player
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPendingPlayerTeleportToPlayer(PendingTeleportToPlayerEvent e) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (e.getRequest().isPresent()) {
                        if (e.getRequest().get().getStatus() != TeleportRequest.Status.CANCELLED && e.getRequest().get().getStatus() != TeleportRequest.Status.REJECTED) {
                            pluginManager.callEvent(new TeleportEvent(e.getPlayerToTeleport(), e.getPlayerToTeleport().getLocation(), e.getDestination().toLocation(), e.getRequest().orElse(null)));
                        }
                    } else {
                        pluginManager.callEvent(new TeleportEvent(e.getPlayerToTeleport(), e.getPlayerToTeleport().getLocation(), e.getDestination().toLocation(), null));
                    }
                }
            }.runTaskLater(plugin, e.getDelay());
        }
    }

    public static void registerListeners(Essentials essentials) {
        new TeleportationManager(essentials);
    }

    public static void unregisterListeners() {
        instance.listeners.clear();
        instance = null;
    }

    /**
     * Performs the final teleport. If additional checks result in
     * this event being cancelled, this handler will not run. If
     * more sophisticated logic is desire (messages, etc), locate
     * in another listener class.
     */
    private static class TeleportListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onMEssTeleportEvent(TeleportEvent e) {
            if (e.getRequest().isPresent()) {
                if (e.getRequest().get().getStatus() != TeleportRequest.Status.CANCELLED && e.getRequest().get().getStatus() != TeleportRequest.Status.REJECTED) {
                    e.getPlayer().teleport(e.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                    Message.form(e.getPlayer()).setPrefix(MyEssentialsAPI.getInstance().getPrefix()).send(ConfiguredMessage.TP_SUCCESS.replace(e.getRequest().get().getPlayerRequesting().getName()));
                }
                Schedule.sync(() -> MyEssentialsAPI.getInstance().getTeleportRunner().cancelRequest(e.getRequest().get())).run();
            } else {
                e.getPlayer().teleport(e.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
//                e.player.sendMessage("TP call sent"); // Use a separate EventHandler for messages
        }
    }
}
