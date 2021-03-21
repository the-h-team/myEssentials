package com.github.sanctum.myessentials.util.teleportation;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.jetbrains.annotations.NotNull;

/**
 * Encapsulates a requested teleportation.
 * <p>
 * This class cannot be directly listened to and serves
 * only as a base for pending teleport event types.
 */
public abstract class PendingTeleportEvent extends Event implements Cancellable {
    protected final Player player;
    protected final Destination destination;
    protected long delay;
    protected boolean cancelled;

    protected PendingTeleportEvent(@NotNull Player player, @NotNull Location location) {
        this(player, new Destination(location), 0L);
    }

    protected PendingTeleportEvent(@NotNull Player player, @NotNull Player targetPlayer) {
        this(player, new Destination(targetPlayer), 0L);
    }

    protected PendingTeleportEvent(@NotNull Player player, Destination destination, long delay) {
        this.player = player;
        this.destination = destination;
        this.delay = delay;
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
     * Delays the schedule of call of {@link MEssTeleportEvent}.
     *
     * @param delay new delay in ticks
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }

    /**
     * Get the current delay in ticks.
     * <p>
     * Delays the schedule of call of {@link MEssTeleportEvent}.
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
}
