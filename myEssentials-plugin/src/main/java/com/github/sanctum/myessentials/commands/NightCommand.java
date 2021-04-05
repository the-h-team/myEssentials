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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class NightCommand extends CommandBuilder {
	private final TabCompletionBuilder builder = TabCompletion.build(getData().getLabel());
	private final Random r = new Random();

	public NightCommand() {
		super(InternalCommandData.NIGHT_COMMAND);
	}

	@Override
	public @NotNull
	List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return builder
				.forArgs(args)
				.level(1)
				.completeAt(getData().getLabel())
				.filter(() -> Arrays.asList("night", "midnight", "dusk"))
				.map("night", () -> {
					if (r.nextBoolean()) {
						if (r.nextInt(28) < 6) {
							sendMessage(player, "&e&oEach value is a different time of night.");
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
				sendMessage(player, "&aIt is now day time.");
				return true;
			}
			return true;
		}
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("night")) {
				if (testPermission(player)) {
					player.getWorld().setTime(13000);
					sendMessage(player, "&aIt is now night time.");
					return true;
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("midnight")) {
				if (testPermission(player)) {
					player.getWorld().setTime(18000);
					sendMessage(player, "&aIt is now mid-night time.");
					return true;
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("dusk")) {
				if (testPermission(player)) {
					player.getWorld().setTime(22000);
					sendMessage(player, "&aIt is now dusk time.");
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
