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

import com.github.sanctum.labyrinth.gui.shared.SharedMenu;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InvseeCommand extends CommandBuilder {
	public InvseeCommand() {
		super(InternalCommandData.INVSEE_COMMAND);
	}

	@Override
	public @Nullable
	List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return null;
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {

		if (args.length == 0) {

			return true;
		}

		if (args.length == 1) {

			if (Bukkit.getPlayer(args[0]) != null) {
				Player target = Bukkit.getPlayer(args[0]);

				if (player == target) {
					sendMessage(player, ConfiguredMessage.INVSEE_DENY_SELF);
					return true;
				}

				assert target != null;
				player.openInventory(SharedMenu.open(target));

			} else {
				sendMessage(player, ConfiguredMessage.PLAYER_NOT_FOUND);
				return true;
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
		return true;
	}
}
