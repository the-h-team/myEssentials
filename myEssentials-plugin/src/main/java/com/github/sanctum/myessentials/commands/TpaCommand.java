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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.github.sanctum.myessentials.util.teleportation.ExistingTeleportRequestException;
import com.github.sanctum.myessentials.util.teleportation.TeleportRequest;
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
        if (!testPermission(player)) {
            return false;
        }
        if (args.length != 1) {
            sendUsage(player);
            return false;
        }
        Optional.ofNullable(Bukkit.getPlayerExact(args[0])).ifPresent(target -> {
            try {
                api.getTeleportRunner().requestTeleport(player, target, TeleportRequest.Type.NORMAL_TELEPORT);
                // Created new request, send messages
                sendMessage(player, ConfiguredMessage.TPA_SENT.replace(target.getDisplayName()));
                player.spigot().sendMessage(textLib.textRunnable(
                        ConfiguredMessage.TPA_TO_CANCEL_TEXT.toString(),
                        ConfiguredMessage.TPA_TO_CANCEL_BUTTON.toString(),
                        ConfiguredMessage.TPA_TO_CANCEL_TEXT2.replace(InternalCommandData.TPA_CANCEL_COMMAND.getLabel()),
                        ConfiguredMessage.TPA_TO_CANCEL_HOVER.toString(),
                        InternalCommandData.TPA_CANCEL_COMMAND.getLabel()));
                sendMessage(target, ConfiguredMessage.TPA_REQUEST_TO_YOU.replace(player.getDisplayName()));
                target.spigot().sendMessage(textLib.textRunnable(
                        ConfiguredMessage.TPA_TO_ACCEPT_TEXT.toString(),
                        ConfiguredMessage.TPA_TO_ACCEPT_BUTTON.toString(),
                        ConfiguredMessage.TPA_TO_ACCEPT_TEXT2.replace(InternalCommandData.TP_ACCEPT_COMMAND.getLabel()),
                        ConfiguredMessage.TPA_TO_ACCEPT_HOVER.toString(),
                        InternalCommandData.TP_ACCEPT_COMMAND.getLabel()));
                target.spigot().sendMessage(textLib.textRunnable(
                        ConfiguredMessage.TPA_TO_REJECT_TEXT.toString(),
                        ConfiguredMessage.TPA_TO_REJECT_BUTTON.toString(),
                        ConfiguredMessage.TPA_TO_REJECT_TEXT2.replace(InternalCommandData.TP_REJECT_COMMAND.getLabel()),
                        ConfiguredMessage.TPA_TO_REJECT_HOVER.toString(),
                        InternalCommandData.TP_REJECT_COMMAND.getLabel()));
            } catch (ExistingTeleportRequestException e) {
                // Existing request found, report to user
                final LocalDateTime expiration = e.getExistingRequest().getExpiration();
                sendMessage(target, "&cYou already have an existing request&e which will expire at &f" +
                        expiration.format(DateTimeFormatter.ofPattern("h:mma")) +
                        " &7(in&f " +
                        (expiration.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) +
                        " &7seconds).");
            }
        });
        return true;
    }
}
