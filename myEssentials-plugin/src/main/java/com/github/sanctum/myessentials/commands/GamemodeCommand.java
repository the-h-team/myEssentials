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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.sanctum.myessentials.util.ConfiguredMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GamemodeCommand extends CommandBuilder {
	public GamemodeCommand() {
		super(InternalCommandData.GAMEMODE_COMMAND);
	}

	@Override
	public @Nullable
	List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		if (args.length < 2) {
			// null = returns default completion (with player names)
			return null;
		} else if (args.length > 2) {
			// return no entries
			return Collections.emptyList();
		}
		return Arrays.asList("survival", "creative", "adventure", "spectator");
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length != 2) {
			// message usage
			sendUsage(player);
			return false;
		}
		// testPermission
		if (!testPermission(player)) return true;
		return setGamemode(player, args[1], args[0]);
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length != 2) {
			sendUsage(sender);
			return false;
		}
		return setGamemode(sender, args[1], args[0]);
	}

	private boolean setGamemode(CommandSender sender, String gamemodeName, String playerName) {
		final GameMode gameMode;
		switch (gamemodeName.toLowerCase()) {
			case "survival":
				gameMode = GameMode.SURVIVAL;
				break;
			case "creative":
				gameMode = GameMode.CREATIVE;
				break;
			case "adventure":
				gameMode = GameMode.ADVENTURE;
				break;
			case "spectator":
				gameMode = GameMode.SPECTATOR;
				break;
			default:
				// invalid gamemode
				return false;
		}
		final Player player = Bukkit.getPlayer(playerName);
		if (player == null) {
			// Name isn't a player
			sendMessage(sender, ConfiguredMessage.NOT_VALID_PLAYER);
		} else if (!player.isOnline()) {
			// Player must be online
			sendMessage(sender, ConfiguredMessage.PLAYER_MUST_BE_ONLINE);
		} else {
			// We have a valid player
			// valid player
			player.setGameMode(gameMode);
			sendMessage(sender, ConfiguredMessage.SET_GAMEMODE.replace(playerName, gamemodeName));
		}
		return false;
	}
}
