package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import com.github.sanctum.skulls.CustomHead;
import com.github.sanctum.skulls.CustomHeadLoader;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HeadCommand extends CommandBuilder {
	public HeadCommand() {
		super(OptionLoader.TEST_COMMAND.from("head", "/head", "Get ANY ones head.", "mess.staff.head", "playerhead"));
	}

	@Override
	public @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {
			if (args.length == 1) {

				ItemStack item = CustomHead.Manager.get(args[0]);

				if (item != null) {
					player.getWorld().dropItem(player.getLocation(), item);
				} else {
					sendMessage(player, "&cThe head could not be found, searching based off value...");
					ItemStack test = CustomHeadLoader.provide(args[0]);
					player.getWorld().dropItem(player.getLocation(), test);
				}
				sendMessage(player, "&aHere you go!");

			}
		}
		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
		return true;
	}
}
