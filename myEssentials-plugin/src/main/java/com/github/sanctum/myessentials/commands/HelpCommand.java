/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials, a derivative work inspired by the
 *  Essentials <http://ess3.net/> and EssentialsX <https://essentialsx.net/>
 *  projects, both licensed under the GPLv3.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.string.PaginatedAssortment;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class HelpCommand extends CommandBuilder {
	public HelpCommand() {
		super(InternalCommandData.HELP_COMMAND);
	}

	private PaginatedAssortment helpMenu(Player p) {
		List<String> collection = new ArrayList<>(getCommandList(p));
		Collections.sort(collection);
		return new PaginatedAssortment(p, collection).
				setNavigateCommand("help").
				setListTitle("&e▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬[ &fmEssentials ({PAGE}/{TOTAL}) &e]▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").
				setListBorder("&e▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").
				setLinesPerPage(10);
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {

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
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}

	public static List<String> getCommandList(Player p) {
		final List<String> list = new LinkedList<>();
		Essentials.KNOWN_COMMANDS_MAP.forEach((key, value) -> {
			final String text = "/&6" + value.getName() + " &r- " + value.getDescription();
			if (!list.contains(value.getDescription().isEmpty() ? "/&6" + value.getName() + "&r" : text)) {
				if (value.getPermission() != null && p.hasPermission(value.getPermission())) {
					list.add(value.getDescription().isEmpty() ? "/&6" + value.getName() + "&r" : text);
				}
			}
		});
		return list;
/*		LinkedList<String> append = internalMap.stream()
				.filter(data -> p.hasPermission(Objects.requireNonNull(data.getPermissionNode())))
				.map(data -> data.getUsage() + " &r- " + data.getDescription()).collect(Collectors.toCollection(LinkedList::new));
		try {
			SimpleCommandMap commandMap = getCommandMap();
			assert commandMap != null;
			final List<String> placement = new ArrayList<>();
			for (Map.Entry<String, Command> entry : KNOWN_COMMANDS_MAP.entrySet()) {
                String text = "/&6" + entry.getValue().getName() + " &r- " + entry.getValue().getDescription();
				if (!append.contains(entry.getValue().getDescription().isEmpty() ? "/&6" + entry.getValue().getName() + "&r" : text)) {
					if (entry.getValue().getPermission() != null && p.hasPermission(entry.getValue().getPermission())) {
						append.add(entry.getValue().getDescription().isEmpty() ? "/&6" + entry.getValue().getName() + "&r" : text);
					}
				}
			}
			return
		} catch (Exception e) {
			e.printStackTrace();
		}
		return append;*/
	}
}
