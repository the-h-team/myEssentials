package com.github.sanctum.myessentials.util.teleportation;

import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.util.events.MEssPendingTeleportEvent;
import com.github.sanctum.myessentials.util.events.MEssPendingTeleportToLocationEvent;
import com.github.sanctum.myessentials.util.events.MEssPendingTeleportToPlayerEvent;
import com.github.sanctum.myessentials.util.events.MEssTeleportEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class TeleportRunnerImpl implements TeleportRunner, Listener {
    private final Set<TeleportRequest> pending = new HashSet<>();
    private final Set<TeleportRequest> successful = new HashSet<>();
    private final Set<TeleportRequest> expired = new HashSet<>();
    private final Essentials plugin;

    public TeleportRunnerImpl(Essentials essentials) {
        this.plugin = essentials;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void teleportPlayer(@NotNull Player player, Destination destination) {
        final Optional<Player> destinationPlayer = destination.getDestinationPlayer();
        if (destinationPlayer.isPresent()) {
            Bukkit.getPluginManager().callEvent(new MEssPendingTeleportToPlayerEvent(player, destinationPlayer.get()));
            return;
        }
        Bukkit.getPluginManager().callEvent(new MEssPendingTeleportToLocationEvent(player, destination.toLocation()));
    }

    @Override
    public TeleportRequest requestTeleport(@NotNull Player requester, @NotNull Player target) {
        // TODO: message code
        return new TeleportRequestImpl(new Destination(target), requester);
    }

    @Override
    public TeleportRequest requestTeleportCustom(@NotNull Player requester, @NotNull Player target, long expiration) {
        // TODO: messaging
        return new TeleportRequestImpl(new Destination(target), target, expiration);
    }

    @Override
    public void acceptTeleport(TeleportRequest request) {
        request.acceptTeleport();
    }

    @Override
    public void rejectTeleport(TeleportRequest request) {
        request.rejectTeleport();
    }

    @Override
    public boolean queryTeleportStatus(TeleportRequest request) {
        return successful.contains(request);
    }

    // Process successful teleport, move requests to successful map
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleportComplete(MEssTeleportEvent event) {
        event.getRequest().ifPresent(request -> {
            pending.remove(request);
            successful.add(request);
        });
    }

    private final class TeleportRequestImpl extends TeleportRequest {
        private final BukkitRunnable expirationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (time.isBefore(LocalDateTime.now())) {
                    if (!isComplete) {
                        pending.remove(TeleportRequestImpl.this);
                        expired.add(TeleportRequestImpl.this);
                    }
                    cancel();
                }
            }
        };

        protected TeleportRequestImpl(Destination destination, Player teleporting, long expirationDelay) {
            super(destination, teleporting, expirationDelay);
            pending.add(this);
            expirationTask.runTaskTimer(plugin, 0L, 20L);
        }

        protected TeleportRequestImpl(Destination destination, Player teleporting) {
            super(destination, teleporting);
            pending.add(this);
            expirationTask.runTaskTimer(plugin, 0L, 20L);
        }

        @Override
        protected void acceptTeleport() {
            if (isComplete) return;
            status = Status.ACCEPTED;
            final Optional<Player> destinationPlayer = destination.getDestinationPlayer();
            final MEssPendingTeleportEvent event;
            if (destinationPlayer.isPresent()) {
                event = new MEssPendingTeleportToPlayerEvent(teleporting, destinationPlayer.get());
            } else {
                event = new MEssPendingTeleportToLocationEvent(teleporting, destination.toLocation());
            }
            Bukkit.getPluginManager().callEvent(event);
            isComplete = true;
        }

        @Override
        protected void rejectTeleport() {
            if (isComplete) return;
            status = Status.REJECTED;
            pending.remove(this);
            isComplete = true;
        }
    }
}
