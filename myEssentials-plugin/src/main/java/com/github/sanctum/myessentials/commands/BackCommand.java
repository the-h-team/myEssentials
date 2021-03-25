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

import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class BackCommand extends CommandBuilder {
	public BackCommand() {
		super(InternalCommandData.BACK_COMMAND);
		command.setPermissionMessage(color("&cYou don't have permission: &f" + getData().getPermissionNode()));
	}

	@Override
	public boolean playerView(@NotNull Player p, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {
			if (!testPermission(p)) {
				return true;
			}
			Location previous = MyEssentialsAPI.getInstance().getPreviousLocation(p.getUniqueId());
			if (previous == null) {
				sendMessage(p, "&cNo previous location was found.");
				return true;
			}
			p.teleport(previous);
			sendMessage(p, "&aTeleporting to your previous location.");
			return true;
		}

		return false;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}
}
