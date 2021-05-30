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

import com.github.sanctum.labyrinth.formatting.PaginatedList;
import com.github.sanctum.labyrinth.formatting.TabCompletion;
import com.github.sanctum.labyrinth.formatting.TabCompletionBuilder;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.api.AddonQuery;
import com.github.sanctum.myessentials.api.EssentialsAddon;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class HelpCommand extends CommandBuilder {
	public HelpCommand() {
		super(InternalCommandData.HELP_COMMAND);
	}

	private final TabCompletionBuilder builder = TabCompletion.build(getData().getLabel());

	@Override
	public @NotNull List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return builder.forArgs(args)
				.level(1)
				.completeAt(getData().getLabel())
				.filter(() -> getCommands(player).stream().filter(command -> {
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
				.collect()
				.get(1);
	}

	private PaginatedList<EssentialsAddon> addons(Player p) {
		Message msg = Message.form(p);
		return new PaginatedList<>(new ArrayList<>(AddonQuery.getKnownAddons()))
				.limit(10)
				.compare(Comparator.comparing(EssentialsAddon::getAddonName))
				.start((pagination, page, max) -> msg.send("&e▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬[ &fmEssentials (" + page + "/" + max + ") &e]▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"))
				.finish((pagination, page, max) -> {
					msg.send("&e▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					TextLib component = TextLib.getInstance();
					int next = page + 1;
					int last = Math.max(page - 1, 1);
					List<BaseComponent> toSend = new LinkedList<>();
					if (page == 1) {
						if (page == max) {
							toSend.add(component.textHoverable("", "&8« ", "You are on the first page already."));
							toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
							toSend.add(component.textHoverable("", " &8»", "You are already on the last page."));
							msg.build(toSend.toArray(new BaseComponent[0]));
							return;
						}
						toSend.add(component.textHoverable("", "&8« ", "You are on the first page already."));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.execute(() -> pagination.get(next), component.textHoverable("", " &3»", "")));
						msg.build(toSend.toArray(new BaseComponent[0]));
						return;
					}
					if (page == max) {
						toSend.add(component.execute(() -> pagination.get(last), component.textHoverable("", "&3« ", "")));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.textHoverable("", " &8»", "You are already on the last page."));
						msg.build(toSend.toArray(new BaseComponent[0]));
						return;
					}
					if (next <= max) {
						toSend.add(component.execute(() -> pagination.get(last), component.textHoverable("", "&3« ", "")));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.execute(() -> pagination.get(next), component.textHoverable("", " &3»", "")));
						msg.build(toSend.toArray(new BaseComponent[0]));
					}
				}).decorate((pagination, addon, page, max, placement) -> {
					TextLib.consume(t -> {
						LinkedList<BaseComponent> toSend = new LinkedList<>();
						toSend.add(t.execute(() -> {
							sendMessage(p, "&f&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
							sendMessage(p, "&aTotal Commands: &e" + addon.getCommands().size());
							sendMessage(p, "&aTotal Listeners: &e" + addon.getListeners().size());
							sendMessage(p, "&aStandalone: &3" + addon.isStandalone());
							sendMessage(p, "&f&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
						}, t.textHoverable("&r| [&7" + Arrays.toString(addon.getAuthors()) + "&r] ", "&2" + addon.getAddonName(), "&6Click &rfor &6info.")));
						toSend.add(t.textHoverable("", " &7[&8Description&7]", (!addon.getAddonDescription().isEmpty() ? addon.getAddonDescription() : "Nothing sorry :/")));
						msg.build(toSend.toArray(new BaseComponent[0]));

					});
				});
	}

	private PaginatedList<Command> help(Player p) {
		Message msg = Message.form(p);
		return new PaginatedList<>(getCommands(p))
				.limit(10)
				.compare(Comparator.comparing(Command::getLabel))
				.start((pagination, page, max) -> msg.send("&e▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬[ &fmEssentials (" + page + "/" + max + ") &e]▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"))
				.finish((pagination, page, max) -> {
					msg.send("&e▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					TextLib component = TextLib.getInstance();
					int next = page + 1;
					int last = Math.max(page - 1, 1);
					List<BaseComponent> toSend = new LinkedList<>();
					if (page == 1) {
						if (page == max) {
							toSend.add(component.textHoverable("", "&8« ", "You are on the first page already."));
							toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
							toSend.add(component.textHoverable("", " &8»", "You are already on the last page."));
							msg.build(toSend.toArray(new BaseComponent[0]));
							return;
						}
						toSend.add(component.textHoverable("", "&8« ", "You are on the first page already."));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.execute(() -> pagination.get(next), component.textHoverable("", " &3»", "")));
						msg.build(toSend.toArray(new BaseComponent[0]));
						return;
					}
					if (page == max) {
						toSend.add(component.execute(() -> pagination.get(last), component.textHoverable("", "&3« ", "")));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.textHoverable("", " &8»", "You are already on the last page."));
						msg.build(toSend.toArray(new BaseComponent[0]));
						return;
					}
					if (next <= max) {
						toSend.add(component.execute(() -> pagination.get(last), component.textHoverable("", "&3« ", "")));
						toSend.add(component.textHoverable("&f<&7" + page + "&f/&7" + max + "&f>", "", ""));
						toSend.add(component.execute(() -> pagination.get(next), component.textHoverable("", " &3»", "")));
						msg.build(toSend.toArray(new BaseComponent[0]));
					}
				}).decorate((pagination, command, page, max, placement) -> {
					try {
						Plugin providing = JavaPlugin.getProvidingPlugin(command.getClass());

						TextLib.consume(t -> {
							LinkedList<BaseComponent> toSend = new LinkedList<>();
							toSend.add(t.textSuggestable("&r/", "&6" + command.getLabel(), "&6Click &rto &6auto-suggest.", command.getLabel() + " "));
							toSend.add(t.textRunnable("", " &7[&2Plugin&7]", "&a" + providing, "help " + providing.getName()));
							toSend.add(t.textHoverable("", " &7[&8Description&7]", (!command.getDescription().isEmpty() ? command.getDescription() : "Nothing sorry :/")));
							msg.build(toSend.toArray(new BaseComponent[0]));

						});

					} catch (IllegalArgumentException e) {
						TextLib.consume(t -> {

							LinkedList<BaseComponent> toSend = new LinkedList<>();
							toSend.add(t.textSuggestable("&r/", "&6" + command.getLabel(), "&6Click &rto &6auto-suggest.", command.getLabel() + " "));
							toSend.add(t.textHoverable("", " &7[&2Plugin&7]", "&cNo info :/"));
							toSend.add(t.textHoverable("", " &7[&8Description&7]", (!command.getDescription().isEmpty() ? command.getDescription() : "Nothing sorry :/")));
							msg.build(toSend.toArray(new BaseComponent[0]));

						});
					}
				});
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {

		int length = args.length;

		if (length == 0) {
			help(player).get(1);
			return true;
		}

		Message msg = Message.form(player);

		if (length == 1) {
			try {
				Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {

				if (args[0].equalsIgnoreCase("addons")) {
					addons(player).get(1);
					return true;
				}

				help(player).filter(cmd -> {
					try {
						Plugin p = JavaPlugin.getProvidingPlugin(cmd.getClass());
						return p.getName().equalsIgnoreCase(args[0]);
					} catch (Exception ignored) {
						return false;
					}
				}).start((pagination, page, max) -> msg.send("&e▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬[ &f" + args[0] + " (" + page + "/" + max + ") &e]▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"))
						.decorate((pagination, command, page, max, placement) ->
								TextLib.consume(t -> msg.build(t.textSuggestable("&r/", "&6" + command.getLabel() + " &r- " + command.getDescription(), "&6Click &rto &6auto-suggest.", command.getLabel() + " ")))).get(1);
				return true;
			}
			help(player).get(Integer.parseInt(args[0]));
			return true;
		}

		return false;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}

	public List<Command> getCommands(Player p) {
		final List<Command> list = new LinkedList<>();
		Essentials.KNOWN_COMMANDS_MAP.forEach((key, value) -> {
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
	}

}
