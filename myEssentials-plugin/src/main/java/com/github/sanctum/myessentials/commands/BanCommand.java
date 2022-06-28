package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BanCommand extends CommandOutput {
	public BanCommand() {
		super(InternalCommandData.BAN_COMMAND);
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList()))
				.get();
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {
			sendUsage(player);
			return true;
		}

		if (args.length == 1) {
			PlayerSearch search = PlayerSearch.look(args[0]);
			if (testPermission(player)) {
				if (search.isValid()) {
					if (search.ban(player.getName())) {
						sendMessage(player, ConfiguredMessage.BANNED_TARGET);
					} else {
						sendMessage(player, ConfiguredMessage.TARGET_ALREADY_BANNED);
					}

				} else {
					sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
					return true;
				}
				return true;
			}
			return true;
		}

		StringBuilder text = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			text.append(args[i]).append(" ");
		}
		String get = text.toString().trim();

		PlayerSearch search = PlayerSearch.look(args[0]);
		if (testPermission(player)) {
			if (search.isValid()) {

				if (search.ban(player.getName(), get)) {
					sendMessage(player, ConfiguredMessage.BANNED_REASON.replace(get));
				} else {
					sendMessage(player, ConfiguredMessage.TARGET_ALREADY_BANNED);
				}

			} else {
				sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
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
			PlayerSearch search = PlayerSearch.look(args[0]);
			if (testPermission(sender)) {
				if (search.isValid()) {

					OfflinePlayer target = search.getOfflinePlayer();

					if (search.ban(sender.getName())) {
						sendMessage(sender, ConfiguredMessage.BANNED_TARGET);
					} else {
						sendMessage(sender, ConfiguredMessage.TARGET_ALREADY_BANNED);
					}

				} else {
					sendMessage(sender, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
					return true;
				}
				return true;
			}
			return true;
		}

		StringBuilder text = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			text.append(args[i]).append(" ");
		}
		String get = text.toString().trim();

		PlayerSearch search = PlayerSearch.look(args[0]);
		if (testPermission(sender)) {
			if (search.isValid()) {

				OfflinePlayer target = search.getOfflinePlayer();

				if (search.ban(sender.getName(), get)) {
					sendMessage(sender, ConfiguredMessage.BANNED_REASON.replace(get));
				} else {
					sendMessage(sender, ConfiguredMessage.TARGET_ALREADY_BANNED);
				}

			} else {
				sendMessage(sender, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
			}
		}
		return true;
	}
}
