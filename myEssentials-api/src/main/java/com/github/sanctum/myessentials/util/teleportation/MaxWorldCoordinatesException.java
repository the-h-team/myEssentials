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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Thrown in the event that the provided Location encapsulates
 * an invalid or out-of-bounds value for X or Z.
 *
 * @since 1.0.0
 */
public final class MaxWorldCoordinatesException extends Exception {
    private static final long serialVersionUID = 2157754117490228022L;
    public enum Type {
        /**
         * The exception was caused by limitations of the base game.
         */
        GAME,
        /**
         * The exception was caused by a world border.
         */
        WORLD_BORDER
    }

    private final Location location;
    private final Type type;
    private final Double x;
    private final Double z;
    private final Double y;

    MaxWorldCoordinatesException(@NotNull Location location, Type type, @Nullable Double x, @Nullable Double z, @Nullable Double y) {
        this.location = location.clone();
        this.type = type;
        this.x = x;
        this.z = z;
        this.y = y;
    }

    /**
     * Get the location that caused this exception.
     *
     * @return a copy of the {@link Location} that caused this exception
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Get information about which facility led to this exception.
     *
     * @return information about which facility led to this exception
     */
    public Type getType() {
        return type;
    }

    /**
     * Get the X coordinate, if it was invalid.
     *
     * @return an Optional describing an invalid X coordinate if present
     */
    public Optional<Double> getErrantX() {
        return Optional.ofNullable(x);
    }

    /**
     * Get the Z coordinate, if it was invalid.
     *
     * @return an Optional describing an invalid Z coordinate if present
     */
    public Optional<Double> getErrantZ() {
        return Optional.ofNullable(z);
    }

    /**
     * Get the Y coordinate, if it was invalid.
     *
     * @return an Optional describing an invalid Y coordinate if present
     */
    public Optional<Double> getErrantY() {
        return Optional.ofNullable(y);
    }
}
