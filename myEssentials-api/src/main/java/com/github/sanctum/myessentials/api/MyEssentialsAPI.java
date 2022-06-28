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
import com.github.sanctum.labyrinth.data.container.LabyrinthCollection;
import com.github.sanctum.myessentials.model.CommandData;
import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.IExecutorHandler;
import com.github.sanctum.myessentials.model.Messenger;
import com.github.sanctum.myessentials.model.kit.Kit;
import com.github.sanctum.myessentials.model.warp.Warp;
import com.github.sanctum.myessentials.model.warp.WarpHolder;
import com.github.sanctum.myessentials.util.SignEdit;
import com.github.sanctum.myessentials.util.teleportation.TeleportRunner;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MyEssentialsAPI {

    static MyEssentialsAPI getInstance() {
        return Bukkit.getServicesManager().load(MyEssentialsAPI.class);
    }

    Command registerCommand(CommandOutput commandBuilder);

    void unregisterCommand(Command command);

    @Nullable
    Command getRegistration(CommandData commandData);

    IExecutorHandler getExecutorHandler();

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

    SignEdit wrapSign(Block b);

    /**
     * Get the messenger.
     *
     * @return message utility
     */
    Messenger getMessenger();

    Kit.Holder getKitHolder(@NotNull OfflinePlayer player);

    WarpHolder getWarpHolder(@NotNull OfflinePlayer player);

    LabyrinthCollection<Kit.Holder> getKitHolders();

    LabyrinthCollection<WarpHolder> getWarpHolders();

    LabyrinthCollection<Kit> getKits();

    LabyrinthCollection<Warp> getWarps();

    Kit getKit(@NotNull String name);

    Warp getWarp(@NotNull String name);

    void loadKit(@NotNull Kit kit);

    void unloadKit(@NotNull Kit kit);

    void loadWarp(@NotNull Warp warp);

    void unloadWarp(@NotNull Warp warp);

    String getPrefix();

    void logInfo(String toLog);

    void logInfo(Supplier<String> toLog);

    void logSevere(String toLog);

    void logSevere(Supplier<String> toLog);

}
