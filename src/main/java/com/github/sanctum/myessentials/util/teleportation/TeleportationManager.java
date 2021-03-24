package com.github.sanctum.myessentials.util.teleportation;

import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.util.events.MEssPendingTeleportToLocationEvent;
import com.github.sanctum.myessentials.util.events.MEssPendingTeleportToPlayerEvent;
import com.github.sanctum.myessentials.util.events.MEssTeleportEvent;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

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

    public static void registerListeners(Essentials essentials) {
        new TeleportationManager(essentials);
    }

    public static void unregisterListeners() {
        instance.listeners.clear();
        instance = null;
    }

    private class PendingTeleportListener implements Listener {
        // Prepare teleport to a Location
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPendingPlayerTeleportToLocation(MEssPendingTeleportToLocationEvent e) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    pluginManager.callEvent(new MEssTeleportEvent(e.getPlayerToTeleport(), e.getPlayerToTeleport().getLocation(), e.getDestination().toLocation()));
                }
            }.runTaskLater(plugin, e.getDelay());
        }

        // Prepare teleport to a target Player
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPendingPlayerTeleportToPlayer(MEssPendingTeleportToPlayerEvent e) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    pluginManager.callEvent(new MEssTeleportEvent(e.getPlayerToTeleport(), e.getPlayerToTeleport().getLocation(), e.getDestination().toLocation()));
                }
            }.runTaskLater(plugin, e.getDelay());
        }
    }

    /**
     * Performs the final teleport. If additional checks result in
     * this event being cancelled, this handler will not run. If
     * more sophisticated logic is desire (messages, etc), locate
     * in another listener class.
     */
    private static class TeleportListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onMEssTeleportEvent(MEssTeleportEvent e) {
            e.getPlayer().teleport(e.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
//                e.player.sendMessage("TP call sent"); // Use a separate EventHandler for messages
        }
    }
}
