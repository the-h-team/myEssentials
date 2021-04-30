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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.sanctum.myessentials.util.PlayerWrapper;
import com.github.sanctum.myessentials.util.teleportation.Destination;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TeleportCommand extends CommandBuilder {
	private final PlayerWrapper playerWrapper = new PlayerWrapper();

	public TeleportCommand() {
		super(InternalCommandData.TELEPORT_COMMAND);
	}

	@Override
	public List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		if (!command.testPermissionSilent(player)) return Collections.emptyList();
		if (args.length <= 1) {
			return playerWrapper.collect()
					.stream()
					.map(HumanEntity::getName)
					.collect(Collectors.toList());
		}
		final Location location = player.getLocation();
		if (args.length == 2) {
			final LinkedList<String> linkedList = new LinkedList<>();
			playerWrapper.collect()
					.stream()
					.map(HumanEntity::getName)
					.forEach(linkedList::add);
			linkedList.add(String.valueOf(location.getBlockX()));
			return linkedList;
		} else {
			try {
				final int i = Integer.parseInt(args[1]);
				if (i == location.getBlockX()) {
					if (args.length == 3) {
						return Collections.singletonList(String.valueOf(location.getBlockY()));
					} else if (args.length == 4) {
						return Collections.singletonList(String.valueOf(location.getBlockZ()));
					}
				}
			} catch (NumberFormatException ignored) {
			}
		}
		return Collections.emptyList();
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (!testPermission(player)) {
			return false;
		}
		return consoleView(player, commandLabel, args);
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length < 2) {
			sendUsage(sender);
			return false;
		}
		final Optional<Player> teleporting = playerWrapper.get(args[0]);
		if (!teleporting.isPresent()) {
			sendMessage(sender, args[0] + " is not a valid player.");
			return false;
		}
		final Player teleportingPlayer = teleporting.get();
		if (args.length == 2) {
			final Optional<Player> target = playerWrapper.get(args[1]);
			if (!target.isPresent()) {
				sendMessage(sender, args[1] + " is not a valid player name.");
				return false;
			}
			sendMessage(sender, "Teleporting " +
					teleportingPlayer.getName() +
					" to " +
					target.get().getName());
			api.getTeleportRunner().teleportPlayer(teleportingPlayer, new Destination(target.get()));
			return true;
		} else if (args.length == 4) {
			try {
				final double x = Double.parseDouble(args[1]);
				final double y = Double.parseDouble(args[2]);
				final double z = Double.parseDouble(args[3]);
				final Location location = new Location(teleportingPlayer.getWorld(), x, y, z,
						teleportingPlayer.getLocation().getYaw(),
						teleportingPlayer.getLocation().getPitch());
				sendMessage(sender, "Teleporting " +
						teleportingPlayer.getName() +
						" to " +
						location);
				api.getTeleportRunner().teleportPlayer(teleportingPlayer, new Destination(location));
				return true;
			} catch (NumberFormatException ignored) {
				sendMessage(sender, "&cInvalid coordinates.");
			}
		}
		sendUsage(sender);
		return false;
	}
}
