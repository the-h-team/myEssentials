package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NightCommand extends CommandBuilder {
	public NightCommand() {
		super(InternalCommandData.NIGHT_COMMAND);
	}

	@Override
	public @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return SimpleTabCompletion.of(args).then(TabCompletionIndex.ONE, "night", "midnight", "dusk")
				.get();
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {
			if (testPermission(player)) {
				player.getWorld().setTime(16000);
				sendMessage(player, ConfiguredMessage.SET_NIGHT);
				return true;
			}
			return true;
		}
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("night")) {
				if (testPermission(player)) {
					player.getWorld().setTime(13000);
					sendMessage(player, ConfiguredMessage.SET_NIGHT);
					return true;
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("midnight")) {
				if (testPermission(player)) {
					player.getWorld().setTime(18000);
					sendMessage(player, ConfiguredMessage.SET_MIDNIGHT);
					return true;
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("dusk")) {
				if (testPermission(player)) {
					player.getWorld().setTime(22000);
					sendMessage(player, ConfiguredMessage.SET_DUSK);
					return true;
				}
				return true;
			}
			sendUsage(player);
		}
		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}
}
