package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.CommandInput;
import com.github.sanctum.myessentials.model.warp.Warp;
import com.github.sanctum.myessentials.model.warp.WarpHolder;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeleteHomeCommand extends CommandInput {

	public DeleteHomeCommand() {
		super(OptionLoader.TEST_COMMAND.from("delhome", "/delhome", "Delete a home.", "mess.delhome"));
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, MyEssentialsAPI.getInstance().getWarpHolder(player).getAll().stream().map(Warp::getName).collect(Collectors.toList()))
				.get();
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {
			WarpHolder holder = MyEssentialsAPI.getInstance().getWarpHolder(player);
			if (args.length == 0) {

			}
			if (args.length == 1) {
				Warp test = holder.get(args[0]);
				if (test != null) {
					sendMessage(player, "&cDeleted home " + args[0]);
					holder.remove(test);
				} else {
					sendMessage(player, "&cHome not found.");
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
