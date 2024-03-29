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

import com.github.sanctum.myessentials.util.teleportation.Destination;
import com.github.sanctum.myessentials.util.teleportation.TeleportRequest;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Encapsulates a requested teleportation.
 */
public class PendingTeleportEvent extends Event implements Cancellable {
	private static final HandlerList HANDLER_LIST = new HandlerList();
	protected final Player player;
	protected final TeleportRequest request;
	protected final Destination destination;
	protected long delay;
	protected boolean cancelled;

	public PendingTeleportEvent(@Nullable TeleportRequest request, @NotNull Player player, Destination destination) {
		this(request, player, destination, 0L);
	}
	public PendingTeleportEvent(@Nullable TeleportRequest request, @NotNull Player player, Destination destination, long delay) {
		this.player = player;
		this.request = request;
		this.destination = destination;
		this.delay = delay;
	}

	/**
	 * Get the teleport request associated with this event, if applicable.
	 *
	 * @return the associated teleport request if present
	 */
	public Optional<TeleportRequest> getRequest() {
		return Optional.ofNullable(request);
	}

	/**
	 * Get the Player that may be teleported.
	 *
	 * @return player that may be teleported
	 */
	public Player getPlayerToTeleport() {
		return player;
	}

    /**
     * Set a new delay.
     * <p>
     * Delays the schedule of call of {@link TeleportEvent}.
     *
     * @param delay new delay in ticks
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }

    /**
     * Get the current delay in ticks.
     * <p>
     * Delays the schedule of call of {@link TeleportEvent}.
     * <p>
     * Defaults to 0L ticks.
     *
     * @return current delay
     */
    public long getDelay() {
        return delay;
    }

    /**
     * Get the requested teleport destination.
     * <p>
     * {@link Destination} may describe either a fixed location
     * or the dynamic location of a player. Additionally, the
     * target player (if target is a player) can be retrieved via
     * {@link Destination#getDestinationPlayer()}, which returns
     * an empty optional in the case that the destination does not
     * describe a player.
     *
     * @return requested teleport destination
     */
    public Destination getDestination() {
        return destination;
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
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
