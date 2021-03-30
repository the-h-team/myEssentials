package com.github.sanctum.myessentials.api;

import org.bukkit.command.CommandSender;

/**
 * Provides messaging functions.
 */
public interface Messenger {
    /**
     * Broadcast a message to everyone on the server.
     *
     * @param message the message of the broadcast
     */
    void broadcastMessage(String message);

    /**
     * Broadcast a message to everyone on the server.
     * <p>
     * Requires the sender to have appropriate permissions.
     *
     * @param sender the sender of the broadcast
     * @param message the message of the broadcast
     * @return true unless sender/broadcast is disallowed
     */
    boolean broadcastMessage(CommandSender sender, String message);

    /**
     * Broadcast a message to everyone on the server.
     * <p>
     * Sends plugin prefix.
     * <p>
     * Requires the sender to have appropriate permissions.
     *
     * @param sender the sender of the broadcast
     * @param message the message of the broadcast
     * @return true unless sender/broadcast is disallowed
     */
    boolean broadcastMessagePrefixed(CommandSender sender, String message);
}
