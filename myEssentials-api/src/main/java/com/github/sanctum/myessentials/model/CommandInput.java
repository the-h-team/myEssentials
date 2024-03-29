/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.util.ProvidedMessage;
import com.github.sanctum.panther.util.Applicable;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CommandInput {
	protected final MyEssentialsAPI api = MyEssentialsAPI.getInstance();
	public final Plugin plugin = JavaPlugin.getProvidingPlugin(MyEssentialsAPI.class);
	protected final Command command;

	public final CommandData commandData;

	public CommandInput(CommandData commandData) {
		this.commandData = commandData;
		this.command = api.registerCommand(this);
	}

	public CommandInput(CommandData commandData, Applicable... pre) {
		this.commandData = commandData;
		for (Applicable p : pre) {
			p.run();
		}
		this.command = api.registerCommand(this);
	}

	public abstract @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException;

	public @Nullable List<String> onConsoleTab(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
		return null;
	}

	public abstract boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args);

	public abstract boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args);

	public CommandData getData() {
		return commandData;
	}

	public PermissionCheck getPermCheck() {
		String parent = getData().getPermissionNode();
		return parent != null ? PermissionCheck.of(parent) : null;
	}

	public boolean testPermission(CommandSender sender) {
		return command.testPermission(sender);
	}

	public void sendMessage(CommandSender sender, ProvidedMessage message) {
		if (!(sender instanceof Player)) {
			Mailer.empty(JavaPlugin.getProvidingPlugin(getClass())).info(message.toString()).deploy();
		} else {
			Mailer.empty(sender).chat(MyEssentialsAPI.getInstance().getPrefix() + " " + message.toString()).deploy();
		}
	}

	public void sendUsage(CommandSender sender) {
		sendMessage(sender, commandData.getUsage());
	}

	public void sendMessage(CommandSender sender, String text) {
		if (!(sender instanceof Player)) {
			Mailer.empty(JavaPlugin.getProvidingPlugin(getClass())).info(text).deploy();
		} else {
			Mailer.empty(sender).chat(MyEssentialsAPI.getInstance().getPrefix() + " " + text).deploy();
		}
	}

	public void sendComponent(Player player, BaseComponent component) {
		Mailer.empty(player).chat(component).deploy();
	}

	public void sendComponent(Player player, BaseComponent... component) {
		Mailer.empty(player).chat(component).deploy();
	}

	protected String color(String text) {
		return StringUtils.use(text).translate();
	}

	public List<String> defaultCompletion(Player player, String s, String[] strings) {
		return null;
	}
}
