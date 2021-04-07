/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.util.teleportation;

import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Describes a request to teleport.
 */
public abstract class TeleportRequest {

    protected final Destination destination;
    protected final Player teleporting;
    protected final Player requester;
    protected final LocalDateTime time = LocalDateTime.now();
    protected final LocalDateTime expiration;
    protected boolean isComplete;
    protected Status status = Status.PENDING;

    protected TeleportRequest(Destination destination, Player requester, Player teleporting, long expirationDelay) {
        this.destination = destination;
        this.requester = requester;
        this.teleporting = teleporting;
        this.expiration = time.plusSeconds(expirationDelay);
    }
    protected TeleportRequest(Destination destination, Player requester, Player teleporting) {
        this(destination, requester, teleporting, 120L);
    }

    /**
     * Get the destination requested.
     *
     * @return destination requested
     */
    public Destination getDestination() {
        return destination;
    }

    /**
     * Get the player that will be teleported.
     *
     * @return player that will be teleported
     */
    public Player getPlayerTeleporting() {
        return teleporting;
    }

    /**
     * Get the player that made this request.
     *
     * @return the player that made this request
     */
    public Player getPlayerRequesting() {
        return requester;
    }

    /**
     * Get the creation time of this request.
     *
     * @return creation time of this request
     */
    public LocalDateTime getCreationTime() {
        return time;
    }

    /**
     * Get the expiration time of this request.
     *
     * @return expiration time of this request
     */
    public LocalDateTime getExpiration() {
        return expiration;
    }

    /**
     * Get the current status of this request.
     *
     * @return the current status of this request
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Accept the teleport request.
     */
    protected abstract void acceptTeleport();

    /**
     * Cancel the teleport request.
     */
    protected abstract void cancelTeleport();

    /**
     * Reject the teleport request.
     */
    protected abstract void rejectTeleport();

    @Override
    public int hashCode() {
        return Objects.hash(destination, teleporting, time, expiration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeleportRequest request = (TeleportRequest) o;
        return isComplete == request.isComplete &&
                destination.equals(request.destination) &&
                teleporting.equals(request.teleporting) &&
                time.equals(request.time) &&
                expiration.equals(request.expiration) &&
                status == request.status;
    }

    /**
     * Describes a request state for teleport requests.
     * <p>
     * Does not indicate the success/fail of the subsequent teleport,
     * only reflects the intents of the request.
     */
    public enum Status {
        /**
         * The request has been created but not yet accepted.
         */
        PENDING,
        /**
         * The request was rejected.
         */
        REJECTED,
        /**
         * The request was accepted.
         */
        ACCEPTED,
        /**
         * The request was withdrawn by the requester.
         */
        CANCELLED
    }
}
