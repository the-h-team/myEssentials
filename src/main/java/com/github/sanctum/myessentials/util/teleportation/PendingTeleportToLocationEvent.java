package com.github.sanctum.myessentials.util.teleportation;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Encapsulates requested teleportation to a fixed location.
 */
public final class PendingTeleportToLocationEvent extends PendingTeleportEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    public PendingTeleportToLocationEvent(@NotNull Player who, @NotNull Location location) {
        super(who, location);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
