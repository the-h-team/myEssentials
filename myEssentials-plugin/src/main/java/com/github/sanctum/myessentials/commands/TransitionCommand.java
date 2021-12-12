package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransitionCommand extends CommandBuilder {

	private static int i;
	private static boolean sent;

	public TransitionCommand() {
		super(InternalCommandData.TRANSITION_COMMAND);
	}

	private static boolean canStop(World w, int start, int stop) {
		int time = (int) w.getTime();
		return time <= start || time >= stop;
	}


	@Override
	public @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return SimpleTabCompletion.of(args).then(TabCompletionIndex.ONE, () -> {
			if (player.hasPermission(getData().getPermissionNode())) {
				return Arrays.asList("day", "night");
			}
			return new ArrayList<>();
		}).get();
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {

		if (testPermission(player)) {
			if (args.length == 0) {
				if (sent) {
					sendMessage(player, ConfiguredMessage.TRANSITION_IN_PROGRESS);
					return true;
				}
				if (canStop(player.getWorld(), 13000, 24000)) {
					sendMessage(player, ConfiguredMessage.ALREADY_DAY);
					return true;
				}
				Schedule.sync(() -> {
					if (!canStop(player.getWorld(), 13000, 24000)) {
						if (!sent) {
							i = (int) player.getWorld().getTime();
							sent = true;
						}
						player.getWorld().setTime(i);
						i += 20;
					} else {
						sendMessage(player, ConfiguredMessage.SET_DAY);
					}
				}).cancelAfter(task -> {
					if (canStop(player.getWorld(), 13000, 24000)) {
						i = 0;
						sent = false;
						task.cancel();
					}
				}).repeat(0, 1);
				return true;
			}
			if (args.length == 1) {
				switch (args[0].toLowerCase()) {
					case "day":
						if (canStop(player.getWorld(), 13000, 24000)) {
							sendMessage(player, ConfiguredMessage.ALREADY_DAY);
							return true;
						}
						if (sent) {
							sendMessage(player, ConfiguredMessage.TRANSITION_IN_PROGRESS);
							return true;
						}
						Schedule.sync(() -> {
							if (!canStop(player.getWorld(), 13000, 24000)) {
								if (!sent) {
									i = (int) player.getWorld().getTime();
									sent = true;
								}
								player.getWorld().setTime(i);
								i += 20;
							} else {
								sendMessage(player, ConfiguredMessage.SET_DAY);
							}
						}).cancelAfter(task -> {
							if (canStop(player.getWorld(), 13000, 24000)) {
								i = 0;
								sent = false;
								task.cancel();
							}
						}).repeat(0, 1);
						break;
					case "night":
						if (canStop(player.getWorld(), 0, 13000)) {
							sendMessage(player, ConfiguredMessage.ALREADY_NIGHT);
							return true;
						}
						if (sent) {
							sendMessage(player, ConfiguredMessage.TRANSITION_IN_PROGRESS);
							return true;
						}
						Schedule.sync(() -> {
							if (!canStop(player.getWorld(), 0, 13000)) {
								if (!sent) {
									i = (int) player.getWorld().getTime();
									sent = true;
								}
								player.getWorld().setTime(i);
								i += 20;
							} else {
								sendMessage(player, ConfiguredMessage.SET_NIGHT);
							}
						}).cancelAfter(task -> {
							if (canStop(player.getWorld(), 0, 13000)) {
								i = 0;
								sent = false;
								task.cancel();
							}
						}).repeat(0, 1);
						break;
				}
			}
			if (args.length == 2) {
				try {
					Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					sendMessage(player, "&c" + e.getMessage());
					return true;
				}
				if (sent) {
					sendMessage(player, ConfiguredMessage.TRANSITION_IN_PROGRESS);
					return true;
				}
				if (Integer.parseInt(args[1]) > 500) {
					sendMessage(player, ConfiguredMessage.TRANSITION_TOO_FAST);
					return true;
				}
				switch (args[0].toLowerCase()) {
					case "day":
						if (canStop(player.getWorld(), 13000, 24000)) {
							sendMessage(player, ConfiguredMessage.ALREADY_DAY);
							return true;
						}
						Schedule.sync(() -> {
							if (!canStop(player.getWorld(), 13000, 24000)) {
								if (!sent) {
									i = (int) player.getWorld().getTime();
									sent = true;
								}
								player.getWorld().setTime(i);
								i += 20 + Integer.parseInt(args[1]);
							} else {
								sendMessage(player, ConfiguredMessage.SET_DAY);
							}
						}).cancelAfter(task -> {
							if (canStop(player.getWorld(), 13000, 24000)) {
								i = 0;
								sent = false;
								task.cancel();
							}
						}).repeat(0, 1);
						break;
					case "night":
						Schedule.sync(() -> {
							if (canStop(player.getWorld(), 0, 13000)) {
								sendMessage(player, ConfiguredMessage.ALREADY_NIGHT);
								return;
							}
							if (!canStop(player.getWorld(), 0, 13000)) {
								if (!sent) {
									i = (int) player.getWorld().getTime();
									sent = true;
								}
								player.getWorld().setTime(i);
								i += 20 + Integer.parseInt(args[1]);
							} else {
								sendMessage(player, ConfiguredMessage.SET_NIGHT);
							}
						}).cancelAfter(task -> {
							if (canStop(player.getWorld(), 0, 13000)) {
								i = 0;
								sent = false;
								task.cancel();
							}
						}).repeat(0, 1);
						break;
				}
			}
		}
		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(sender)) {
			if (args.length == 0) {
				if (sent) {
					sendMessage(sender, ConfiguredMessage.TRANSITION_IN_PROGRESS);
					return true;
				}
				if (canStop(Bukkit.getWorlds().get(0), 13000, 24000)) {
					sendMessage(sender, ConfiguredMessage.ALREADY_DAY);
					return true;
				}
				Schedule.sync(() -> {
					if (!canStop(Bukkit.getWorlds().get(0), 13000, 24000)) {
						if (!sent) {
							i = (int) Bukkit.getWorlds().get(0).getTime();
							sent = true;
						}
						Bukkit.getWorlds().get(0).setTime(i);
						i += 20;
					} else {
						sendMessage(sender, ConfiguredMessage.SET_DAY);
					}
				}).cancelAfter(task -> {
					if (canStop(Bukkit.getWorlds().get(0), 13000, 24000)) {
						i = 0;
						sent = false;
						task.cancel();
					}
				}).repeat(0, 1);
				return true;
			}
			if (args.length == 1) {
				switch (args[0].toLowerCase()) {
					case "day":
						if (canStop(Bukkit.getWorlds().get(0), 13000, 24000)) {
							sendMessage(sender, ConfiguredMessage.ALREADY_DAY);
							return true;
						}
						if (sent) {
							sendMessage(sender, ConfiguredMessage.TRANSITION_IN_PROGRESS);
							return true;
						}
						Schedule.sync(() -> {
							if (!canStop(Bukkit.getWorlds().get(0), 13000, 24000)) {
								if (!sent) {
									i = (int) Bukkit.getWorlds().get(0).getTime();
									sent = true;
								}
								Bukkit.getWorlds().get(0).setTime(i);
								i += 20;
							} else {
								sendMessage(sender, ConfiguredMessage.SET_DAY);
							}
						}).cancelAfter(task -> {
							if (canStop(Bukkit.getWorlds().get(0), 13000, 24000)) {
								i = 0;
								sent = false;
								task.cancel();
							}
						}).repeat(0, 1);
						break;
					case "night":
						if (canStop(Bukkit.getWorlds().get(0), 0, 13000)) {
							sendMessage(sender, ConfiguredMessage.ALREADY_NIGHT);
							return true;
						}
						if (sent) {
							sendMessage(sender, ConfiguredMessage.TRANSITION_IN_PROGRESS);
							return true;
						}
						Schedule.sync(() -> {
							if (!canStop(Bukkit.getWorlds().get(0), 0, 13000)) {
								if (!sent) {
									i = (int) Bukkit.getWorlds().get(0).getTime();
									sent = true;
								}
								Bukkit.getWorlds().get(0).setTime(i);
								i += 20;
							} else {
								sendMessage(sender, ConfiguredMessage.SET_NIGHT);
							}
						}).cancelAfter(task -> {
							if (canStop(Bukkit.getWorlds().get(0), 0, 13000)) {
								i = 0;
								sent = false;
								task.cancel();
							}
						}).repeat(0, 1);
						break;
				}
			}
			if (args.length == 2) {
				try {
					Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					sendMessage(sender, "&c" + e.getMessage());
					return true;
				}
				if (sent) {
					sendMessage(sender, ConfiguredMessage.TRANSITION_IN_PROGRESS);
					return true;
				}
				if (Integer.parseInt(args[1]) > 500) {
					sendMessage(sender, ConfiguredMessage.TRANSITION_TOO_FAST);
					return true;
				}
				switch (args[0].toLowerCase()) {
					case "day":
						if (canStop(Bukkit.getWorlds().get(0), 13000, 24000)) {
							sendMessage(sender, ConfiguredMessage.ALREADY_DAY);
							return true;
						}
						Schedule.sync(() -> {
							if (!canStop(Bukkit.getWorlds().get(0), 13000, 24000)) {
								if (!sent) {
									i = (int) Bukkit.getWorlds().get(0).getTime();
									sent = true;
								}
								Bukkit.getWorlds().get(0).setTime(i);
								i += 20 + Integer.parseInt(args[1]);
							} else {
								sendMessage(sender, ConfiguredMessage.SET_DAY);
							}
						}).cancelAfter(task -> {
							if (canStop(Bukkit.getWorlds().get(0), 13000, 24000)) {
								i = 0;
								sent = false;
								task.cancel();
							}
						}).repeat(0, 1);
						break;
					case "night":
						Schedule.sync(() -> {
							if (canStop(Bukkit.getWorlds().get(0), 0, 13000)) {
								sendMessage(sender, ConfiguredMessage.ALREADY_NIGHT);
								return;
							}
							if (!canStop(Bukkit.getWorlds().get(0), 0, 13000)) {
								if (!sent) {
									i = (int) Bukkit.getWorlds().get(0).getTime();
									sent = true;
								}
								Bukkit.getWorlds().get(0).setTime(i);
								i += 20 + Integer.parseInt(args[1]);
							} else {
								sendMessage(sender, ConfiguredMessage.SET_NIGHT);
							}
						}).cancelAfter(task -> {
							if (canStop(Bukkit.getWorlds().get(0), 0, 13000)) {
								i = 0;
								sent = false;
								task.cancel();
							}
						}).repeat(0, 1);
						break;
				}
			}
		}
		return true;
	}
}
