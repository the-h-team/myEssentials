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
	}

	@Override
	public boolean playerView(@NotNull Player p, @NotNull String s, @NotNull String[] args) {
		if (args.length == 0) {
			if (!p.hasPermission(InternalCommandData.BACK_COMMAND.getPermissionNode())) {
				sendMessage(p, "&cYou don't have permission: &f" + InternalCommandData.BACK_COMMAND.getPermissionNode());
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
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
		return false;
	}
}
