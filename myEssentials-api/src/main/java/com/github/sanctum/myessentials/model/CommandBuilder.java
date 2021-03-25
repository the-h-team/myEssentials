/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.api.CommandData;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.util.ProvidedMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class CommandBuilder {
    protected final MyEssentialsAPI api = MyEssentialsAPI.getInstance();
    protected final Plugin plugin = JavaPlugin.getProvidingPlugin(getClass());
    protected final Command command;

    public final CommandData commandData;

    public CommandBuilder(CommandData commandData) {
        this.commandData = commandData;
        this.command = api.registerCommand(this);
    }

    public abstract boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args);
    public abstract boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args);

    public CommandData getData() {
        return commandData;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean testPermission(CommandSender sender) {
        return command.testPermission(sender);
    }

    protected void sendMessage(CommandSender sender, ProvidedMessage message) {
        if (!(sender instanceof Player)) {
            JavaPlugin.getProvidingPlugin(getClass()).getLogger().info(message.toString());
        } else {
            new Message((Player) sender, "[&2" + JavaPlugin.getProvidingPlugin(getClass()) + "&r]").send(message.toString());
        }
    }

    protected void sendMessage(CommandSender sender, String text) {
        if (!(sender instanceof Player)) {
            JavaPlugin.getProvidingPlugin(getClass()).getLogger().info(text);
        } else {
            new Message((Player) sender, "[&2" + JavaPlugin.getProvidingPlugin(getClass()) + "&r]").send(text);
        }
    }

    protected String color(String text) {
        return StringUtils.translate(text);
    }
}