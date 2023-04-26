package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.model.CommandInput;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.DateTimeCalculator;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import com.github.sanctum.panther.util.ParsedTimeFormat;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TempbanCommand extends CommandInput {
	public TempbanCommand() {
		super(InternalCommandData.TEMPBAN_COMMAND);
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return SimpleTabCompletion.of(args).then(TabCompletionIndex.ONE, Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList()))
				.then(TabCompletionIndex.TWO, "1s", "1m", "1h", "1d", "2s", "2m", "3h", "3d")
				.then(TabCompletionIndex.THREE, Collections.singletonList(ConfiguredMessage.REASON.toString()))
				.get();
	}

	long toSeconds(ParsedTimeFormat format) {
		return TimeUnit.HOURS.toSeconds(format.getHours()) + TimeUnit.MINUTES.toSeconds(format.getMinutes()) + format.getSeconds();
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
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
						result = toSeconds(ParsedTimeFormat.of(args[1]));
					} catch (DateTimeParseException e) {
						sendMessage(player, ConfiguredMessage.INVALID_TIME_FORMAT);
						sendMessage(player, ConfiguredMessage.TIME_EXAMPLE);
						return true;
					}

					if (search.ban(player.getName(), kick -> {
						kick.input(1, ConfiguredMessage.YOU_HAVE_BEEN_BANNED.toString());
						kick.input(2, ConfiguredMessage.BAN_EXPIRATION.replace(search.getBanTimer().toFormat()));
					}, result, false)) {
						sendMessage(player, ConfiguredMessage.UNBAN_TIME_TO_SENDER.replace(search.getBanTimer().toFormat()));
					} else {
						if (search.getBanTimer() != null) {
							if (search.getBanTimer().isComplete()) {
								LabyrinthProvider.getInstance().remove(search.getBanTimer());
								search.unban(false);
								Bukkit.dispatchCommand(player, commandLabel + " " + args[0] + " " + args[1]);
								return true;
							}
							sendMessage(player, ConfiguredMessage.TARGET_ALREADY_BANNED);
							sendMessage(player, ConfiguredMessage.UNBANNED_TIME.replace(search.getBanTimer().toFormat()));
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

		StringBuilder sbuilder = new StringBuilder();
		for (int i = 2; i < args.length; i++) {
			sbuilder.append(args[i]).append(" ");
		}
		String get = sbuilder.toString().trim();

		long result;
		try {
			result = toSeconds(ParsedTimeFormat.of(args[1]));
		} catch (DateTimeParseException e) {
			sendMessage(player, ConfiguredMessage.INVALID_TIME_FORMAT);
			sendMessage(player, ConfiguredMessage.TIME_EXAMPLE);
			return true;
		}
		if (testPermission(player)) {
			PlayerSearch search = PlayerSearch.look(args[0]);
			if (search.isValid()) {
				if (search.ban(player.getName(), kick -> {
					kick.input(1, ConfiguredMessage.YOU_HAVE_BEEN_BANNED.toString());
					final String replace = ConfiguredMessage.BAN_KICK_REASON.replace(get);
					kick.input(3, replace);
					kick.input(2, ConfiguredMessage.BAN_EXPIRATION.replace(search.getBanTimer().toFormat()));
					kick.reason(StringUtils.use(get).translate());
				}, result, false)) {
					sendMessage(player, ConfiguredMessage.UNBAN_TIME_TO_SENDER.replace(search.getBanTimer().toFormat()));
				} else {
					if (search.getBanTimer() != null) {
						if (search.getBanTimer().isComplete()) {
							LabyrinthProvider.getInstance().remove(search.getBanTimer());
							search.unban(false);
							Bukkit.dispatchCommand(player, commandLabel + " " + args[0] + " " + args[1] + " " + get);
							return true;
						}
						sendMessage(player, ConfiguredMessage.TARGET_ALREADY_BANNED);
						sendMessage(player, ConfiguredMessage.UNBANNED_TIME.replace(search.getBanTimer().toFormat()));
					}
				}

			} else {
				if (testPermission(player)) {
					sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
				}
			}
		}
		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
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
						banLength = toSeconds(ParsedTimeFormat.of(args[1]));
					} catch (DateTimeParseException e) {
						sendMessage(sender, ConfiguredMessage.INVALID_TIME_CONSOLE);
						sendMessage(sender, ConfiguredMessage.TIME_EXAMPLE);
						return true;
					}
					if (search.ban(sender.getName(), kick -> {
						kick.input(1, ConfiguredMessage.YOU_HAVE_BEEN_BANNED.toString());
						kick.input(2, ConfiguredMessage.BAN_EXPIRATION.replace(search.getBanTimer().toFormat()));
					}, banLength, false)) {
						sendMessage(sender, ConfiguredMessage.UNBAN_TIME_TO_SENDER.replace(search.getBanTimer().toFormat()));
					} else {
						if (search.getBanTimer() != null) {
							if (search.getBanTimer().isComplete()) {
								LabyrinthProvider.getInstance().remove(search.getBanTimer());
								search.unban(false);
								Bukkit.dispatchCommand(sender, commandLabel + " " + args[0] + " " + args[1]);
								return true;
							}
							sendMessage(sender, ConfiguredMessage.TARGET_ALREADY_BANNED);
							sendMessage(sender, ConfiguredMessage.UNBANNED_TIME.replace(search.getBanTimer().toFormat()));
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

		StringBuilder sbuilder = new StringBuilder();
		for (int i = 2; i < args.length; i++) {
			sbuilder.append(args[i]).append(" ");
		}
		String get = sbuilder.toString().trim();

		long banLength;
		try {
			banLength = DateTimeCalculator.parse(args[1].toUpperCase());
		} catch (DateTimeParseException e) {
			try {
				banLength = toSeconds(ParsedTimeFormat.of(args[1]));
			} catch (DateTimeParseException ex) {
				sendMessage(sender, ConfiguredMessage.INVALID_TIME_CONSOLE);
				sendMessage(sender, ConfiguredMessage.TIME_EXAMPLE);
				return true;
			}
		}
		if (testPermission(sender)) {
			PlayerSearch search = PlayerSearch.look(args[0]);
			if (search.isValid()) {
				if (search.ban(sender.getName(), kick -> {
					kick.input(1, ConfiguredMessage.YOU_HAVE_BEEN_BANNED.toString());
					final String replace = ConfiguredMessage.BAN_KICK_REASON.replace(get);
					kick.input(3, replace);
					kick.input(2, ConfiguredMessage.BAN_EXPIRATION.replace(search.getBanTimer().toFormat()));
					kick.reason(StringUtils.use(get).translate());
				}, banLength, false)) {
					sendMessage(sender, ConfiguredMessage.UNBAN_TIME_TO_SENDER.replace(search.getBanTimer().toFormat()));
				} else {
					if (search.getBanTimer() != null) {
						if (search.getBanTimer().isComplete()) {
							LabyrinthProvider.getInstance().remove(search.getBanTimer());
							search.unban(false);
							Bukkit.dispatchCommand(sender, commandLabel + " " + args[0] + " " + args[1] + " " + get);
							return true;
						}
						sendMessage(sender, ConfiguredMessage.TARGET_ALREADY_BANNED);
						sendMessage(sender, ConfiguredMessage.UNBANNED_TIME.replace(search.getBanTimer().toFormat()));
					}
				}

			} else {
				if (testPermission(sender)) {
					sendMessage(sender, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
				}
			}
		}
		return true;
	}
}
