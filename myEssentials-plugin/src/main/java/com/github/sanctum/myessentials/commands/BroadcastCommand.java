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

import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.InternalCommandData;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class BroadcastCommand extends CommandOutput {
	public BroadcastCommand() {
		super(InternalCommandData.BROADCAST_COMMAND);
	}

	private final SimpleTabCompletion builder = SimpleTabCompletion.empty();

	@Override
	public List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return builder
				.fillArgs(args)
				.then(TabCompletionIndex.ONE, "message")
				.then(TabCompletionIndex.TWO, "message", TabCompletionIndex.ONE, "goes")
				.then(TabCompletionIndex.THREE, "goes", TabCompletionIndex.TWO, "here")
				.get();
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (!testPermission(player)) {
			return false;
		}
		api.getMessenger().broadcastMessage(player, String.join(" ", args));
		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		api.getMessenger().broadcastMessage(String.join(" ", args));
		return true;
	}
}
