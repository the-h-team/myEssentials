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
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import com.github.sanctum.myessentials.util.teleportation.Destination;
import com.github.sanctum.myessentials.util.teleportation.MaxWorldCoordinatesException;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public final class TeleportCommand extends CommandOutput {

	public TeleportCommand() {
		super(InternalCommandData.TELEPORT_COMMAND);
	}

	@Override
	public List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		if (!command.testPermissionSilent(player)) return ImmutableList.of();
		final Collection<Player> players = PlayerSearch.getOnlinePlayers().collect();
		final ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
		// first arg - show all players and player's x
		if (args.length <= 1) {
			players.stream().map(Player::getName).forEach(builder::add);
			builder.add("~");
			builder.add(String.valueOf(player.getLocation().getBlockX()));
		} else if (args.length == 2) {
			// second arg - if first is a player, show filtered players + first player's x (fallback to current player if invalid);
			// otherwise player's own y (if first arg parses)
			final Optional<Player> namedPlayer = PlayerSearch.getOnlinePlayers().get(args[0]);
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
					if (!args[0].startsWith("~")) {
						Double.parseDouble(args[0]);
					}
				} catch (NumberFormatException ignored) {
					return ImmutableList.of();
				}
				builder.add("~");
				builder.add(String.valueOf(player.getLocation().getBlockY()));
			}
		} else if (args.length == 3) {
			// third arg - if first+second arg is a player, nothing;
			// first arg player + second arg double, send arg player's y (fallback to current player)
			// first+second arg double, send player's own z (if first+second args parse)
			final Optional<Player> firstNamedPlayer = PlayerSearch.getOnlinePlayers().get(args[0]);
			final Optional<Player> secondNamedPlayer = PlayerSearch.getOnlinePlayers().get(args[1]);
			if (firstNamedPlayer.isPresent() && secondNamedPlayer.isPresent()) {
				return ImmutableList.of();
			}
			if (firstNamedPlayer.isPresent()) {
				try {
					if (!args[1].startsWith("~")) {
						Double.parseDouble(args[1]);
					}
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
					if (!args[0].startsWith("~")) {
						Double.parseDouble(args[0]);
					}
					if (!args[1].startsWith("~")) {
						Double.parseDouble(args[1]);
					}
				} catch (NumberFormatException ignored) {
					return ImmutableList.of();
				}
				builder.add("~");
				builder.add(String.valueOf(player.getLocation().getBlockZ()));
			}
		} else if (args.length == 4) {
			// fourth arg - format MUST be player x y z; if not nothing.
			// return arg player's z (fallback to current player);
			final Optional<Player> namedPlayer = PlayerSearch.getOnlinePlayers().get(args[0]);
			if (!namedPlayer.isPresent()) return ImmutableList.of();
			try {
				if (!args[1].startsWith("~")) {
					Double.parseDouble(args[1]);
				}
				if (!args[2].startsWith("~")) {
					Double.parseDouble(args[2]);
				}
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
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (!testPermission(player)) {
			return false;
		}
		if (args.length < 2) {
			if (args.length == 1) {
				PlayerSearch search = PlayerSearch.look(args[0]);
				if (search.isValid()) {
					if (search.isOnline()) {
						// TODO: Refactor PlayerSearch with inner class to override nullity
						api.getTeleportRunner().teleportPlayer(player, new Destination(search.getPlayer()));
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
				final Optional<Destination> checked = sanityCheckedDestination(player, posLocation);
				if (!checked.isPresent()) return true;
				sendMessage(player, "Teleporting to " + formatPosition(args, 0, posLocation));
				api.getTeleportRunner().teleportPlayer(player, checked.get());
				return true;
			} else {
				sendMessage(player, "&cInvalid coordinates.");
			}
		}
		return onConsole(player, commandLabel, args);
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length < 2) {
			sendUsage(sender);
			return false;
		}
		final Optional<Player> teleporting = PlayerSearch.getOnlinePlayers().get(args[0]);
		if (!teleporting.isPresent()) {
			sendMessage(sender, args[0] + " is not a valid player.");
			return false;
		}
		final Player teleportingPlayer = teleporting.get();
		if (args.length == 2) {
			// %teleport [teleporting] [target]
			// Teleports the first player to the second (target) player
			final Optional<Player> target = PlayerSearch.getOnlinePlayers().get(args[1]);
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
				final Optional<Destination> checked = sanityCheckedDestination(sender, posLocation);
				if (!checked.isPresent()) return true;
				sendMessage(sender, "Teleporting " +
						teleportingPlayer.getName() +
						" to " +
						formatPosition(args, 1, posLocation));
				api.getTeleportRunner().teleportPlayer(teleportingPlayer, checked.get());
				return true;
			} else {
				sendMessage(sender, "&cInvalid coordinates.");
			}
		}
		sendUsage(sender);
		return true;
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
			x = wholeToHalf(Double.parseDouble(args[firstIndex]));
			y = Double.parseDouble(args[firstIndex + 1]);
			z = wholeToHalf(Double.parseDouble(args[firstIndex + 2]));
		} catch (NumberFormatException ignored) {
			return Optional.empty();
		}
		return Optional.of(new Location(null, x, y, z));
	}

	/**
	 * Nudge integer coordinates to block centers.
	 *
	 * @param rawValue original double
	 * @return double with whole values nudged +0.5
	 */
	private double wholeToHalf(double rawValue) {
		if (rawValue % 1.0d == 0d) {
			return rawValue + 0.5d;
		}
		return rawValue;
	}

	private String formatPosition(String[] args, int argOffset, Location posLocation) {
		final int y = argOffset + 1;
		final int z = argOffset + 2;
		return (!(args[argOffset].contains(".") || args[y].contains(".") || args[z].contains(".")) ?
				"block &7" + args[argOffset] + "," + args[y] + "," + args[z] :
				"&e" + prettyPrintCoordinates(posLocation)
		);
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
		for (int i = firstIndex; i < firstIndex + 3; ++i) {
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
				final String num = xArg.substring(1);
				if (!num.isEmpty()) playerLoc.add(Double.parseDouble(num), 0d, 0d);
			} else {
				playerLoc.setX(wholeToHalf(Double.parseDouble(xArg)));
			}
			if (yArg.startsWith("~")) {
				final String num = yArg.substring(1);
				if (!num.isEmpty()) playerLoc.add(0d, Double.parseDouble(num), 0d);
			} else {
				playerLoc.setY(Double.parseDouble(yArg));
			}
			if (zArg.startsWith("~")) {
				final String num = zArg.substring(1);
				if (!num.isEmpty()) playerLoc.add(0d, 0d, Double.parseDouble(num));
			} else {
				playerLoc.setZ(wholeToHalf(Double.parseDouble(zArg)));
			}
		} catch (NumberFormatException ignored) {
			return Optional.empty();
		}
		return Optional.of(playerLoc);
	}

	/**
	 * Sanity-check the provided Location, ensuring that it is safely
	 * converted into a Location-based Destination; report back to
	 * a sender if this was not possible.
	 *
	 * @param sender sender to reply to
	 * @param posLocation location to be checked
	 * @return an Optional describing a valid location, if possible
	 */
	private Optional<Destination> sanityCheckedDestination(CommandSender sender, Location posLocation) {
		try {
			return Optional.of(new Destination(posLocation));
		} catch (MaxWorldCoordinatesException e) {
			switch (e.getType()) {
				case GAME:
				case WORLD_BORDER:
					final StringBuilder sb;
					if ((e.getErrantX().isPresent() && e.getErrantZ().isPresent()) || (e.getErrantY().isPresent() && (e.getErrantX().isPresent() || e.getErrantZ().isPresent()))) {
						sb = new StringBuilder("&cInvalid coordinates!");
					} else {
						sb = new StringBuilder("&cInvalid coordinate!");
					}
					e.getErrantX().ifPresent(x -> sb.append(" X:").append(x));
					e.getErrantY().ifPresent(y -> sb.append(" Y:").append(y));
					e.getErrantZ().ifPresent(z -> sb.append(" Z:").append(z));
					sendMessage(sender, sb.toString());
					if (e.getType() == MaxWorldCoordinatesException.Type.WORLD_BORDER) {
						sendMessage(sender, "&8&oThe specified location does not exist within the world border.");
					}
			}
			return Optional.empty();
		}
	}

	private String prettyPrintCoordinates(Location location) {
		if (location == null) return "null";
		return String.format("%.3f, %.5f, %.3f", location.getX(), location.getY(), location.getZ());
	}
}
