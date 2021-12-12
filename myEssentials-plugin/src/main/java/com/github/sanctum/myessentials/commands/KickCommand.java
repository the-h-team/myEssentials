package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.moderation.KickReason;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KickCommand extends CommandBuilder {
	public KickCommand() {
		super(InternalCommandData.KICK_COMMAND);
	}

	@Override
	public @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return SimpleTabCompletion.of(args).then(TabCompletionIndex.ONE, Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()))
				.get();
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {
			sendUsage(player);
			return true;
		}

		if (args.length == 1) {
			PlayerSearch search = PlayerSearch.look(args[0]);
			if (testPermission(player)) {
				if (search.isValid()) {

					if (search.kick(KickReason.next()
							.input(1, MyEssentialsAPI.getInstance().getPrefix())
							.input(2, ConfiguredMessage.YOU_WERE_KICKED.toString())
							.input(3, ConfiguredMessage.DEFAULT_KICK_REASON.toString()), false)) {
						sendMessage(player, ConfiguredMessage.TARGET_KICKED);
					} else {
						sendMessage(player, ConfiguredMessage.TARGET_OFFLINE);
					}

				} else {
					sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
					return true;
				}
				return true;
			}
			return true;
		}

		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			stringBuilder.append(args[i]).append(" ");
		}
		if (testPermission(player)) {
			String get = stringBuilder.toString().trim();

			PlayerSearch search = PlayerSearch.look(args[0]);
			if (search.isValid()) {

				if (search.kick((KickReason.next()
						.input(1, MyEssentialsAPI.getInstance().getPrefix())
						.input(2, ConfiguredMessage.YOU_WERE_KICKED.toString())
						.input(3, ConfiguredMessage.CUSTOM_KICK_REASON.replace(get))
						.reason(get)), false)) {
					sendMessage(player, ConfiguredMessage.TARGET_KICKED_WITH_REASON.replace(get));
				} else {
					sendMessage(player, ConfiguredMessage.TARGET_OFFLINE);
				}

			} else {
				sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
			}
		}
		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {
			sendUsage(sender);
			return true;
		}

		if (args.length == 1) {
			PlayerSearch search = PlayerSearch.look(args[0]);
			if (testPermission(sender)) {
				if (search.isValid()) {

					if (search.kick(KickReason.next()
							.input(1, MyEssentialsAPI.getInstance().getPrefix())
							.input(2, ConfiguredMessage.YOU_WERE_KICKED.toString())
							.input(3, ConfiguredMessage.DEFAULT_KICK_REASON.toString()), true)) {
						sendMessage(sender, ConfiguredMessage.TARGET_KICKED);
					} else {
						sendMessage(sender, ConfiguredMessage.TARGET_OFFLINE);
					}

				} else {
					sendMessage(sender, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
					return true;
				}
				return true;
			}
			return true;
		}

		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			stringBuilder.append(args[i]).append(" ");
		}
		if (testPermission(sender)) {
			String get = stringBuilder.toString().trim();

			PlayerSearch search = PlayerSearch.look(args[0]);
			if (search.isValid()) {

				if (search.kick((KickReason.next()
						.input(1, MyEssentialsAPI.getInstance().getPrefix())
						.input(2, ConfiguredMessage.YOU_WERE_KICKED.toString())
						.input(3, ConfiguredMessage.CUSTOM_KICK_REASON.replace(get))
						.reason(get)), true)) {
					sendMessage(sender, ConfiguredMessage.TARGET_KICKED_WITH_REASON.replace(get));
				} else {
					sendMessage(sender, ConfiguredMessage.TARGET_OFFLINE);
				}

			} else {
				sendMessage(sender, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
			}
		}
		return true;
	}
}
