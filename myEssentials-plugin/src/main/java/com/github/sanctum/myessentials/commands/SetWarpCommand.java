package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.CommandInput;
import com.github.sanctum.myessentials.model.warp.DefaultWarp;
import com.github.sanctum.myessentials.model.warp.Warp;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetWarpCommand extends CommandInput {

	public SetWarpCommand() {
		super(OptionLoader.TEST_COMMAND.from("setwarp", "/setwarp", "Set a warp.", "mess.setwarp"));
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
				if (test == null) {
					Warp w = new DefaultWarp(null, args[0], player.getLocation());
					MyEssentialsAPI.getInstance().loadWarp(w);
					sendMessage(player, "&aWarp " + args[0] + " set.");
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
