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
import java.util.Collections;
import java.util.List;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class BroadcastCommand extends CommandBuilder {
	public BroadcastCommand() {
		super(InternalCommandData.BROADCAST_COMMAND);
	}

	private final TabCompletionBuilder builder = TabCompletion.build(getData().getLabel());

	@Override
	public List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {


		if (args.length == 2) {
			return builder
					.forArgs(args)
					.level(2)
					.completeAt(getData().getLabel())
					.filter(() -> Collections.singletonList("goes"))
					.map("goes", () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 3))
					.collect()
					.get(2);
		}

		if (args.length == 3) {
			return builder
					.forArgs(args)
					.level(3)
					.completeAt(getData().getLabel())
					.filter(() -> Collections.singletonList("here"))
					.map("here", () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 3))
					.collect()
					.get(3);
		}

		return builder
				.forArgs(args)
				.level(1)
				.completeAt(getData().getLabel())
				.filter(() -> Collections.singletonList("message"))
				.map("message", () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 3))
				.collect()
				.get(1);
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (!testPermission(player)) {
			return false;
		}
		api.getMessenger().broadcastMessage(player, String.join(" ", args));
		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		api.getMessenger().broadcastMessage(String.join(" ", args));
		return true;
	}
}
