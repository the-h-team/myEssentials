package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.CommandInput;
import com.github.sanctum.myessentials.model.warp.DefaultWarp;
import com.github.sanctum.myessentials.model.warp.Warp;
import com.github.sanctum.myessentials.model.warp.WarpHolder;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetHomeCommand extends CommandInput {

	public SetHomeCommand() {
		super(OptionLoader.TEST_COMMAND.from("sethome", "/sethome", "Set a home.", "mess.sethome"));
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {
			WarpHolder holder = MyEssentialsAPI.getInstance().getWarpHolder(player);
			if (args.length == 0) {

			}
			if (args.length == 1) {
				Warp test = holder.get(args[0]);
				if (test == null) {
					holder.add(new DefaultWarp(player, args[0], player.getLocation()));
					sendMessage(player, "&aSet home " + args[0]);
				} else {
					new FancyMessage("&6Home " + args[0] + " already exists, would you like to update it? ").then("&f[").then("&2Yes?").hover("&eClick to confirm.").action(() -> {
						holder.remove(test);
						player.performCommand("sethome " + args[0]);
					}).then("&f]").send(player).queue();
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
