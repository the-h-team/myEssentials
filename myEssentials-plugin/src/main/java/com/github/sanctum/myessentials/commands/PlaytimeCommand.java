package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.pagination.EasyPagination;
import com.github.sanctum.labyrinth.library.TimeWatch;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaytimeCommand extends CommandBuilder {
	public PlaytimeCommand() {
		super(OptionLoader.TEST_COMMAND.from("playtime", "/playtime", "Check your amount of time played.", null, "pt", "timeplayed"));
	}

	@Override
	public @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		final long time = player.getFirstPlayed();

		if (args.length == 0) {
			TimeWatch.Recording total = TimeWatch.Recording.subtract(time);
			sendMessage(player, MessageFormat.format("&a{0} days of game time wow! Thats {1} hours {2} minutes & {3} seconds.", total.getDays(), total.getHours(), total.getMinutes(), total.getSeconds()));
		}

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("hours")) {

				sendMessage(player, "&aYou have played for a total of " + TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - time));

			}

			if (args[0].equalsIgnoreCase("top")) {
				EasyPagination<Player> pagination = new EasyPagination<>(player, Bukkit.getOnlinePlayers().stream().sorted((o1, o2) -> Long.compare(o2.getFirstPlayed(), o1.getFirstPlayed())).collect(Collectors.toList()));
				pagination.limit(6);
				pagination.setHeader((player1, message) -> {
					message.then("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				});
				pagination.setFormat((player1, integer, message) -> {
					message.then("&6#" + integer + " &e" + player1.getDisplayName());
				});
				pagination.setFooter((player1, message) -> {
					message.then("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				});
				sendMessage(player, "&6Most active players");
				pagination.send(1);
			}

		}
		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
		return true;
	}
}
