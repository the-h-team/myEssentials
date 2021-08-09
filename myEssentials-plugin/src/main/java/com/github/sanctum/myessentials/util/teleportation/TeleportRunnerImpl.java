package com.github.sanctum.myessentials.util.teleportation;

import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.util.events.PendingTeleportEvent;
import com.github.sanctum.myessentials.util.events.PendingTeleportToLocationEvent;
import com.github.sanctum.myessentials.util.events.PendingTeleportToPlayerEvent;
import com.github.sanctum.myessentials.util.events.TeleportEvent;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public final class TeleportRunnerImpl implements TeleportRunner, Listener {
    private final Set<TeleportRequest> CACHE = new HashSet<>();
    private final Essentials plugin;

    public TeleportRunnerImpl(Essentials essentials) {
        this.plugin = essentials;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void teleportPlayer(@NotNull Player player, Destination destination) {
        final Optional<Player> destinationPlayer = destination.getDestinationPlayer();
        if (destinationPlayer.isPresent()) {
            Bukkit.getPluginManager().callEvent(new PendingTeleportToPlayerEvent(null, player, destinationPlayer.get()));
            return;
        }
        try {
            Bukkit.getPluginManager().callEvent(new PendingTeleportToLocationEvent(null, player, destination.toLocation()));
        } catch (MaxWorldCoordinatesException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void requestTeleport(@NotNull Player requester, @NotNull Player requested, TeleportRequest.Type type) throws ExistingTeleportRequestException{
        // TODO: message code
        final Optional<TeleportRequest> existingRequest = getActiveRequests().stream()
                .filter(tr -> requester == tr.requester && requested == tr.destination.player && type == tr.type)
                .findAny();
        if (existingRequest.isPresent()) throw new ExistingTeleportRequestException(existingRequest.get());
        CACHE.add(new TeleportRequestImpl(requester, requested, type));
    }

    @Override
    public void requestTeleportCustom(@NotNull Player requester, @NotNull Player requested, TeleportRequest.Type type, long expiration) throws ExistingTeleportRequestException {
        // TODO: messaging
        final Optional<TeleportRequest> existingRequest = getActiveRequests().stream()
                .filter(tr -> requester == tr.requester && requested == tr.destination.player && type == tr.type)
                .findAny();
        if (existingRequest.isPresent()) throw new ExistingTeleportRequestException(existingRequest.get());
        CACHE.add(new TeleportRequestImpl(requester, requested, type, expiration));
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
        return CACHE.contains(request) && !(request.getStatus() == TeleportRequest.Status.CANCELLED || request.getStatus() == TeleportRequest.Status.REJECTED);
    }

    @Override
    public @NotNull Set<TeleportRequest> getActiveRequests() {
        return CACHE.stream().filter(pr -> pr.getStatus() == TeleportRequest.Status.PENDING || pr.getStatus() == TeleportRequest.Status.TELEPORTING || pr.getStatus() == TeleportRequest.Status.ACCEPTED).collect(Collectors.toSet());
    }

    @Override
    public @NotNull Set<TeleportRequest> getExpiredRequests() {
        return CACHE.stream().filter(pr -> pr.isComplete && pr.getStatus() == TeleportRequest.Status.EXPIRED).collect(Collectors.toSet());
    }

    // Process successful teleport, move requests to successful map
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleportComplete(TeleportEvent event) {
        event.getRequest().ifPresent(request -> {
            request.status = TeleportRequest.Status.TELEPORTING;
        });
    }

    private final class TeleportRequestImpl extends TeleportRequest {
        private final BukkitRunnable expirationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (expiration.isBefore(LocalDateTime.now())) {
                    if (!isComplete) {
                        TeleportRequestImpl.this.status = Status.EXPIRED;
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
                event = new PendingTeleportToPlayerEvent(this, teleporting, destinationPlayer.get());
            } else {
                try {
                    event = new PendingTeleportToLocationEvent(this, teleporting, destination.toLocation());
                } catch (MaxWorldCoordinatesException e) {
                    throw new IllegalStateException(e);
                }
            }
            Bukkit.getPluginManager().callEvent(event);
            cleanup();
        }

        @Override
        protected void cancelTeleport() {
            status = Status.CANCELLED;
            cleanup();
            CACHE.removeIf(req -> req.getPlayerTeleporting().equals(getPlayerTeleporting()));
        }

        @Override
        protected void rejectTeleport() {
            status = Status.REJECTED;
            cleanup();
            CACHE.removeIf(req -> req.getPlayerTeleporting().equals(getPlayerTeleporting()));
        }

        private void cleanup() {
            if (!expirationTask.isCancelled()) expirationTask.cancel();
            isComplete = true;
        }
    }
}
