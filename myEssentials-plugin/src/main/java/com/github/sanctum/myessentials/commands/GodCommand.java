package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GodCommand extends CommandBuilder {
	public GodCommand() {
		super(InternalCommandData.GOD_COMMAND);
	}

	@Override
	public @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {
			PlayerSearch search = PlayerSearch.look(player);
			if (args.length == 0) {
				if (search.isValid()) {
					if (search.isOnline()) {
						if (search.isInvincible()) {
							sendMessage(player, ConfiguredMessage.GOD_DISABLED);
							search.setInvincible(false);
						} else {
							sendMessage(player, ConfiguredMessage.GOD_ENABLED);
							search.getPlayer().getNearbyEntities(20, 20, 20).forEach(entity -> {
								if (entity instanceof Monster) {
									Monster m = (Monster) entity;
									m.setTarget(null);
								}
							});
							search.setInvincible(true);
						}
					}
				}
			}
			if (args.length == 1) {
				PlayerSearch target = PlayerSearch.look(args[0]);
				if (target.isValid()) {
					if (target.isOnline()) {
						if (target.isInvincible()) {
							sendMessage(player, ConfiguredMessage.GOD_DISABLED_OTHER.replace(target.getOfflinePlayer().getName()));
							target.setInvincible(false);
						} else {
							sendMessage(player, ConfiguredMessage.GOD_ENABLED_OTHER.replace(target.getOfflinePlayer().getName()));
							target.setInvincible(true);
							target.getPlayer().getNearbyEntities(20, 20, 20).forEach(entity -> {
								if (entity instanceof Monster) {
									Monster m = (Monster) entity;
									m.setTarget(null);
								}
							});
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 1) {
			PlayerSearch target = PlayerSearch.look(args[0]);
			if (target.isValid()) {
				if (target.isOnline()) {
					if (target.isInvincible()) {
						sendMessage(sender, ConfiguredMessage.GOD_DISABLED_OTHER.replace(target.getOfflinePlayer().getName()));
						target.setInvincible(false);
					} else {
						sendMessage(sender, ConfiguredMessage.GOD_ENABLED_OTHER.replace(target.getOfflinePlayer().getName()));
						target.setInvincible(true);
						target.getPlayer().getNearbyEntities(20, 20, 20).forEach(entity -> {
							if (entity instanceof Monster) {
								Monster m = (Monster) entity;
								m.setTarget(null);
							}
						});
					}
				}
			}
			return true;
		}
		return false;
	}
}
