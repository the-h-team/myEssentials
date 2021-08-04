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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public final class TeleportCommand extends CommandBuilder {
	private final PlayerWrapper playerWrapper = new PlayerWrapper();

	public TeleportCommand() {
		super(InternalCommandData.TELEPORT_COMMAND);
	}

	@Override
	public List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		if (!command.testPermissionSilent(player)) return ImmutableList.of();
		final Collection<Player> players = playerWrapper.collect();
		final ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
		// first arg - show all players and player's x
		if (args.length <= 1) {
			players.stream().map(Player::getName).forEach(builder::add);
			builder.add(String.valueOf(player.getLocation().getBlockX()));
			builder.add("~1");
		} else if (args.length == 2) {
			// second arg - if first is a player, show filtered players + first player's x (fallback to current player if invalid);
			// otherwise player's own y (if first arg parses)
			final Optional<Player> namedPlayer = playerWrapper.get(args[0]);
			if (namedPlayer.isPresent()) {
				final String named = namedPlayer.get().getName();
				players.stream()
						.map(Player::getName)
						.filter(name -> !name.equals(named))
						.forEach(builder::add);
				final int blockX = namedPlayer
						.filter(Player::isValid)
						.map(Player::getLocation)
						.map(Location::getBlockX)
						.orElseGet(() -> player.getLocation().getBlockX());
				builder.add(String.valueOf(blockX));
			} else {
				try {
					Double.parseDouble(args[0]);
				} catch (NumberFormatException ignored) {
					return ImmutableList.of();
				}
				builder.add(String.valueOf(player.getLocation().getBlockY()));
				builder.add("~1");
			}
		} else if (args.length == 3) {
			// third arg - if first+second arg is a player, nothing;
			// first arg player + second arg double, send arg player's y (fallback to current player)
			// first+second arg double, send player's own z (if first+second args parse)
			final Optional<Player> firstNamedPlayer = playerWrapper.get(args[0]);
			final Optional<Player> secondNamedPlayer = playerWrapper.get(args[1]);
			if (firstNamedPlayer.isPresent() && secondNamedPlayer.isPresent()) {
				return ImmutableList.of();
			}
			if (firstNamedPlayer.isPresent()) {
				try {
					Double.parseDouble(args[1]);
				} catch (NumberFormatException ignored) {
					return ImmutableList.of();
				}
				final int blockY = firstNamedPlayer
						.filter(Player::isValid)
						.map(Player::getLocation)
						.map(Location::getBlockY)
						.orElseGet(() -> player.getLocation().getBlockY());
				builder.add(String.valueOf(blockY));
			} else {
				try {
					Double.parseDouble(args[0]);
					Double.parseDouble(args[1]);
				} catch (NumberFormatException ignored) {
					return ImmutableList.of();
				}
				builder.add(String.valueOf(player.getLocation().getBlockZ()));
				builder.add("~1");
			}
		} else if (args.length == 4) {
			// fourth arg - format MUST be player x y z; if not nothing.
			// return arg player's z (fallback to current player);
			final Optional<Player> namedPlayer = playerWrapper.get(args[0]);
			try {
				Double.parseDouble(args[1]);
				Double.parseDouble(args[2]);
			} catch (NumberFormatException ignored) {
				return ImmutableList.of();
			}
			final int blockZ = namedPlayer
					.filter(Player::isValid)
					.map(Player::getLocation)
					.map(Location::getBlockZ)
					.orElseGet(() -> player.getLocation().getBlockZ());
			builder.add(String.valueOf(blockZ));
		}
		if (args.length == 0) return builder.build();
		// filter completions
		return StringUtil.copyPartialMatches(args[args.length - 1], builder.build(), new LinkedList<>());
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
			final Optional<Location> resolution = resolveRelativeLocation(player, args, 0);
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
	 * @param args command input arguments
	 * @param firstIndex the index at which to begin parsing
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

	/**
	 * Resolve arguments into a relative location.
	 * <p>
	 * If relative location syntax is present, captures player's location
	 * and calculates accordingly.
	 * <p>
	 * Delegates to {@link #resolveLocation}.
	 *
	 * @param context the player whose location to capture
	 * @param args command input arguments
	 * @param firstIndex the index at which to begin parsing
	 * @return an Optional describing a potentially relative Location
	 */
	@SuppressWarnings("SameParameterValue")
	private Optional<Location> resolveRelativeLocation(Player context, String[] args, int firstIndex) {
		boolean hasRelative = false;
		for (int i = 0; i < 3; ++i) {
			if (args[i].startsWith("~")) {
				hasRelative = true;
				break;
			}
		}
		if (!hasRelative) return resolveLocation(args, firstIndex);
		final Location playerLoc = context.getLocation();
		final String xArg = args[firstIndex];
		final String yArg = args[firstIndex + 1];
		final String zArg = args[firstIndex + 2];
		try {
			if (xArg.startsWith("~")) {
				playerLoc.add(Double.parseDouble(xArg.replaceAll("~", "")), 0d, 0d);
			} else {
				playerLoc.setX(Double.parseDouble(xArg));
			}
			if (yArg.startsWith("~")) {
				playerLoc.add(0d, Double.parseDouble(yArg.replaceAll("~", "")), 0d);
			} else {
				playerLoc.setY(Double.parseDouble(yArg));
			}
			if (zArg.startsWith("~")) {
				playerLoc.add(0d, 0d, Double.parseDouble(zArg.replaceAll("~", "")));
			} else {
				playerLoc.setZ(Double.parseDouble(zArg));
			}
		} catch (NumberFormatException ignored) {
			return Optional.empty();
		}
		return Optional.of(playerLoc);
	}
}
