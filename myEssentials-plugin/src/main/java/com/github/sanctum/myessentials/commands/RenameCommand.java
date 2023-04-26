package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.myessentials.model.CommandInput;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RenameCommand extends CommandInput {
	public RenameCommand() {
		super(OptionLoader.TEST_COMMAND.from("rename", "/rename", "Rename an item title.", "mess.staff.rename", "label", "itemname"));
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {
			if (args.length >= 1) {
				final ItemStack item = new ItemStack(player.getInventory().getItemInMainHand());
				if (!item.getType().isAir()) {
					StringBuilder builder = new StringBuilder();
					for (int i = 0; i < args.length; i++) {
						if (i == args.length - 1) {
							builder.append(args[i]);
						} else {
							builder.append(args[i]).append(" ");
						}
					}
					sendMessage(player, "&aHere you go!");
					player.getInventory().getItemInMainHand().setAmount(0);
					player.getInventory().addItem(Items.edit(edit -> edit.setItem(item).setTitle(builder.toString().trim()).build()));
				} else {
					sendMessage(player, "&cUnknown item type.");
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
