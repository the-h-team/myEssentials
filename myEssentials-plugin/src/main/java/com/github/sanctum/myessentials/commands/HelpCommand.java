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

import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.formatting.pagination.EasyPagination;
import com.github.sanctum.labyrinth.library.CommandUtils;
import com.github.sanctum.myessentials.api.EssentialsAddon;
import com.github.sanctum.myessentials.api.EssentialsAddonQuery;
import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.InternalCommandData;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class HelpCommand extends CommandOutput {
	public HelpCommand() {
		super(InternalCommandData.HELP_COMMAND);
	}

	private final SimpleTabCompletion builder = SimpleTabCompletion.empty();

	@Override
	public @NotNull List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return builder.fillArgs(args)
				.then(TabCompletionIndex.ONE, getCommands(player).stream().filter(command -> {
					try {
						Plugin p = JavaPlugin.getProvidingPlugin(command.getClass());
						return true;
					} catch (Exception ignored) {
						return false;
					}
				}).map(command -> {
					Plugin p = JavaPlugin.getProvidingPlugin(command.getClass());
					return p.getName();
				}).collect(Collectors.toList()))
				.get();
	}

	private EasyPagination<EssentialsAddon> addons(Player p) {
		EasyPagination<EssentialsAddon> easy = new EasyPagination<>(p, EssentialsAddonQuery.getKnownAddons(), Comparator.comparing(EssentialsAddon::getName));
		easy.limit(10);
		easy.setHeader((player, message) -> {
			message.then("&e▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬[ &fmEssentials &e]▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		});
		easy.setFooter((player, message) -> {
			message.then("&e▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		});
		easy.setFormat((addon, integer, m) -> {
			m.then("&r| [&7" + Arrays.toString(addon.getAuthors()) + "&r] &2" + addon.getName()).hover("&6Click &rfor &6info.").action(() -> {
				FancyMessage message = new FancyMessage();
				message.then("&f&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				message.then("\n");
				message.then("&aTotal Commands: &e" + addon.getContext().getCommands().size());
				message.then("\n");
				message.then("&aTotal Commands: &e" + addon.getContext().getCommands().size());
				message.then("\n");
				message.then("&aTotal Listeners: &e" + addon.getContext().getListeners().size());
				message.then("\n");
				message.then("&Active: &3" + addon.isActive());
				message.then("\n");
				message.then("&f&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				message.then("\n");
				message.then("&7[&8Description&7]").hover(!addon.getDescription().isEmpty() ? addon.getDescription() : "Nothing sorry :/");
				message.send(p).deploy();
			});
		});
		return easy;
	}

	private EasyPagination<Command> help(Player p) {
		EasyPagination<Command> easy = new EasyPagination<>(p, getCommands(p), Comparator.comparing(Command::getLabel));
		easy.limit(10);
		easy.setHeader((player, message) -> {
			message.then("&e▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬[ &fmEssentials &e]▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		});
		easy.setFooter((player, message) -> {
			message.then("&e▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		});
		easy.setFormat((command, integer, m) -> {
			try {
				Plugin providing = JavaPlugin.getProvidingPlugin(command.getClass());
				if (command instanceof PluginCommand) providing = ((PluginCommand) command).getPlugin();
				m.then("&f/&6" + command.getLabel()).hover("&6Click &rto &6auto-suggest.").suggest("/" + command.getLabel() + " ")
						.then(" &7[&2Plugin&7]").hover("&a" + providing).command("help " + providing.getName())
						.then(" &7[&8Description&7]").hover(!command.getDescription().isEmpty() ? command.getDescription() : "No description.");

			} catch (IllegalArgumentException e) {
				m.then("&f/&6" + command.getLabel()).hover("&6Click &rto &6auto-suggest.").suggest("/" + command.getLabel() + " ")
						.then(" &7[&2Plugin&7]").hover("&cN/A")
						.then(" &7[&8Description&7]").hover(!command.getDescription().isEmpty() ? command.getDescription() : "No description.");
			}

		});
		return easy;
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {

		int length = args.length;

		if (length == 0) {
			help(player).send(1);
			return true;
		}

		if (length == 1) {
			try {
				Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {

				if (args[0].equalsIgnoreCase("addons")) {
					addons(player).send(1);
					return true;
				}
				help(player).send(Integer.parseInt(args[0]));
			}
			return true;
		}

		return false;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}

	public List<Command> getCommands(Player p) {
		final List<Command> list = new LinkedList<>();
		return CommandUtils.read(e -> {
			Map<String, Command> map = e.getValue();
			map.forEach((key, value) -> {
				if (!list.contains(value)) {
					if (value.getPermission() != null) {
						if (p.hasPermission(value.getPermission())) {
							list.add(value);
						}
					} else {
						list.add(value);
					}
				}
			});
			return list;
		});
	}

}
