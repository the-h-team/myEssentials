package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.warp.Warp;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeleteWarpCommand extends CommandOutput {

	public DeleteWarpCommand() {
		super(OptionLoader.TEST_COMMAND.from("delwarp", "/delwarp", "Delete a warp.", "mess.delwarp"));
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {
			if (args.length == 0) {

			}
			if (args.length == 1) {
				Warp test = MyEssentialsAPI.getInstance().getWarp(args[0]);
				if (test != null) {
					MyEssentialsAPI.getInstance().unloadWarp(test);
					sendMessage(player, "&cWarp " + args[0] + " deleted.");
				} else {
					sendMessage(player, "&cWarp not found.");
				}
			}
		}
		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
		return true;
	}
}
