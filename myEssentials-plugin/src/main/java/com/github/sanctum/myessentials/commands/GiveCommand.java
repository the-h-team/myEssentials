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
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GiveCommand extends CommandOutput {
	public GiveCommand() {
		super(InternalCommandData.GIVE_COMMAND);
	}

	@Override
	public @Nullable
	List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()))
				.then(TabCompletionIndex.TWO, Arrays.stream(Material.values()).map(material -> material.name().toLowerCase(Locale.ROOT).replace("_", "")).collect(Collectors.toList()))
				.then(TabCompletionIndex.THREE, Arrays.stream(Material.values()).map(material -> material.name().toLowerCase(Locale.ROOT).replace("_", "")).collect(Collectors.toList()))
				.get();
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {
			if (args.length == 0) {
				sendUsage(player);
				return true;
			}
			PlayerSearch search = PlayerSearch.look(args[0]);
			if (args.length == 1) {
				if (search.isValid()) {

					if (search.isOnline()) {
						sendUsage(player);
					} else {
						sendMessage(player, ConfiguredMessage.PLAYER_MUST_BE_ONLINE.replace(args[0]));
					}

				} else {
					sendMessage(player, ConfiguredMessage.PLAYER_NOT_FOUND.replace(args[0]));
				}
				return true;
			}

			if (args.length == 2) {
				if (search.isValid()) {

					if (search.isOnline()) {

						Material result = Items.findMaterial(args[1]);

						if (result != null && !result.isAir()) {
							ItemStack item = new ItemStack(result, 1);
							search.getPlayer().getWorld().dropItem(search.getPlayer().getEyeLocation(), item);
							sendMessage(player, "&aTarget &e" + search.getPlayer().getName() + " &agiven &6x" + 1 + " &f" + args[1]);
							sendMessage(search.getPlayer(), "&aYou've been given &6x" + 1 + " &f" + args[1]);
						} else {
							sendMessage(player, "&cMaterial '" + args[1] + "' unknown.");
						}

					} else {
						sendMessage(player, ConfiguredMessage.PLAYER_MUST_BE_ONLINE.replace(args[0]));
					}

				} else {
					sendMessage(player, ConfiguredMessage.PLAYER_NOT_FOUND.replace(args[0]));
				}
				return true;
			}

			if (args.length == 3) {
				if (search.isValid()) {

					if (search.isOnline()) {

						Material result = Items.findMaterial(args[2]);

						if (result != null && !result.isAir()) {

							if (StringUtils.use(args[1]).isInt()) {
								ItemStack item = new ItemStack(result, Integer.parseInt(args[1]));
								search.getPlayer().getWorld().dropItem(search.getPlayer().getEyeLocation(), item);
								sendMessage(player, "&aTarget &e" + search.getPlayer().getName() + " &agiven &6x" + args[1] + " &f" + args[2]);
								sendMessage(search.getPlayer(), "&aYou've been given &6x" + args[1] + " &f" + args[2]);
							} else {
								sendMessage(player, "&cInvalid amount specified!");
							}

						} else {
							sendMessage(player, "&cMaterial '" + args[2] + "' unknown.");
						}

					} else {
						sendMessage(player, ConfiguredMessage.PLAYER_MUST_BE_ONLINE.replace(args[0]));
					}

				} else {
					sendMessage(player, ConfiguredMessage.PLAYER_NOT_FOUND.replace(args[0]));
				}
				return true;
			}

		}
		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender player, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {
			sendUsage(player);
			return true;
		}
		PlayerSearch search = PlayerSearch.look(args[0]);
		if (args.length == 1) {
			if (search.isValid()) {

				if (search.isOnline()) {
					sendUsage(player);
				} else {
					sendMessage(player, ConfiguredMessage.PLAYER_MUST_BE_ONLINE.replace(args[0]));
				}

			} else {
				sendMessage(player, ConfiguredMessage.PLAYER_NOT_FOUND.replace(args[0]));
			}
			return true;
		}

		if (args.length == 2) {
			if (search.isValid()) {

				if (search.isOnline()) {

					Material result = Items.findMaterial(args[1]);

					if (result != null && !result.isAir()) {
						ItemStack item = new ItemStack(result, 1);
						search.getPlayer().getWorld().dropItem(search.getPlayer().getEyeLocation(), item);
						sendMessage(player, "&aTarget &e" + search.getPlayer().getName() + " &agiven &6x" + 1 + " &f" + args[1]);
						sendMessage(search.getPlayer(), "&aYou've been given &6x" + 1 + " &f" + args[1]);
					} else {
						sendMessage(player, "&cMaterial '" + args[1] + "' unknown.");
					}

				} else {
					sendMessage(player, ConfiguredMessage.PLAYER_MUST_BE_ONLINE.replace(args[0]));
				}

			} else {
				sendMessage(player, ConfiguredMessage.PLAYER_NOT_FOUND.replace(args[0]));
			}
			return true;
		}

		if (args.length == 3) {
			if (search.isValid()) {

				if (search.isOnline()) {

					Material result = Items.findMaterial(args[2]);

					if (result != null && !result.isAir()) {

						if (StringUtils.use(args[1]).isInt()) {
							ItemStack item = new ItemStack(result, Integer.parseInt(args[1]));
							search.getPlayer().getWorld().dropItem(search.getPlayer().getEyeLocation(), item);
							sendMessage(player, "&aTarget &e" + search.getPlayer().getName() + " &agiven &6x" + args[1] + " &f" + args[2]);
							sendMessage(search.getPlayer(), "&aYou've been given &6x" + args[1] + " &f" + args[2]);
						} else {
							sendMessage(player, "&cInvalid amount specified!");
						}

					} else {
						sendMessage(player, "&cMaterial '" + args[2] + "' unknown.");
					}

				} else {
					sendMessage(player, ConfiguredMessage.PLAYER_MUST_BE_ONLINE.replace(args[0]));
				}

			} else {
				sendMessage(player, ConfiguredMessage.PLAYER_NOT_FOUND.replace(args[0]));
			}
			return true;
		}
		return true;
	}
}
