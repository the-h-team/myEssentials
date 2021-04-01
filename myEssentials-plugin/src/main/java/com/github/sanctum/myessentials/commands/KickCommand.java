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
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class KickCommand extends CommandBuilder {
	public KickCommand() {
		super(InternalCommandData.KICK_COMMAND);
	}

	private final TabCompletionBuilder builder = TabCompletion.build(getData().getLabel());

	@Override
	public @NotNull
	List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return builder.forArgs(args)
				.level(1)
				.completeAt(getData().getLabel())
				.filter(() -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()))
				.collect()
				.get(1);
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {

			return true;
		}

		if (args.length == 1) {
			PlayerSearch search = PlayerSearch.look(args[0]);
			if (testPermission(player)) {
				if (search.isValid()) {

					OfflinePlayer target = search.getOfflinePlayer();

					if (search.kick()) {
						sendMessage(player, "Target kicked");
					} else {
						sendMessage(player, "Target is offline.");
					}

				} else {
					sendMessage(player, "&c&oTarget " + args[0] + " was not found.");
					return true;
				}
				return true;
			}
			return true;
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			builder.append(args[i]).append(" ");
		}
		if (testPermission(player)) {
			String get = builder.toString().trim();

			PlayerSearch search = PlayerSearch.look(args[0]);
			if (search.isValid()) {

				OfflinePlayer target = search.getOfflinePlayer();

				if (search.kick(get)) {
					sendMessage(player, "Target kicked for '" + get + "'");
				} else {
					sendMessage(player, "Target is offline.");
				}

			} else {
				sendMessage(player, "&c&oTarget " + args[0] + " was not found.");
				return true;
			}
			return true;
		}

		return false;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}
}
