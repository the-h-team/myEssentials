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
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Encapsulates a raw location or the dynamic location of a Player.
 */
public final class Destination {
    private static final double WORLD_MAX_XZ = 30_000_001d;
    private static final double WORLD_MAX_XZ_NEGATIVE = -WORLD_MAX_XZ;
    protected final Supplier<Location> toLoc;
    protected final Player player;

    /**
     * Construct a Destination based on a fixed location.
     *
     * @param to target destination
     * @throws MaxWorldCoordinatesException if x/z values are invalid
     */
    public Destination(Location to) throws MaxWorldCoordinatesException {
        final double x = to.getX();
        final double z = to.getZ();
        boolean xOut = (x > WORLD_MAX_XZ || x < WORLD_MAX_XZ_NEGATIVE);
        boolean zOut = (z > WORLD_MAX_XZ || z < WORLD_MAX_XZ_NEGATIVE);
        // test game limits
        if (xOut || zOut) {
            throw new MaxWorldCoordinatesException(to,
                    MaxWorldCoordinatesException.Type.GAME,
                    xOut ? x : null,
                    zOut ? z : null
            );
        }
        // test world border
        final World world = to.getWorld();
        if (world != null) {
            if (!world.getWorldBorder().isInside(to)) {
                // calculate details
                final WorldBorder worldBorder = world.getWorldBorder();
                final Location center = worldBorder.getCenter();
                final double halfSize = worldBorder.getSize() / 2d;
                final double max_X = center.getX() + halfSize;
                final double min_X = center.getX() - halfSize;
                final double max_Z = center.getZ() + halfSize;
                final double min_Z = center.getZ() - halfSize;
                throw new MaxWorldCoordinatesException(
                        to,
                        MaxWorldCoordinatesException.Type.WORLD_BORDER,
                        (x > max_X || x < min_X) ? x : null,
                        (z > max_Z || z < min_Z) ? z : null
                );
            }
        }
        this.toLoc = () -> to;
        this.player = null;
    }

    /**
     * Construct a Destination based on a Player.
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
