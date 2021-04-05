/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen. However, this class directly extends a component
 *  of Bukkit API, and thus its license must be LGPL-compatible.
 */
package com.github.sanctum.myessentials.util.events;

import com.github.sanctum.myessentials.util.teleportation.TeleportRequest;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Facilitates and carries out the final teleportation.
 */
public final class TeleportEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    protected final Location from;
    protected final Location to;
    protected final Player player;
    protected final TeleportRequest request;
    private boolean cancelled = false;

    public TeleportEvent(@NotNull Player who, @NotNull Location from, @NotNull Location to) {
        this(who, from, to, null);
    }

    public TeleportEvent(@NotNull Player who, @NotNull Location from, @NotNull Location to, TeleportRequest request) {
        this.player = who;
        this.from = from;
        this.to = to;
        this.request = request;
    }

    /**
     * Get the player that is teleporting.
     *
     * @return the player that is teleporting
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * Get the original location of the player.
     *
     * @return original location of the player
     */
    public @NotNull Location getFrom() {
        return from;
    }

    /**
     * Get the target location on successful teleport.
     *
     * @return target location on successful teleport
     */
    public @NotNull Location getTo() {
        return to;
    }

    /**
     * Get the TeleportRequest associated with this event if there was one.
     *
     * @return an Optional to describe TeleportRequest for this event
     */
    public Optional<TeleportRequest> getRequest() {
        return Optional.ofNullable(request);
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
}
