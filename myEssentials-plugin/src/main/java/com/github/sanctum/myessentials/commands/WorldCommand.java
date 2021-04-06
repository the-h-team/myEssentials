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

import com.github.sanctum.labyrinth.formatting.TabCompletion;
import com.github.sanctum.labyrinth.formatting.TabCompletionBuilder;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.sanctum.myessentials.util.ConfiguredMessage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class WorldCommand extends CommandBuilder {
	private final Map<UUID, Boolean> taskScheduled = new HashMap<>();
	private final TabCompletionBuilder builder = TabCompletion.build(getData().getLabel());
	private final AtomicReference<Location> teleportLocation = new AtomicReference<>();

	public WorldCommand() {
		super(InternalCommandData.WORLD_COMMAND);
	}

	@Override
	public @NotNull
	List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return builder
				.forArgs(args)
				.level(1)
				.completeAt(getData().getLabel())
				.filter(() -> Bukkit.getWorlds()
						.stream()
						.map(World::getName)
						.collect(Collectors.toList()))
				.collect()
				.get(1);
	}

	public int random(int bounds) {
		return (int) (Math.random() * bounds * (Math.random() > 0.5 ? 1 : -1));
	}

	/**
	 * Checks if a location is safe (solid ground with 2 breathable blocks)
	 *
	 * @param location Location to check
	 * @return True if location is safe
	 */
	public boolean hasSurface(Location location) {
		Block feet = location.getBlock();
		Block head = feet.getRelative(BlockFace.UP);
		if (!feet.getType().isAir() && !feet.getLocation().add(0, 1, 0).getBlock().getType().isAir() && !head.getType().isAir()) {
			return false; // not transparent (will suffocate)
		}
		return feet.getRelative(BlockFace.DOWN).getType().isSolid(); // not solid
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {

		if (testPermission(player)) {

			if (args.length == 0) {
				sendUsage(player);
				return true;
			}

			if (args.length == 1) {
				String world = args[0];

				if (Bukkit.getWorld(world) == null) {
					// world not found.
					return true;
				}
				if (taskScheduled.containsKey(player.getUniqueId()) && taskScheduled.get(player.getUniqueId())) {
					taskScheduled.put(player.getUniqueId(), false);
					sendMessage(player, ConfiguredMessage.STOPPING_SEARCH);
					return true;
				}
				taskScheduled.put(player.getUniqueId(), true);

				Schedule.sync(() -> {
					int x = random(10500);
					int z = random(3500);
					int y = 150;
					teleportLocation.set(new Location(Bukkit.getWorld(world), x, y, z));
					y = Objects.requireNonNull(teleportLocation.get().getWorld()).getHighestBlockYAt(teleportLocation.get());
					teleportLocation.get().setY(y);
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(color(MyEssentialsAPI.getInstance().getPrefix() + " Searching for suitable location...")));
					player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 10, 1);

				}).cancelAfter(player).cancelAfter(task -> {
					if (taskScheduled.containsKey(player.getUniqueId()) && !taskScheduled.get(player.getUniqueId())) {
						sendMessage(player, ConfiguredMessage.SEARCH_INTERRUPTED);
						task.cancel();
					}
					if (!taskScheduled.containsKey(player.getUniqueId())) {
						sendMessage(player, ConfiguredMessage.SEARCH_INTERRUPTED);
						task.cancel();
					}
					if (teleportLocation.get() != null) {
						if (hasSurface(teleportLocation.get())) {
							player.teleport(teleportLocation.get());
							teleportLocation.set(null);
							sendMessage(player, ConfiguredMessage.TELEPORTED_SAFEST_LOCATION.replace(world));
							taskScheduled.remove(player.getUniqueId());
							task.cancel();
						}
					}
				}).repeat(0, 3 * 20);

				return true;
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}
}
