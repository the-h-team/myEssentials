package com.github.sanctum.myessentials.util.teleportation;

import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.util.events.PendingTeleportEvent;
import com.github.sanctum.myessentials.util.events.PendingTeleportToLocationEvent;
import com.github.sanctum.myessentials.util.events.PendingTeleportToPlayerEvent;
import com.github.sanctum.myessentials.util.events.TeleportEvent;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

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
            Bukkit.getPluginManager().callEvent(new PendingTeleportToPlayerEvent(player, destinationPlayer.get()));
            return;
        }
        Bukkit.getPluginManager().callEvent(new PendingTeleportToLocationEvent(player, destination.toLocation()));
    }

    @Override
    public void requestTeleport(@NotNull Player requester, @NotNull Player target, @NotNull Player destination) {
        // TODO: message code
        pending.add(new TeleportRequestImpl(new Destination(destination), requester, target));
    }

    @Override
    public void requestTeleportCustom(@NotNull Player requester, @NotNull Player target, @NotNull Player destination, long expiration) {
        // TODO: messaging
        pending.add(new TeleportRequestImpl(new Destination(destination), requester, target, expiration));
    }

    @Override
    public void acceptTeleport(TeleportRequest request) {
        request.acceptTeleport();
    }

    @Override
    public void cancelRequest(TeleportRequest request) {
        request.cancelTeleport();
    }

    @Override
    public void rejectTeleport(TeleportRequest request) {
        request.rejectTeleport();
    }

    @Override
    public boolean queryTeleportStatus(TeleportRequest request) {
        return successful.contains(request);
    }

    @Override
    public @NotNull Set<TeleportRequest> getActiveRequests() {
        return Collections.unmodifiableSet(pending);
    }

    @Override
    public @NotNull Set<TeleportRequest> getExpiredRequests() {
        return expired;
    }

    // Process successful teleport, move requests to successful map
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleportComplete(TeleportEvent event) {
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

        protected TeleportRequestImpl(Destination destination, Player requester, Player teleporting, long expirationDelay) {
            super(destination, requester, teleporting, expirationDelay);
            expirationTask.runTaskTimer(plugin, 0L, 20L);
        }

        protected TeleportRequestImpl(Destination destination, Player requester, Player teleporting) {
            super(destination, requester, teleporting);
            expirationTask.runTaskTimer(plugin, 0L, 20L);
        }

        @Override
        protected void acceptTeleport() {
            if (isComplete) return;
            status = Status.ACCEPTED;
            final Optional<Player> destinationPlayer = destination.getDestinationPlayer();
            final PendingTeleportEvent event;
            if (destinationPlayer.isPresent()) {
                event = new PendingTeleportToPlayerEvent(teleporting, destinationPlayer.get());
            } else {
                event = new PendingTeleportToLocationEvent(teleporting, destination.toLocation());
            }
            Bukkit.getPluginManager().callEvent(event);
            isComplete = true;
        }

        @Override
        protected void cancelTeleport() {
            if (isComplete) return;
            status = Status.CANCELLED;
            if (!expirationTask.isCancelled()) expirationTask.cancel();
            pending.remove(this);
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
