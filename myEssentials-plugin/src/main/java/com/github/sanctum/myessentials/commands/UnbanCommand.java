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
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class UnbanCommand extends CommandBuilder {
	private final TabCompletionBuilder builder = TabCompletion.build(getData().getLabel());

	public UnbanCommand() {
		super(InternalCommandData.UNBAN_COMMAND);
	}

	@Override
	public @NotNull
	List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return builder.forArgs(args)
				.level(1)
				.completeAt(getData().getLabel())
				.filter(() -> Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()))
				.collect().get(1);
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {

		if (args.length == 1) {
			PlayerSearch search = PlayerSearch.look(args[0]);
			if (search.isValid()) {

				OfflinePlayer target = search.getOfflinePlayer();

				if (search.unban()) {
					sendMessage(player, "Target unbanned");
				} else {
					sendMessage(player, "Target is already not banned.");
				}

			} else {
				if (testPermission(player)) {
					sendMessage(player, "&c&oTarget " + args[0] + " was not found.");
					return true;
				}
				return true;
			}
			return true;
		}

		return false;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {

		if (args.length == 1) {
			PlayerSearch search = PlayerSearch.look(args[0]);
			if (search.isValid()) {

				OfflinePlayer target = search.getOfflinePlayer();

				if (search.unban()) {
					sendMessage(sender, "Target unbanned");
				} else {
					sendMessage(sender, "Target is already not banned.");
				}

			} else {
				if (testPermission(sender)) {
					sendMessage(sender, "&c&oTarget " + args[0] + " was not found.");
					return true;
				}
				return true;
			}
			return true;
		}
		return false;
	}
}
