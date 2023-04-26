package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.CommandInput;
import com.github.sanctum.myessentials.model.warp.Warp;
import com.github.sanctum.myessentials.model.warp.WarpHolder;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HomeCommand extends CommandInput {

	public HomeCommand() {
		super(OptionLoader.TEST_COMMAND.from("home", "/home", "Warp to a home.", "mess.home"));
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, MyEssentialsAPI.getInstance().getWarpHolder(player).getAll().stream().map(Warp::getName).collect(Collectors.toList()))
				.get();
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		WarpHolder holder = MyEssentialsAPI.getInstance().getWarpHolder(player);
		if (args.length == 0) {
			sendMessage(player, "&cInvalid usage expected a warp name.");
		}
		if (args.length == 1) {
			Warp warp = holder.get(args[0]);
			if (warp != null) {
				TaskScheduler.of(() -> {
					player.teleport(warp.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
					player.playSound(warp.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 1);
					sendMessage(player, "&6Teleported to &f" + warp.getName());
				}).schedule();
			} else {
				sendMessage(player, "&cHome " + args[0] + " doesn't exist.");
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
