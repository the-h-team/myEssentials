/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.util.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.jetbrains.annotations.NotNull;

/**
 * Facilitates and carries out the final teleportation.
 */
public final class MEssTeleportEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    protected final Location from;
    protected final Location to;
    protected final Player player;
    private boolean cancelled = false;

    public MEssTeleportEvent(@NotNull Player who, @NotNull Location from, @NotNull Location to) {
        this.player = who;
        this.from = from;
        this.to = to;
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
