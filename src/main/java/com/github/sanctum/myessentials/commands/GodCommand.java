package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandData;
import com.github.sanctum.myessentials.model.CommandBuilder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class GodCommand extends CommandBuilder {
	public GodCommand() {
		super(CommandData.GOD_COMMAND);
	}

	@Override
	public boolean playerView(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
		return false;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
		return false;
	}
}
