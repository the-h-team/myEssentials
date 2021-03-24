package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.api.AddonQuery;
import com.github.sanctum.myessentials.api.EssentialsAddon;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.gui.MenuList;
import com.github.sanctum.myessentials.util.permissions.PermissiveConnection;
import java.util.Map;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class StaffCommand extends CommandBuilder {
	public StaffCommand() {
		super(InternalCommandData.STAFF_COMMAND);
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String s, @NotNull String[] strings) {

		if (strings.length == 0) {
			// myPermissions enabled, get the data.
			if (PermissiveConnection.trusted()) {
				sendMessage(player, PermissiveConnection.getGroup(player) + " is your group and your weight is " + PermissiveConnection.getWeight(player));
				// If the rank priority is high enough let them open the menu.
				if (PermissiveConnection.getWeight(player) > 0) {
					MenuList.SingleMenu.ADDON_REGISTRATION.get().open(player);
				}

			}

			return true;
		}

		return false;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
		return false;
	}
}
