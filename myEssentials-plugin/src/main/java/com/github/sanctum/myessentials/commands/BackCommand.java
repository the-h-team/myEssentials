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

import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class BackCommand extends CommandOutput {
	public BackCommand() {
		super(InternalCommandData.BACK_COMMAND);
	}

	@Override
	public List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return Collections.emptyList();
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {
			if (!testPermission(player)) {
				return true;
			}
			Location previous = api.getPreviousLocation(player);
			if (previous == null) {
				sendMessage(player, ConfiguredMessage.NO_PREVIOUS_LOCATION);
				return true;
			}
			player.teleport(previous);
			sendMessage(player, ConfiguredMessage.TELEPORTED_PREVIOUS);
			return true;
		}

		return false;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
		return false;
	}
}
