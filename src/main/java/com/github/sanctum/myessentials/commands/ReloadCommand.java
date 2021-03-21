package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.CommandData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ReloadCommand extends CommandBuilder {
	public ReloadCommand() {
		super(CommandData.RELOAD_COMMAND);
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String s, @NotNull String[] strings) {
		return false;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
		return false;
	}
}
