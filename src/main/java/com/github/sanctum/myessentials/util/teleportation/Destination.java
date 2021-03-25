/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.util.teleportation;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Encapsulates a raw location or the dynamic location of a Player.
 */
public final class Destination {
    protected final Supplier<Location> toLoc;
    protected final Player player;

    /**
     * Construct a destination based on a fixed location.
     *
     * @param to target destination
     */
    public Destination(Location to) {
        this.toLoc = () -> to;
        this.player = null;
    }

    /**
     * Construct a destination based on a Player.
     *
     * @param player player to target
     */
    public Destination(@NotNull Player player) {
        this.toLoc = player::getLocation;
        this.player = player;
    }

    /**
     * Get the location of this Destination immediately.
     * <p>
     * For players, returns the player's location at this exact moment.
     * Encapsulates {@link Player#getLocation()} and thus is likely not
     * thread-safe.
     * <p>
     * For simple, fixed locations, the originally-provided Location object
     * is returned. Note that {@link Location} is mutable, and this class
     * makes no attempt to clone the Location provided. If you make changes
     * on the Location object used to create this Destination they may be
     * reflected in this method call.
     *
     * @return current player location or simple Location
     */
    public Location toLocation() {
        return toLoc.get();
    }

    /**
     * If this Destination represents a Player, this
     * method will return an optional which describes
     * that player. Otherwise an empty optional is
     * returned.
     *
     * @return Optional describing a player, if present
     */
    public Optional<Player> getDestinationPlayer() {
        return Optional.ofNullable(player);
    }
}
