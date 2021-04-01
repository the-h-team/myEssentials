/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.api;

import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.util.teleportation.TeleportRunner;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface MyEssentialsAPI {

    static MyEssentialsAPI getInstance() {
        return Bukkit.getServicesManager().load(MyEssentialsAPI.class);
    }

    Command registerCommand(CommandBuilder commandBuilder);

    void unregisterCommand(Command command);

    @Nullable
    Command getRegistration(CommandData commandData);

    /**
     * Get data for all commands registered by MyEssentials.
     *
     * @return set of data for all registered commands
     */
    Set<CommandData> getRegisteredCommands();

    FileList getFileList();

    /**
     * Get the most recent location recorded for this player before they
     * teleported.
     *
     * @param player a player
     * @return last location
     */
    @Nullable
    Location getPreviousLocation(Player player);

    /**
     * Get the last known location of an offline player based on
     * their unique id.
     *
     * @param uuid the uniqueId of a player
     * @return last location
     */
    @Nullable
    Location getPreviousLocationOffline(UUID uuid);

    FileManager getAddonFile(String name, String directory);

    /**
     * Get a variety of utilities relating to the teleportation of players.
     *
     * @return teleport runner
     */
    TeleportRunner getTeleportRunner();

    /**
     * Get the messenger.
     *
     * @return message utility
     */
    Messenger getMessenger();

    String getPrefix();

    void logInfo(String toLog);

    void logInfo(Supplier<String> toLog);

    void logSevere(String toLog);

    void logSevere(Supplier<String> toLog);

}
