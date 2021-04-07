/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials, a derivative work inspired by the
 *  Essentials <http://ess3.net/> and EssentialsX <https://essentialsx.net/>
 *  projects, both licensed under the GPLv3.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TpaCommand extends CommandBuilder {
    private final TextLib textLib = TextLib.getInstance();

    public TpaCommand() {
        super(InternalCommandData.TPA_COMMAND);
    }

    @Override
    public @Nullable
    List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length > 1 || !testPermission(player)) return Collections.emptyList();
        return null;
    }

    @Override
    public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
        return true;
    }

    @Override
    public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(player) || args.length != 1) {
            return false;
        }
        Optional.ofNullable(Bukkit.getPlayerExact(args[0])).ifPresent(target -> {
            api.getTeleportRunner().requestTeleport(player, target);
            sendMessage(player, "&aRequest sent to &e{0}");
            player.spigot().sendMessage(textLib.textRunnable(
                    "To cancel this request, click&7[",
                    "&lhere",
                    "&7]&r or type &7/tpacancel",
                    "Click to cancel",
                    "/tpacancel"));
            sendMessage(target, "&c{0} &6has requested to teleport to you.");
            target.spigot().sendMessage(textLib.textRunnable(
                    "To accept this request, click &7[",
                    "&lhere",
                    "&7]&r or type &7/tpaccept",
                    "Accept",
                    "/tpaccept"));
            target.spigot().sendMessage(textLib.textRunnable(
                    "To reject, click &7[",
                    "&lhere",
                    "&7]&r or type &7/tpreject",
                    "Reject",
                    "/tpreject"));
        });
        return true;
    }
}
