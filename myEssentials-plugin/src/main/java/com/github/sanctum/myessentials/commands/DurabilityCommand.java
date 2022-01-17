package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.data.service.PlayerSearch;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.util.OptionLoader;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DurabilityCommand extends CommandBuilder {
	public DurabilityCommand() {
		super(OptionLoader.TEST_COMMAND.from("durability", "/durability", "Adjust the durability of the item you hold.", "mess.staff.durability", "damageitem"));
	}

	@Override
	public @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {

			if (args.length == 1) {

				if (!StringUtils.use(args[0]).isInt()) {
					sendMessage(player, "&cInvalid durability amount!");
					return true;
				}

				int amount = Integer.parseInt(args[0]);

				ItemStack item = player.getInventory().getItemInMainHand();

				if (item.getType() != Material.AIR) {
					ItemMeta theMeta = item.getItemMeta();
					Damageable damageable = (Damageable) theMeta;
					if (!damageable.hasDamage()) {
						sendMessage(player, "&cItem already fully repaired.");
						return true;
					}
					int toSet = damageable.getDamage() + amount;
					int actual = Math.min(Math.max(0, toSet), item.getType().getMaxDurability());
					damageable.setDamage(actual);
					item.setItemMeta(theMeta);
					if (actual == item.getType().getMaxDurability()) {
						item.setType(Material.AIR);
					}
					sendMessage(player, "&5Item durability adjusted.");
				} else {
					sendMessage(player, "&cCannot repair air...");
				}
			}
			if (args.length == 2) {
				if (args[1].equalsIgnoreCase("-s")) {
					if (!StringUtils.use(args[0]).isInt()) {
						sendMessage(Bukkit.getConsoleSender(), "&cInvalid durability amount!");
						return true;
					}

					int amount = Integer.parseInt(args[0]);

					ItemStack item = player.getInventory().getItemInMainHand();

					if (item.getType() != Material.AIR) {
						ItemMeta theMeta = item.getItemMeta();
						Damageable damageable = (Damageable) theMeta;
						int toSet = damageable.getDamage() + amount;
						int actual = Math.min(Math.max(0, toSet), item.getType().getMaxDurability());
						damageable.setDamage(actual);
						item.setItemMeta(theMeta);
						if (actual == item.getType().getMaxDurability()) {
							item.setType(Material.AIR);
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 2) {

			if (!StringUtils.use(args[1]).isInt()) {
				sendMessage(sender, "&cInvalid durability amount!");
				return true;
			}

			PlayerSearch player = PlayerSearch.of(args[0]);

			if (player == null || !player.getPlayer().isOnline()) {
				sendMessage(sender, "&cThis player was not found!");
				return true;
			}

			int amount = Integer.parseInt(args[1]);

			ItemStack item = player.getPlayer().getPlayer().getInventory().getItemInMainHand();

			if (item.getType() != Material.AIR) {
				ItemMeta theMeta = item.getItemMeta();
				Damageable damageable = (Damageable) theMeta;
				if (!damageable.hasDamage()) {
					sendMessage(sender, "&cItem already fully repaired.");
					return true;
				}
				int toSet = damageable.getDamage() + amount;
				int actual = Math.min(Math.max(0, toSet), item.getType().getMaxDurability());
				damageable.setDamage(actual);
				item.setItemMeta(theMeta);
				if (actual == item.getType().getMaxDurability()) {
					item.setType(Material.AIR);
				}
				sendMessage(sender, "&5Item durability adjusted for " + player.getName());
			} else {
				sendMessage(sender, "&cCannot repair air...");
			}
		}
		return true;
	}
}
