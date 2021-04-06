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

import com.github.sanctum.labyrinth.formatting.TabCompletion;
import com.github.sanctum.labyrinth.formatting.TabCompletionBuilder;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.github.sanctum.myessentials.util.ConfiguredMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class DayCommand extends CommandBuilder {
	private final TabCompletionBuilder builder = TabCompletion.build(getData().getLabel());
	private final Random r = new Random();

	public DayCommand() {
		super(InternalCommandData.DAY_COMMAND);
	}

	@Override
	public @NotNull
	List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return builder
				.forArgs(args)
				.level(1)
				.completeAt(getData().getLabel())
				.filter(() -> Arrays.asList("morning", "noon", "afternoon"))
				.map("morning", () -> {
					if (r.nextBoolean()) {
						if (r.nextInt(28) < 6) {
							sendMessage(player, ConfiguredMessage.DAY_VALUES_DESC);
						}
					}
				})
				.collect()
				.get(1);
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {

		if (args.length == 0) {
			if (testPermission(player)) {
				player.getWorld().setTime(0);
				sendMessage(player, ConfiguredMessage.SET_DAY);
				return true;
			}
			return true;
		}
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("morning")) {
				if (testPermission(player)) {
					player.getWorld().setTime(0);
					sendMessage(player, ConfiguredMessage.SET_MORNING);
					return true;
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("noon")) {
				if (testPermission(player)) {
					player.getWorld().setTime(6000);
					sendMessage(player, ConfiguredMessage.SET_NOON);
					return true;
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("afternoon")) {
				if (testPermission(player)) {
					player.getWorld().setTime(9500);
					sendMessage(player, ConfiguredMessage.SET_AFTERNOON);
					return true;
				}
				return true;
			}
			sendUsage(player);
			return true;
		}

		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}
}
