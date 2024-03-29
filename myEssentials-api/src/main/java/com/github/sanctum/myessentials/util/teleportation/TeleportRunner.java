/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.util.teleportation;

import java.util.Set;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Performs teleport operations and provides information.
 */
public interface TeleportRunner {
    /**
     * Attempt to teleport a player to a given Destination.
     * <p>
     * The plugin will perform the necessary sanity-checks before
     * the actual teleportation is performed.
     *
     * @param player a player to be teleported
     * @param destination a destination for the player
     */
    void teleportPlayer(@NotNull Player player, Destination destination);

    /**
     * Request teleportation from one to another player.
     *
     * @param requester the player requesting
     * @param requested the player requested
     * @param type the request type
     * @throws ExistingTeleportRequestException if a similar pending request
     * already exists
     */
    void requestTeleport(@NotNull Player requester, @NotNull Player requested, TeleportRequest.Type type) throws ExistingTeleportRequestException;

    /**
     * Request teleportation from one to another player, specifying the
     * time after which the request will expire.
     *
     * @param requester the player requesting
     * @param requested the player requested
     * @param type the request type
     * @param expiration an expiration in seconds
     * @throws ExistingTeleportRequestException if a similar pending request
     * already exists
     */
    void requestTeleportCustom(@NotNull Player requester, @NotNull Player requested, TeleportRequest.Type type, long expiration) throws ExistingTeleportRequestException;

    /**
     * Accept a teleport request.
     *
     * @param request request to accept
     */
    void acceptTeleport(TeleportRequest request);

    /**
     * Cancel a teleport request.
     *
     * @param request request to cancel
     */
    void cancelRequest(TeleportRequest request);

    /**
     * Reject a teleport request.
     *
     * @param request request to reject
     */
    void rejectTeleport(TeleportRequest request);

    /**
     * Query the execution status of the request.
     * <p>
     * Returns true only if the request was accepted and successfully
     * complete. Requests can be accepted and fail to complete, so be
     * sure to check {@link TeleportRequest#getStatus()} if your code
     * needs to work with that data.
     *
     * @param request request to check
     * @return true only if the request was accepted and completed
     */
    boolean queryTeleportStatus(TeleportRequest request);

    /**
     * Get all active teleport requests.
     *
     * @return all active teleport requests
     */
    @NotNull Set<TeleportRequest> getActiveRequests();

    /**
     * Get all expired teleport requests.
     *
     * @return all expired teleport requests
     */
    @NotNull Set<TeleportRequest> getExpiredRequests();

}
