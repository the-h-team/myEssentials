/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.library.Applicable;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.util.ProvidedMessage;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CommandBuilder {
    protected final MyEssentialsAPI api = MyEssentialsAPI.getInstance();
    public final Plugin plugin = JavaPlugin.getProvidingPlugin(getClass());
    protected final Command command;

    public final CommandData commandData;

    public CommandBuilder(CommandData commandData) {
        this.commandData = commandData;
        this.command = api.registerCommand(this);
    }

    public CommandBuilder(CommandData commandData, Applicable... pre) {
        this.commandData = commandData;
        for (Applicable p : pre) {
            p.apply();
        }
        this.command = api.registerCommand(this);
    }

    public abstract @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException;

    public abstract boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args);

    public abstract boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args);

    public CommandData getData() {
        return commandData;
    }

    public boolean testPermission(CommandSender sender) {
        return command.testPermission(sender);
    }

    public void sendMessage(CommandSender sender, ProvidedMessage message) {
        if (!(sender instanceof Player)) {
            Message.loggedFor(JavaPlugin.getProvidingPlugin(getClass())).info(message.toString());
        } else {
            Message.form((Player) sender).setPrefix(MyEssentialsAPI.getInstance().getPrefix()).send(message.toString());
        }
    }

    public void sendUsage(CommandSender sender) {
        sendMessage(sender, commandData.getUsage());
    }

    public void sendMessage(CommandSender sender, String text) {
        if (!(sender instanceof Player)) {
            Message.loggedFor(JavaPlugin.getProvidingPlugin(getClass())).info(text);
        } else {
            Message.form((Player) sender).setPrefix(MyEssentialsAPI.getInstance().getPrefix()).send(text);
        }
    }

    public void sendComponent(Player player, BaseComponent component) {
        Message.form(player).build(component);
    }

    public void sendComponent(Player player, BaseComponent... component) {
        Message.form(player).build(component);
    }

    protected String color(String text) {
        return StringUtils.use(text).translate();
    }

    public List<String> defaultCompletion(Player player, String s, String[] strings) {
        return null;
    }
}
