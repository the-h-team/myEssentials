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
import com.github.sanctum.myessentials.util.PlayerWrapper;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import com.github.sanctum.myessentials.util.teleportation.Destination;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
		if (args.length < 2) {
			if (args.length == 1) {
				PlayerSearch search = PlayerSearch.look(args[0]);
				if (search.isValid()) {
					if (search.isOnline()) {
						player.teleport(search.getPlayer());
					}
				}
			}
			return true;
		} else if (args.length == 3) {
			// teleport [x] [y] [z]
			// We are teleporting the player
			final Optional<Location> resolution = resolveLocation(args, 0);
			if (resolution.isPresent()) {
				final Location posLocation = resolution.get();
				final Location playerLocation = player.getLocation();
				posLocation.setWorld(playerLocation.getWorld());
				posLocation.setYaw(playerLocation.getYaw());
				posLocation.setPitch(playerLocation.getPitch());
				sendMessage(player, "Teleporting to " + posLocation);
				api.getTeleportRunner().teleportPlayer(player, new Destination(posLocation));
				return true;
			} else {
				sendMessage(player, "&cInvalid coordinates.");
			}
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
			// %teleport [teleporting] [target]
			// Teleports the first player to the second (target) player
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
			// %teleport [teleporting] [x] [y] [z]
			// Teleport the player to coordinates
			final Optional<Location> resolution = resolveLocation(args, 1);
			if (resolution.isPresent()) {
				final Location posLocation = resolution.get();
				final Location playerLocation = teleportingPlayer.getLocation();
				posLocation.setWorld(playerLocation.getWorld());
				posLocation.setYaw(playerLocation.getYaw());
				posLocation.setPitch(playerLocation.getPitch());
				sendMessage(sender, "Teleporting " +
						teleportingPlayer.getName() +
						" to " +
						posLocation);
				api.getTeleportRunner().teleportPlayer(teleportingPlayer, new Destination(posLocation));
				return true;
			} else {
				sendMessage(sender, "&cInvalid coordinates.");
			}
		}
		sendUsage(sender);
		return false;
	}

	/**
	 * Resolve arguments into a simple (worldless) location.
	 * <p>
	 * If there any errors parsing strings into doubles
	 * this function returns an empty Optional.
	 *
	 * @return an Optional describing a Location without world
	 */
	private Optional<Location> resolveLocation(String[] args, int firstIndex) {
		final double x;
		final double y;
		final double z;
		try {
			x = Double.parseDouble(args[firstIndex]);
			y = Double.parseDouble(args[firstIndex + 1]);
			z = Double.parseDouble(args[firstIndex + 2]);
		} catch (NumberFormatException ignored) {
			return Optional.empty();
		}
		return Optional.of(new Location(null, x, y, z));
	}
}
