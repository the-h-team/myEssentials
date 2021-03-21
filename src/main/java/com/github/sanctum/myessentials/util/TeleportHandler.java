package com.github.sanctum.myessentials.util;

import com.github.sanctum.myessentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class TeleportHandler {
    private static final PluginManager PM = Bukkit.getPluginManager();
    private static TeleportHandler instance;
    private final JavaPlugin plugin;
    private final List<Listener> listeners = new ArrayList<>();

    private TeleportHandler(Essentials essentials) {
        if (instance != null) throw new IllegalStateException("Already initialized!");
        instance = this;
        plugin = essentials;
    }

    public abstract static class Destination<T> implements Supplier<T> {
        protected final Supplier<Location> toLoc;

        private Destination(Location to) {
            this.toLoc = () -> to;
        }

        private <Z extends Entity> Destination(Z anotherPlayer) {
            this.toLoc = anotherPlayer::getLocation;
        }

        public Location toLocation() {
            return toLoc.get();
        }

        public static Destination<Location> ofLocation(Location location) {
            return new Destination<Location>(location) {
                @Override
                public Location get() {
                    return location;
                }
            };
        }

        public static <T extends Entity> Destination<T> ofEntity(T entity) {
            return new Destination<T>(entity) {
                @Override
                public T get() {
                    return entity;
                }
            };
        }
    }

    public abstract static class PendingTeleportEvent extends PlayerEvent implements Cancellable {
        private static final HandlerList HANDLERS_LOCATIONS = new HandlerList();
        private static final HandlerList HANDLERS_ENTITIES = new HandlerList();
        protected long delay;
        protected boolean cancelled;

        protected PendingTeleportEvent(@NotNull Player player) {
            this(player, 0L);
        }

        protected PendingTeleportEvent(@NotNull Player player, long delay) {
            super(player);
            this.delay = delay;
        }

        public void setDelay(long delay) {
            this.delay = delay;
        }

        public long getDelay() {
            return delay;
        }

        public abstract Destination<?> getDestination();

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            return HANDLERS_LOCATIONS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS_LOCATIONS;
        }

        private static class PendingTeleportListener implements Listener {
            /**
             * Add delay for testing
             */
            @EventHandler(priority = EventPriority.LOW)
            public void onPendingPlayerTeleportEvent(PendingTeleportEvent e) {
                e.setDelay(20L);
                e.player.sendMessage("Delay set to 20 ticks");
            }

            /**
             * Prepare teleport to a Location
             */
            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            public void onPendingPlayerTeleportToLocation(PendingTeleportToLocationEvent e) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        PM.callEvent(new MEssTeleportEvent(e.player, e.player.getLocation(), e.getDestination().toLocation()));
                    }
                }.runTaskLater(instance.plugin, e.delay);
            }

            /**
             * Prepare teleport to a target Entity
             * @param <T> type of Entity
             */
            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            public <T extends Entity> void onPendingPlayerTeleportToEntity(PendingTeleportToEntityEvent<T> e) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        PM.callEvent(new MEssTeleportEvent(e.getPlayer(), e.getPlayer().getLocation(), e.getDestination().toLocation()));
                    }
                }.runTaskLater(instance.plugin, e.delay);
            }
        }
    }

    public static class PendingTeleportToEntityEvent<T extends Entity> extends PendingTeleportEvent {
        private static final HandlerList HANDLERS = new HandlerList();
        private final T entity;

        public PendingTeleportToEntityEvent(@NotNull Player who, T entity) {
            super(who);
            this.entity = entity;
        }

        @Override
        public Destination<T> getDestination() {
            return Destination.ofEntity(entity);
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class PendingTeleportToLocationEvent extends PendingTeleportEvent {
        private static final HandlerList HANDLERS = new HandlerList();
        private final Location location;

        public PendingTeleportToLocationEvent(@NotNull Player who, Location location) {
            super(who);
            this.location = location;
        }

        @Override
        public Destination<Location> getDestination() {
            return Destination.ofLocation(location);
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    public static class MEssTeleportEvent extends PlayerEvent implements Cancellable {
        private static final HandlerList HANDLERS = new HandlerList();

        private final Location from;
        private final Location to;
        private boolean cancelled = false;

        public MEssTeleportEvent(@NotNull Player who, Location from, Location to) {
            super(who);
            this.from = from;
            this.to = to;
        }

        public Location getFrom() {
            return from;
        }

        public Location getTo() {
            return to;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }

        /**
         * Performs the final teleport
         */
        private static class TeleportListener implements Listener {
            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            public void onMEssTeleportEvent(MEssTeleportEvent e) {
//                e.player.teleport(e.to, PlayerTeleportEvent.TeleportCause.PLUGIN);
                e.player.sendMessage("TP call sent");
            }
        }
    }

    public static void registerListeners(Essentials essentials) {
        new TeleportHandler(essentials);
        instance.listeners.add(new PendingTeleportEvent.PendingTeleportListener());
        instance.listeners.add(new MEssTeleportEvent.TeleportListener());
        for (Listener listener : instance.listeners) {
            essentials.getServer().getPluginManager().registerEvents(listener, essentials);
        }
    }

    public static void unregisterListeners() {
        instance.listeners.clear();
        instance = null;
    }
}
