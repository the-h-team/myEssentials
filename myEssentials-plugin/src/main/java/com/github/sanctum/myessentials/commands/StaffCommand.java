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

import com.github.sanctum.myessentials.api.AddonQuery;
import com.github.sanctum.myessentials.api.EssentialsAddon;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.gui.MenuList;
import com.github.sanctum.myessentials.util.permissions.PermissiveConnection;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StaffCommand extends CommandBuilder {
	public StaffCommand() {
		super(InternalCommandData.STAFF_COMMAND);
	}

	@Override
	public @Nullable
	List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return null;
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {

		if (args.length == 0) {
			// myPermissions enabled, get the data.
			if (PermissiveConnection.trusted()) {
				sendMessage(player, PermissiveConnection.getGroup(player) + " is your group and your weight is " + PermissiveConnection.getWeight(player));
				//If the rank priority is high enough let them open the menu.
				if (PermissiveConnection.getWeight(player) > 0) {
					MenuList.SingleMenu.ADDON_REGISTRATION.get().open(player);
					for (String a : AddonQuery.getEnabledAddons()) {
						EssentialsAddon addon = AddonQuery.find(a);
						sendMessage(player, addon.getAddonName());
						sendMessage(player, addon.getAddonDescription());
						sendMessage(player, "------------------");
						sendMessage(player, " ");
					}
				}

			}

			return true;
		}

		return false;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}
}
