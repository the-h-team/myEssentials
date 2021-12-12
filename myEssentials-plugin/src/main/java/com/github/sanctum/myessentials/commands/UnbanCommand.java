package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnbanCommand extends CommandBuilder {
	public UnbanCommand() {
		super(InternalCommandData.UNBAN_COMMAND);
	}

	@Override
	public @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return SimpleTabCompletion.of(args).then(TabCompletionIndex.ONE, Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()))
				.get();
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {
			if (args.length == 1) {
				PlayerSearch search = PlayerSearch.look(args[0]);
				if (search.isValid()) {

					if (search.unban()) {
						sendMessage(player, ConfiguredMessage.TARGET_UNBANNED);
					} else {
						sendMessage(player, ConfiguredMessage.TARGET_NOT_BANNED);
					}

				} else {
					sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
				}
			}
		}
		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {
			if (args.length == 1) {
				PlayerSearch search = PlayerSearch.look(args[0]);
				if (search.isValid()) {

					if (search.unban()) {
						sendMessage(player, ConfiguredMessage.TARGET_UNBANNED);
					} else {
						sendMessage(player, ConfiguredMessage.TARGET_NOT_BANNED);
					}

				} else {
					sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
				}
			}
		}
		return true;
	}
}
