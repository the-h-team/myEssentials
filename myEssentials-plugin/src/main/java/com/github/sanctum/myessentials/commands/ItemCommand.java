/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials, a derivative work inspired by the
 *  Essentials <http://ess3.net/> and EssentialsX <https://essentialsx.net/>
 *  projects, both licensed under the GPLv3.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.myessentials.model.CommandInput;
import com.github.sanctum.myessentials.model.InternalCommandData;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ItemCommand extends CommandInput {
	public ItemCommand() {
		super(InternalCommandData.ITEM_COMMAND);
	}

	private final SimpleTabCompletion builder = SimpleTabCompletion.empty();

	@Override
	public @NotNull
	List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return builder.fillArgs(args)
				.then(TabCompletionIndex.ONE, Arrays.stream(Material.values()).map(Enum::name).map(s -> s.toLowerCase().replace("_", "")).collect(Collectors.toList()))
				.then(TabCompletionIndex.TWO, Arrays.stream(Material.values()).map(Enum::name).map(s -> s.toLowerCase().replace("_", "")).collect(Collectors.toList()))
				.get();

	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {

		if (testPermission(player)) {

			if (args.length == 0) {
				sendUsage(player);
			}

			if (args.length == 1) {
				Material mat = Items.findMaterial(args[0]);
				if (mat != null) {
					ItemStack i = new ItemStack(mat);
					player.getWorld().dropItem(player.getLocation(), i);
					sendMessage(player, "&aYou received " + "1 " + args[0]);
				} else {
					sendMessage(player, "&cThis item doesnt exist!");
				}
			}

			if (args.length == 2) {
				try {
					int amount = Integer.parseInt(args[0]);
					Material mat = Items.findMaterial(args[1]);
					if (mat != null) {
						ItemStack item = new ItemStack(mat);
						for (int i = 0; i < amount; i++) {
							player.getWorld().dropItem(player.getLocation(), item);
						}
						sendMessage(player, "&aYou received " + amount + " " + args[1]);
					} else {
						sendMessage(player, "&cThis item doesnt exist!");
					}
				} catch (NumberFormatException e) {
					try {
						int amount = Integer.parseInt(args[1]);
						Material mat = Items.findMaterial(args[0]);
						if (mat != null) {
							ItemStack item = new ItemStack(mat);
							for (int i = 0; i < amount; i++) {
								player.getWorld().dropItem(player.getLocation(), item);
							}
							sendMessage(player, "&aYou received " + amount + " " + args[0]);
						} else {
							sendMessage(player, "&cThis item doesnt exist!");
						}
					} catch (NumberFormatException ex) {

					}
				}
			}


		}

		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}
}
