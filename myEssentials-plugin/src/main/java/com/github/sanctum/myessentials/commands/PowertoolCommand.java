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

import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.labyrinth.library.NamespacedKey;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PowertoolCommand extends CommandBuilder {
	public PowertoolCommand() {
		super(InternalCommandData.POWERTOOL_COMMAND);
	}


	public static final NamespacedKey KEY = new NamespacedKey(Essentials.getInstance(), "power_tool");

	@Override
	public @Nullable
	List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return null;
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {

		if (testPermission(player)) {

			StringBuilder builder = new StringBuilder();

			for (String arg : args) {
				builder.append(arg).append(" ");
			}
			String result = builder.toString().trim();
			ItemStack hand = player.getInventory().getItemInMainHand();

			ItemStack wand = new ItemStack(hand.getType());
			ItemMeta meta = wand.getItemMeta();
			if (meta != null) {
				//meta.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, result);
				meta.setDisplayName(StringUtils.use("&7[Powertool] &f/&6" + result).translate());
				meta.setLore(Collections.singletonList(StringUtils.use("Left-click to use the designated command.").translate()));
				wand.setItemMeta(meta);
				hand.setAmount(0);
				player.getInventory().addItem(wand);
			}
		}

		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return true;
	}
}
