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
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.DateTimeCalculator;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TempBanCommand extends CommandBuilder {
	private final TabCompletionBuilder builder = TabCompletion.build(getData().getLabel());

	public TempBanCommand() {
		super(InternalCommandData.TEMPBAN_COMMAND);
	}

	@Override
	public @NotNull
	List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {

		if (args.length == 3) {
			return builder.forArgs(args)
					.level(3)
					.completeAt(getData().getLabel())
					.filter(() -> Collections.singletonList(ConfiguredMessage.REASON.toString()))
					.collect()
					.get(3);
		}

		if (args.length == 2) {
			return builder.forArgs(args)
					.level(2)
					.completeAt(getData().getLabel())
					.filter(() -> {
						List<String> result = new ArrayList<>(Arrays.asList("1s", "1m", "1h", "1d", "2s", "2m", "3h", "3d"));
						Collections.sort(result);
						return result;
					})
					.collect()
					.get(2);
		}

		return builder.forArgs(args)
				.level(1)
				.completeAt(getData().getLabel())
				.filter(() -> Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList()))
				.collect()
				.get(1);
	}


	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {

		if (args.length == 0) {
			sendUsage(player);
			return true;
		}

		if (args.length == 1) {
			sendUsage(player);
			return true;
		}


		if (args.length == 2) {
			if (testPermission(player)) {
				PlayerSearch search = PlayerSearch.look(args[0]);
				if (search.isValid()) {
					long result;
					try {
						result = DateTimeCalculator.parse(args[1].toUpperCase());
					} catch (DateTimeParseException e) {
						try {
							result = DateTimeCalculator.parseShort(args[1].toUpperCase());
						} catch (DateTimeParseException ex) {
							sendMessage(player, ConfiguredMessage.INVALID_TIME_FORMAT);
							sendMessage(player, ConfiguredMessage.TIME_EXAMPLE);
							return true;
						}
					}

					if (search.ban(player.getName(), kick -> {
						kick.input(1, ConfiguredMessage.YOU_HAVE_BEEN_BANNED.toString());
						kick.input(2, ConfiguredMessage.BAN_EXPIRATION.replace(search.getBanTimer().fullTimeLeft()));
					}, result, false)) {
						sendMessage(player, ConfiguredMessage.UNBAN_TIME_TO_SENDER.replace(search.getBanTimer().fullTimeLeft()));
					} else {
						if (search.getBanTimer() != null) {
							if (search.getBanTimer().isComplete()) {
								Cooldown.remove(search.getBanTimer());
								search.unban(false);
								Bukkit.dispatchCommand(player, commandLabel + " " + args[0] + " " + args[1]);
								return true;
							}
							sendMessage(player, ConfiguredMessage.TARGET_ALREADY_BANNED);
							sendMessage(player, ConfiguredMessage.UNBANNED_TIME.replace(search.getBanTimer().fullTimeLeft()));
						}
					}
				}
			} else {
				if (testPermission(player)) {
					sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
					return true;
				}
				return true;
			}
			return true;
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 2; i < args.length; i++) {
			builder.append(args[i]).append(" ");
		}
		String get = builder.toString().trim();

		long result;
		try {
			result = DateTimeCalculator.parse(args[1].toUpperCase());
		} catch (DateTimeParseException e) {
			try {
				result = DateTimeCalculator.parseShort(args[1].toUpperCase());
			} catch (DateTimeParseException ex) {
				sendMessage(player, ConfiguredMessage.INVALID_TIME_FORMAT);
				sendMessage(player, ConfiguredMessage.TIME_EXAMPLE);
				return true;
			}
		}
		if (testPermission(player)) {
			PlayerSearch search = PlayerSearch.look(args[0]);
			if (search.isValid()) {
				if (search.ban(player.getName(), kick -> {
					kick.input(1, ConfiguredMessage.YOU_HAVE_BEEN_BANNED.toString());
					final String replace = ConfiguredMessage.BAN_KICK_REASON.replace(get);
					kick.input(3, replace);
					kick.input(2, ConfiguredMessage.BAN_EXPIRATION.replace(search.getBanTimer().fullTimeLeft()));
					kick.reason(StringUtils.translate(replace));
				}, result, false)) {
					sendMessage(player, ConfiguredMessage.UNBAN_TIME_TO_SENDER.replace(search.getBanTimer().fullTimeLeft()));
				} else {
					if (search.getBanTimer() != null) {
						if (search.getBanTimer().isComplete()) {
							Cooldown.remove(search.getBanTimer());
							search.unban(false);
							Bukkit.dispatchCommand(player, commandLabel + " " + args[0] + " " + args[1] + " " + get);
							return true;
						}
						sendMessage(player, ConfiguredMessage.TARGET_ALREADY_BANNED);
						sendMessage(player, ConfiguredMessage.UNBANNED_TIME.replace(search.getBanTimer().fullTimeLeft()));
					}
				}

			} else {
				if (testPermission(player)) {
					sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
					return true;
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {
			sendUsage(sender);
			return true;
		}

		if (args.length == 1) {
			sendUsage(sender);
			return true;
		}


		if (args.length == 2) {
			if (testPermission(sender)) {
				PlayerSearch search = PlayerSearch.look(args[0]);
				if (search.isValid()) {
					long banLength;
					try {
						banLength = DateTimeCalculator.parse(args[1].toUpperCase());
					} catch (DateTimeParseException e) {
						try {
							banLength = DateTimeCalculator.parseDays(args[1].toUpperCase());
						} catch (DateTimeParseException ex) {
							try {
								banLength = DateTimeCalculator.parseShort(args[1].toUpperCase());
							} catch (DateTimeParseException exc) {
								sendMessage(sender, ConfiguredMessage.INVALID_TIME_CONSOLE);
								sendMessage(sender, ConfiguredMessage.TIME_EXAMPLE);
								return true;
							}
						}
					}
					if (search.ban(sender.getName(), kick -> {
						kick.input(1, ConfiguredMessage.YOU_HAVE_BEEN_BANNED.toString());
						kick.input(2, ConfiguredMessage.BAN_EXPIRATION.replace(search.getBanTimer().fullTimeLeft()));
					}, banLength, false)) {
						sendMessage(sender, ConfiguredMessage.UNBAN_TIME_TO_SENDER.replace(search.getBanTimer().fullTimeLeft()));
					} else {
						if (search.getBanTimer() != null) {
							if (search.getBanTimer().isComplete()) {
								Cooldown.remove(search.getBanTimer());
								search.unban(false);
								Bukkit.dispatchCommand(sender, commandLabel + " " + args[0] + " " + args[1]);
								return true;
							}
							sendMessage(sender, ConfiguredMessage.TARGET_ALREADY_BANNED);
							sendMessage(sender, ConfiguredMessage.UNBANNED_TIME.replace(search.getBanTimer().fullTimeLeft()));
						}
					}
				}
			} else {
				if (testPermission(sender)) {
					sendMessage(sender, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
					return true;
				}
				return true;
			}
			return true;
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 2; i < args.length; i++) {
			builder.append(args[i]).append(" ");
		}
		String get = builder.toString().trim();

		long banLength;
		try {
			banLength = DateTimeCalculator.parse(args[1].toUpperCase());
		} catch (DateTimeParseException e) {
			try {
				banLength = DateTimeCalculator.parseDays(args[1].toUpperCase());
			} catch (DateTimeParseException ex) {
				try {
					banLength = DateTimeCalculator.parseShort(args[1].toUpperCase());
				} catch (DateTimeParseException exc) {
					sendMessage(sender, ConfiguredMessage.INVALID_TIME_CONSOLE);
					sendMessage(sender, ConfiguredMessage.TIME_EXAMPLE);
					return true;
				}
			}
		}
		if (testPermission(sender)) {
			PlayerSearch search = PlayerSearch.look(args[0]);
			if (search.isValid()) {
				if (search.ban(sender.getName(), kick -> {
					kick.input(1, ConfiguredMessage.YOU_HAVE_BEEN_BANNED.toString());
					final String replace = ConfiguredMessage.BAN_KICK_REASON.replace(get);
					kick.input(3, replace);
					kick.input(2, ConfiguredMessage.BAN_EXPIRATION.replace(search.getBanTimer().fullTimeLeft()));
					kick.reason(StringUtils.translate(replace));
				}, banLength, false)) {
					sendMessage(sender, ConfiguredMessage.UNBAN_TIME_TO_SENDER.replace(search.getBanTimer().fullTimeLeft()));
				} else {
					if (search.getBanTimer() != null) {
						if (search.getBanTimer().isComplete()) {
							Cooldown.remove(search.getBanTimer());
							search.unban(false);
							Bukkit.dispatchCommand(sender, commandLabel + " " + args[0] + " " + args[1] + " " + get);
							return true;
						}
						sendMessage(sender, ConfiguredMessage.TARGET_ALREADY_BANNED);
						sendMessage(sender, ConfiguredMessage.UNBANNED_TIME.replace(search.getBanTimer().fullTimeLeft()));
					}
				}

			} else {
				if (testPermission(sender)) {
					sendMessage(sender, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
					return true;
				}
				return true;
			}
		}
		return false;
	}
}
