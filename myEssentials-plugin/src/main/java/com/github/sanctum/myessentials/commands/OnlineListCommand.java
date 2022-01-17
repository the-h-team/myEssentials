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

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.library.ListUtils;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.labyrinth.permissions.Permissions;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OnlineListCommand extends CommandBuilder {
	public OnlineListCommand() {
		super(InternalCommandData.ONLINELIST_COMMAND);
	}

	@Override
	public @Nullable
	List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return null;
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		List<BaseComponent> list = new ArrayList<>();
		TextLib lib = TextLib.getInstance();
		sendMessage(player, "&fThere are currently &b" + PlayerSearch.getOnlinePlayers().list().size() + " &7/ &3" + Bukkit.getMaxPlayers() + "&f players online.");
		Mailer.empty(player).chat("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").deploy();
		Map<String, List<Player>> map = new HashMap<>();
		Permissions permissions = LabyrinthProvider.getInstance().getServicesManager().load(Permissions.class);
		for (Player p : PlayerSearch.getOnlinePlayers().sort()) {
			String group = permissions.getUser(p).getGroup(p.getWorld().getName()).getName();
			if (map.get(group) == null) {
				List<Player> l = new ArrayList<>();
				l.add(p);
				map.put(group, l);
			} else {
				List<Player> l = map.get(group);
				l.add(p);
				map.put(group, l);
			}
		}
		for (Map.Entry<String, List<Player>> entry : map.entrySet()) {

			BaseComponent c = lib.textSuggestable(OptionLoader.GROUP_PREFIX.getString(entry.getKey()) + ": ", "", "", "");
			List<TextComponent> l = ListUtils.use(entry.getValue().stream().map(p -> lib.textSuggestable("", OptionLoader.GROUP_COLOR.getString(entry.getKey()) + p.getDisplayName(), "&7Click to message me.", "msg " + p.getName() + " ")).collect(Collectors.toList())).append(b -> {
				b.addExtra(", ");
				return b;
			});
			for (BaseComponent b : l) {
				c.addExtra(b);
			}
			list.add(c);
		}
		for (BaseComponent b : list) {
			sendComponent(player, b);
		}
		Mailer.empty(player).chat("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").deploy();
		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
		return true;
	}
}
