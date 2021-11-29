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
import com.github.sanctum.labyrinth.library.Entities;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SpawnMobCommand extends CommandBuilder {
	public SpawnMobCommand() {
		super(InternalCommandData.SPAWNMOB_COMMAND);
	}

	private final SimpleTabCompletion builder = SimpleTabCompletion.empty();
	private final List<String> arguments = new ArrayList<>();

	@Override
	public @NotNull
	List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {

		List<String> result = new ArrayList<>();
		if (args.length == 1) {
			arguments.clear();
			for (EntityType e : EntityType.values()) {
				arguments.add(e.name().toLowerCase());
			}
			for (String a : arguments) {
				String arg = args[0];
				if (arg.endsWith(",")) {
					int stop = arg.length() - 1;
					arg = arg.substring(0, stop);
					result.add(arg + "," + a);
				}
				int len = arg.length() - 1;
				if (len > 4) {
					if (a.toLowerCase().startsWith(arg.substring(arg.length() - 4).toLowerCase())) {
						int stop = arg.length() - 2;
						int stop2 = arg.length() - 4;
						arg = arg.substring(0, stop);
						result.add(arg.substring(0, stop2) + a);
					}
				}
				if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
					result.add(a);
				}

			}
			return result;
		}

		if (args.length == 2) {
			arguments.clear();
			for (EntityType e : EntityType.values()) {
				arguments.add(e.name().toLowerCase());
			}
			for (String a : arguments) {
				String arg = args[1];
				if (arg.endsWith(",")) {
					int stop = arg.length() - 1;
					arg = arg.substring(0, stop);
					result.add(arg + "," + a);
				}
				int len = arg.length() - 1;
				if (len > 4) {
					if (a.toLowerCase().startsWith(arg.substring(arg.length() - 4).toLowerCase())) {
						int stop = arg.length() - 2;
						int stop2 = arg.length() - 4;
						arg = arg.substring(0, stop);
						result.add(arg.substring(0, stop2) + a);
					}
				}
				if (a.toLowerCase().startsWith(args[1].toLowerCase())) {
					result.add(a);
				}

			}
			return result;
		}

		return builder.fillArgs(args)
				.then(TabCompletionIndex.THREE, "{POS X,Y,Z}")
				.get();
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {

			if (args.length == 0) {

			}

			if (args.length == 1) {
				String[] mobNames = args[0].split(",");
				List<Entity> list = new ArrayList<>();
				for (String name : mobNames) {
					EntityType type = Entities.getEntity(name);
					if (type != null) {
						list.add(player.getWorld().spawn(player.getLocation(), type.getEntityClass()));
					}
				}
				for (int i = 1; i < list.size(); i++) {
					list.get(i - 1).addPassenger(list.get(i));
				}
				sendMessage(player, "&aSuccessfully spawned " + 1 + " " + args[0]);
			}

			if (args.length == 2) {
				try {
					int amount = Integer.parseInt(args[0]);
					String[] mobNames = args[1].split(",");
					List<Entity> list = new ArrayList<>();
					for (int j = 0; j < amount; j++) {

						for (String name : mobNames) {
							EntityType type = Entities.getEntity(name);
							if (type != null) {
								list.add(player.getWorld().spawn(player.getLocation(), type.getEntityClass()));
							}
						}
						for (int i = 1; i < list.size(); i++) {
							list.get(i - 1).addPassenger(list.get(i));
						}
					}
					sendMessage(player, "&aSuccessfully spawned " + 1 + " " + args[1]);
				} catch (NumberFormatException e) {
					// TODO: message player invalid num
				}
			}
			if (args.length == 3) {
				try {
					int amount = Integer.parseInt(args[0]);
					String[] pos = args[2].split(",");
					Location toSpawn = new Location(player.getWorld(), Double.parseDouble(pos[0]), Double.parseDouble(pos[1]), Double.parseDouble(pos[2])).add(0.5, 0, 0.5);
					String[] mobNames = args[1].split(",");
					List<Entity> list = new ArrayList<>();
					for (int j = 0; j < amount; j++) {

						for (String name : mobNames) {
							EntityType type = Entities.getEntity(name);
							if (type != null) {
								list.add(player.getWorld().spawn(toSpawn, type.getEntityClass()));
							}
						}
						for (int i = 1; i < list.size(); i++) {
							list.get(i - 1).addPassenger(list.get(i));
						}
					}
					sendMessage(player, "&aSuccessfully spawned " + amount + " " + args[1] + " @ location x: " + pos[0] + " y: " + pos[1] + " z: " + pos[2]);
				} catch (Exception e) {
					// TODO: Potentially message player about outcome (invalid mob, etc)
				}
			}
		}
		return false;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}
}
