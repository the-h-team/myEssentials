package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.string.PaginatedAssortment;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class HelpCommand extends CommandBuilder {
	public HelpCommand() {
		super(InternalCommandData.HELP_COMMAND);
	}

	private PaginatedAssortment helpMenu(Player p) {
		return new PaginatedAssortment(p, CommandBuilder.getCommandList(p)).
				setNavigateCommand("help").
				setListTitle("&e▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬[ &fmEssentials ({PAGE}/{TOTAL}) &e]▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").
				setListBorder("&e▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").
				setLinesPerPage(10);
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String s, @NotNull String[] args) {

		int length = args.length;

		if (length == 0) {
			helpMenu(player).export(1);
			return true;
		}

		if (length == 1) {
			try {
				Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				return true;
			}
			helpMenu(player).export(Integer.parseInt(args[0]));
			return true;
		}

		return false;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
		return false;
	}
}
