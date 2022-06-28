package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.warp.Warp;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import java.util.List;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WarpCommand extends CommandOutput {
	public WarpCommand() {
		super(OptionLoader.TEST_COMMAND.from("warp", "/warp", "Go to an existing warp", "mess.warp"));
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {

		}

		if (args.length == 1) {
			if (player.hasPermission(getData().getPermissionNode() + "." + args[0])) {
				Warp warp = MyEssentialsAPI.getInstance().getWarp(args[0]);
				if (warp != null) {
					TaskScheduler.of(() -> {
						player.teleport(warp.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
						player.playSound(warp.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 1);
						sendMessage(player, "&6Teleported to &f" + warp.getName());
					}).schedule();
				} else {
					sendMessage(player, "&cWarp not found.");
				}
			} else {
				// no warp perm.
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
