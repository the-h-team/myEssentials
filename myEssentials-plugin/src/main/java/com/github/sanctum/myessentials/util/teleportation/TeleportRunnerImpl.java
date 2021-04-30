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
    public void requestTeleport(@NotNull Player requester, @NotNull Player requested, TeleportRequest.Type type) throws ExistingTeleportRequestException{
        // TODO: message code
        final Optional<TeleportRequest> existingRequest = pending.stream()
                .filter(tr -> requester == tr.requester && requested == tr.destination.player && type == tr.type)
                .findAny();
        if (existingRequest.isPresent()) throw new ExistingTeleportRequestException(existingRequest.get());
        pending.add(new TeleportRequestImpl(requester, requested, type));
    }

    @Override
    public void requestTeleportCustom(@NotNull Player requester, @NotNull Player requested, TeleportRequest.Type type, long expiration) throws ExistingTeleportRequestException {
        // TODO: messaging
        final Optional<TeleportRequest> existingRequest = pending.stream()
                .filter(tr -> requester == tr.requester && requested == tr.destination.player && type == tr.type)
                .findAny();
        if (existingRequest.isPresent()) throw new ExistingTeleportRequestException(existingRequest.get());
        pending.add(new TeleportRequestImpl(requester, requested, type, expiration));
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
        event.getRequest().ifPresent(successful::add);
    }

    private final class TeleportRequestImpl extends TeleportRequest {
        private final BukkitRunnable expirationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (expiration.isBefore(LocalDateTime.now())) {
                    if (!isComplete) {
                        pending.remove(TeleportRequestImpl.this);
                        expired.add(TeleportRequestImpl.this);
                    }
                    cancel();
                }
            }
        };

        protected TeleportRequestImpl(Player requester, Player requested, Type type, long expirationDelay) {
            super(requester, requested, type, expirationDelay);
            expirationTask.runTaskTimer(plugin, 0L, 20L);
        }

        protected TeleportRequestImpl(Player requester, Player requested, Type type) {
            super(requester, requested, type);
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
            cleanup();
        }

        @Override
        protected void cancelTeleport() {
            if (isComplete) return;
            status = Status.CANCELLED;
            cleanup();
        }

        @Override
        protected void rejectTeleport() {
            if (isComplete) return;
            status = Status.REJECTED;
            cleanup();
        }

        private void cleanup() {
            if (!expirationTask.isCancelled()) expirationTask.cancel();
            pending.remove(this);
            isComplete = true;
        }
    }
}
