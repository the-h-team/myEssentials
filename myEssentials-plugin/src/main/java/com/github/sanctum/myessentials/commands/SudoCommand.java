package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.util.OptionLoader;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SudoCommand extends CommandBuilder {
	public SudoCommand() {
		super(OptionLoader.TEST_COMMAND.from("sudo", "/sudo", "Make someone perform a command.", "mess.staff.sudo", "s", "make"));
	}

	@Override
	public @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {
			if (args.length > 1) {
				PlayerSearch search = PlayerSearch.look(args[0]);

				StringBuilder builder1 = new StringBuilder();
				for (int j = 1; j < args.length; j++) {
					builder1.append(args[j]).append(" ");
				}

				if (search.isValid()) {
					if (search.isOnline()) {
						search.getPlayer().performCommand(builder1.toString().trim());
						sendMessage(player, "&aMaking target perform &7: &r" + builder1.toString().trim());
					}
				}

			}
		}
		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}
}
