package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.myessentials.model.CommandInput;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.events.PlayerPendingHealEvent;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HealCommand extends CommandInput {
	public HealCommand() {
		super(InternalCommandData.HEAL_COMMAND);
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return SimpleTabCompletion.of(args).then(TabCompletionIndex.ONE, Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()))
				.get();
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {
			if (testPermission(player)) {
				Bukkit.getPluginManager().callEvent(new PlayerPendingHealEvent(null, player, 20));
				return true;
			}
			return true;
		}

		if (args.length == 1) {
			PlayerSearch search = PlayerSearch.look(args[0]);
			if (search.isValid()) {
				if (search.isOnline()) {
					Player target = search.getPlayer();
					if (testPermission(player)) {
						assert target != null;
						search.heal(player, 20);
						sendMessage(player, ConfiguredMessage.HEAL_TARGET_MAXED.replace(target.getName()));
					}
				} else {
					if (testPermission(player)) {
						sendMessage(player, ConfiguredMessage.HEAL_TARGET_NOT_ONLINE.replace(search.getOfflinePlayer().getName()));
					}
				}
			} else {
				if (testPermission(player)) {
					sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
				}
			}
		}
		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 1) {
			PlayerSearch search = PlayerSearch.look(args[0]);
			if (search.isValid()) {
				if (search.isOnline()) {
					Player target = search.getPlayer();
					if (testPermission(sender)) {
						assert target != null;
						search.heal(sender, 20);
						sendMessage(sender, ConfiguredMessage.HEAL_TARGET_MAXED.replace(target.getName()));
					}
				} else {
					if (testPermission(sender)) {
						sendMessage(sender, ConfiguredMessage.HEAL_TARGET_NOT_ONLINE.replace(search.getOfflinePlayer().getName()));
					}
				}
			} else {
				if (testPermission(sender)) {
					sendMessage(sender, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
				}
			}
		}
		return true;
	}
}
