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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * We encapsulate the actual Command class extension here because
 * otherwise CommandBuilder would also need to follow LGPL.
 */
public final class CommandImpl extends Command {
    protected final CommandOutput commandBuilder;

    public CommandImpl(CommandOutput commandBuilder) {
        super(commandBuilder.commandData.getLabel());
        this.commandBuilder = commandBuilder;
        setDescription(this.commandBuilder.commandData.getDescription());
        setPermission(this.commandBuilder.commandData.getPermissionNode());
        setPermissionMessage(commandBuilder.color("&cYou don't have permission: &f'<permission>'"));
        final List<String> aliases = this.commandBuilder.commandData.getAliases();
        if (aliases.isEmpty()) return;
        setAliases(aliases);
        setUsage(commandBuilder.commandData.getUsage());
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        final List<String> completions;
        if (sender instanceof Player) {
            completions = commandBuilder.onPlayerTab((Player) sender, alias, args);
        } else {
            completions = commandBuilder.onConsoleTab(sender, alias, args);
        }
        if (completions != null) {
            return completions;
        }
        return super.tabComplete(sender, alias, args);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return commandBuilder.onConsole(sender, commandLabel, args);
        }
        return commandBuilder.onPlayer((Player) sender, commandLabel, args);
    }
}
