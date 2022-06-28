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

import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.data.service.PlayerSearch;
import com.github.sanctum.labyrinth.formatting.FancyMessageChain;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.formatting.string.ImageBreakdown;
import com.github.sanctum.labyrinth.library.TimeWatch;
import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.util.OptionLoader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class WhoisCommand extends CommandOutput {

	public WhoisCommand() {
		super(OptionLoader.TEST_COMMAND.from("whois", "/whois <playerName>", "View information on a given player.", "mess.staff.whois"));
	}

	public void lookup(OfflinePlayer target, Player viewer) {
		FancyMessageChain chain = new FancyMessageChain();

		chain.append(fancy -> {
			fancy.then(target.getName()).style(ChatColor.BOLD).color(ChatColor.DARK_GREEN).then(" ").then("info").color(ChatColor.DARK_GRAY);
		}).append(fancy -> {
			fancy.then("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").style(ChatColor.BOLD, ChatColor.STRIKETHROUGH).color(ChatColor.DARK_GREEN);
		});

		ImageBreakdown breakdown = PlayerSearch.of(target).getHeadImage();

		breakdown.append(" ", "&2ONLINE " + (target.isOnline() ? "&atrue" : "&cfalse"), " ", "&2IP &8" + (target.isOnline() ? target.getPlayer().getAddress().toString() : "N/A"), " ", "&2UUID &8[&bHover&8]");

		int index = 0;
		for (String line : breakdown.read()) {
			if (index == 5) {
				chain.append(fancy -> fancy.then(line).hover("&d" + target.getUniqueId()).suggest(target.getUniqueId().toString()));
			} else {
				chain.append(fancy -> fancy.then(line));
			}
			index++;
		}

		chain.append(fancy -> {
			fancy.then("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").style(ChatColor.BOLD, ChatColor.STRIKETHROUGH).color(ChatColor.DARK_GREEN);
		}).append(fancy -> {
			TimeWatch.Recording rec = PlayerSearch.of(target).getPlaytime();
			String time = "&b" + rec.getDays() + " &8days, &b" + rec.getHours() + " &8hours &b" + rec.getMinutes() + " &8minutes & &b" + rec.getSeconds() + " &8seconds";
			fancy.then("GAMETIME").color(ChatColor.DARK_GREEN).then(" ").then(time);
		}).append(fancy -> {
			TimeWatch.Recording rec = TimeWatch.Recording.subtract(target.getLastPlayed());
			String time = "&b" + rec.getDays() + " &8days or &b" + rec.getHours() + " &8hours &b" + rec.getMinutes() + " &8minutes & &b" + rec.getSeconds() + " &8seconds ago.";
			fancy.then("PLAYED").color(ChatColor.DARK_GREEN).then(" ").then(time);
		}).append(fancy -> {
			fancy.then("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").style(ChatColor.BOLD, ChatColor.STRIKETHROUGH).color(ChatColor.DARK_GREEN);
		});


		chain.send(viewer).queue();
	}

	@Override
	public @NotNull
	List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList()))
				.get();
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {

			if (args.length == 0) {
				lookup(player, player);
			}

			if (args.length == 1) {

				LabyrinthUser user = PlayerSearch.of(args[0]);

				if (user != null) {
					lookup(user.getPlayer(), player);
				}

			}

		}
		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return true;
	}
}
