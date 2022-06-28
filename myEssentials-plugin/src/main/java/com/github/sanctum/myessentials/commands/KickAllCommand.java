package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.InternalCommandData;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KickAllCommand extends CommandOutput {
	public KickAllCommand() {
		super(InternalCommandData.KICKALL_COMMAND);
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {
			if (testPermission(player)) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					Bukkit.dispatchCommand(player, "kick " + p.getName());
				}
				return true;
			}
			return true;
		}

		StringBuilder sbuilder = new StringBuilder();
		for (String arg : args) {
			sbuilder.append(arg).append(" ");
		}
		if (testPermission(player)) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				Bukkit.dispatchCommand(player, "kick " + p.getName() + " " + sbuilder.toString().trim());
			}
		}
		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {
			if (testPermission(sender)) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					Bukkit.dispatchCommand(sender, "kick " + p.getName());
				}
				return true;
			}
			return true;
		}

		StringBuilder sbuilder = new StringBuilder();
		for (String arg : args) {
			sbuilder.append(arg).append(" ");
		}
		if (testPermission(sender)) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				Bukkit.dispatchCommand(sender, "kick " + p.getName() + " " + sbuilder.toString().trim());
			}
		}
		return true;
	}
}
