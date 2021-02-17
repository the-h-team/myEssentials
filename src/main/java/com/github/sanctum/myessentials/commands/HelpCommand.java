package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.string.PaginatedAssortment;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.CommandData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HelpCommand extends CommandBuilder {
	public HelpCommand() {
		super(CommandData.HELP_COMMAND);
	}

	private PaginatedAssortment helpMenu(Player p) {
		PaginatedAssortment assortment = new PaginatedAssortment(p, CommandBuilder.getCommandList());
		assortment.setNavigateCommand("help");
		assortment.setListTitle("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬[ mEssentials ]▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		assortment.setListBorder("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		assortment.setLinesPerPage(10);
		return assortment;
	}

	@Override
	public boolean playerView(Player p, @NotNull String s, @NotNull String[] args) {

		int length = args.length;

		if (length == 0) {
			helpMenu(p).export(1);
			return true;
		}

		if (length == 1) {
			try {
				Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				return true;
			}
			helpMenu(p).export(Integer.parseInt(args[0]));
			return true;
		}

		return false;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
		return false;
	}
}
