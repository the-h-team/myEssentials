package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DayCommand extends CommandOutput {
	public DayCommand() {
		super(InternalCommandData.DAY_COMMAND);
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return SimpleTabCompletion.of(args).then(TabCompletionIndex.ONE, "morning", "noon", "afternoon")
				.get();
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {
			if (testPermission(player)) {
				player.getWorld().setTime(0);
				sendMessage(player, ConfiguredMessage.SET_DAY);
				return true;
			}
			return true;
		}
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("morning")) {
				if (testPermission(player)) {
					player.getWorld().setTime(0);
					sendMessage(player, ConfiguredMessage.SET_MORNING);
					return true;
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("noon")) {
				if (testPermission(player)) {
					player.getWorld().setTime(6000);
					sendMessage(player, ConfiguredMessage.SET_NOON);
					return true;
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("afternoon")) {
				if (testPermission(player)) {
					player.getWorld().setTime(9500);
					sendMessage(player, ConfiguredMessage.SET_AFTERNOON);
					return true;
				}
				return true;
			}
			sendUsage(player);
		}
		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}
}
