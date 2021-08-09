package com.github.sanctum.myessentials.util.teleportation;

import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.events.PendingTeleportEvent;
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
    private final TextLib textLib = TextLib.getInstance();
    private final Set<TeleportRequest> requests = new HashSet<>();
    private final Essentials plugin;

    public TeleportRunnerImpl(Essentials essentials) {
        this.plugin = essentials;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void teleportPlayer(@NotNull Player player, Destination destination) {
        Bukkit.getPluginManager().callEvent(new PendingTeleportEvent(null, player, destination));
    }

    @Override
    public void requestTeleport(@NotNull Player requester, @NotNull Player requested, TeleportRequest.Type type) throws ExistingTeleportRequestException{
        processRequest(requester, requested, type);
        requests.add(new TeleportRequestImpl(requester, requested, type));
    }

    @Override
    public void requestTeleportCustom(@NotNull Player requester, @NotNull Player requested, TeleportRequest.Type type, long expiration) throws ExistingTeleportRequestException {
        processRequest(requester, requested, type);
        requests.add(new TeleportRequestImpl(requester, requested, type, expiration));
    }

    private void processRequest(@NotNull Player requester, @NotNull Player requested, TeleportRequest.Type type) throws ExistingTeleportRequestException {
        final Optional<TeleportRequest> existingRequest = getActiveRequests().stream()
                .filter(tr -> requester == tr.requester && requested == tr.destination.player && type == tr.type)
                .findAny();
        if (existingRequest.isPresent()) throw new ExistingTeleportRequestException(existingRequest.get());
        // Message requester and requested
        requester.sendMessage(ConfiguredMessage.TPA_SENT.replace(requested.getDisplayName()));
        requester.spigot().sendMessage(textLib.textRunnable(
                ConfiguredMessage.TPA_TO_CANCEL_TEXT.toString(),
                ConfiguredMessage.TPA_TO_CANCEL_BUTTON.toString(),
                ConfiguredMessage.TPA_TO_CANCEL_TEXT2.replace(InternalCommandData.TPA_CANCEL_COMMAND.getLabel()),
                ConfiguredMessage.TPA_TO_CANCEL_HOVER.toString(),
                InternalCommandData.TPA_CANCEL_COMMAND.getLabel()));
        if (type == TeleportRequest.Type.NORMAL_TELEPORT)
            requested.sendMessage(ConfiguredMessage.TPA_REQUEST_TO_YOU.replace(requester.getDisplayName()));
        else if (type == TeleportRequest.Type.TELEPORT_HERE)
            requested.sendMessage(ConfiguredMessage.TPA_HERE_REQUESTED.replace(requester.getDisplayName()));
        requested.spigot().sendMessage(textLib.textRunnable(
                ConfiguredMessage.TPA_TO_ACCEPT_TEXT.toString(),
                ConfiguredMessage.TPA_TO_ACCEPT_BUTTON.toString(),
                ConfiguredMessage.TPA_TO_ACCEPT_TEXT2.replace(InternalCommandData.TP_ACCEPT_COMMAND.getLabel()),
                ConfiguredMessage.TPA_TO_ACCEPT_HOVER.toString(),
                InternalCommandData.TP_ACCEPT_COMMAND.getLabel()));
        requested.spigot().sendMessage(textLib.textRunnable(
                ConfiguredMessage.TPA_TO_REJECT_TEXT.toString(),
                ConfiguredMessage.TPA_TO_REJECT_BUTTON.toString(),
                ConfiguredMessage.TPA_TO_REJECT_TEXT2.replace(InternalCommandData.TP_REJECT_COMMAND.getLabel()),
                ConfiguredMessage.TPA_TO_REJECT_HOVER.toString(),
                InternalCommandData.TP_REJECT_COMMAND.getLabel()));
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
        return requests.contains(request) && !(request.getStatus() == TeleportRequest.Status.CANCELLED || request.getStatus() == TeleportRequest.Status.REJECTED);
    }

    @Override
    public @NotNull Set<TeleportRequest> getActiveRequests() {
        return requests.stream().filter(pr -> pr.getStatus() == TeleportRequest.Status.PENDING || pr.getStatus() == TeleportRequest.Status.TELEPORTING || pr.getStatus() == TeleportRequest.Status.ACCEPTED).collect(Collectors.toSet());
    }

    @Override
    public @NotNull Set<TeleportRequest> getExpiredRequests() {
        return requests.stream().filter(pr -> pr.isComplete && pr.getStatus() == TeleportRequest.Status.EXPIRED).collect(Collectors.toSet());
    }

    // Process successful teleport, move requests to successful map
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleportComplete(TeleportEvent event) {
        event.getRequest().ifPresent(request -> request.status = TeleportRequest.Status.TELEPORTING);
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
            Bukkit.getPluginManager().callEvent(new PendingTeleportEvent(this, teleporting, destination));
            cleanup();
        }

        @Override
        protected void cancelTeleport() {
            status = Status.CANCELLED;
            cleanup();
            requests.removeIf(req -> req.getPlayerTeleporting().equals(getPlayerTeleporting()));
        }

        @Override
        protected void rejectTeleport() {
            status = Status.REJECTED;
            cleanup();
            requests.removeIf(req -> req.getPlayerTeleporting().equals(getPlayerTeleporting()));
        }

        private void cleanup() {
            if (!expirationTask.isCancelled()) expirationTask.cancel();
            isComplete = true;
        }
    }
}
