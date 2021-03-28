/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen. However, this class specifically extends a
 *  component of Bukkit API, and thus its license must be LGPL-compatible.
 */
package com.github.sanctum.myessentials.model;

import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * We encapsulate the actual Command class extension here because
 * otherwise CommandBuilder would also need to follow LGPL.
 */
public final class CommandImpl extends Command {
    protected final CommandBuilder commandBuilder;

    public CommandImpl(CommandBuilder commandBuilder) {
        super(commandBuilder.commandData.getLabel());
        this.commandBuilder = commandBuilder;
        setDescription(this.commandBuilder.commandData.getDescription());
        setPermission(this.commandBuilder.commandData.getPermissionNode());
        setPermissionMessage(ChatColor.RED + "You do not have permission to perform this command!");
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        List<String> completions = commandBuilder.tabComplete((Player) sender, alias, args);
        if (completions != null) {
            return completions;
        }
        return commandBuilder.defaultCompletion(sender, alias, args);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return commandBuilder.consoleView(sender, commandLabel, args);
        }
        return commandBuilder.playerView((Player) sender, commandLabel, args);
    }
}
