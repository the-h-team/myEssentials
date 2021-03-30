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

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BanCommand extends CommandBuilder {
	private final Map<String, InetSocketAddress> recentBans = new HashMap<>();

	public BanCommand() {
		super(InternalCommandData.BAN_COMMAND);
	}

	@Override
	public @Nullable
	List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		if (args.length <= 1) {
			// return null = lists player names
			return null;
		} else if (args.length == 2) {
			// suggest "reason"
			return Collections.singletonList("reason");
		}
		// more than two args no suggests
		return Collections.emptyList();
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length < 1) {
			sendUsage(player);
		} else {
			final Player target = Bukkit.getPlayer(args[0]);
			if (target != null) {
				if (target.isBanned()) {
					sendMessage(player, "&cThe target is already banned!");
				} else {
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length < 1) {
			sendUsage(sender);
		} else {
			final Player target = Bukkit.getPlayer(args[0]);
			if (target != null) {
				if (target.isBanned()) {
					sendMessage(sender, "&cThe target is already banned!");
				} else {
				}
				return true;
			}
		}
		return false;
	}
}
