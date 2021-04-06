package com.github.sanctum.myessentials;

import com.github.sanctum.labyrinth.formatting.TabCompletion;
import com.github.sanctum.labyrinth.formatting.TabCompletionBuilder;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.CommandMapper;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.moderation.KickReason;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class Commands {

	private static TabCompletionBuilder kickTab;
	private static TabCompletionBuilder kickTabAll;
	private static TabCompletionBuilder banTab;
	private static TabCompletionBuilder tempBanTab;
	private static TabCompletionBuilder unbanTab;
	private static int i;
	private static boolean sent;

	// Utility class
	private Commands() {
	}

	private static boolean canStop(World w, int start, int stop) {
		int time = (int) w.getTime();
		return time <= start || time >= stop;
	}

	protected static void register() {
		CommandMapper.from(InternalCommandData.FLY_COMMAND)
				.apply((builder, player, commandLabel, args) -> {
					if (!builder.testPermission(player)) {
						return;
					}
					if (args.length == 0) {
						if (player.getGameMode() != GameMode.SURVIVAL) {
							builder.sendMessage(player, ConfiguredMessage.TRY_IN_SURVIVAL);
							return;
						}
						if (player.getAllowFlight()) {
							player.setFlying(false);
							player.setAllowFlight(false);
							builder.sendMessage(player, ConfiguredMessage.FLIGHT_OFF);
							final Listener listener = new Listener() {
								@EventHandler
								public void onNextFallDamage(EntityDamageEvent e) {
									if (e.getEntityType() != EntityType.PLAYER) {
										return;
									}
									if (e.getCause() != EntityDamageEvent.DamageCause.FALL) {
										return;
									}
									final Player checkPlayer = (Player) e.getEntity();
									if (checkPlayer.equals(player)) {
										e.setCancelled(true);
									}
									e.getHandlers().unregister(this);
								}
							};
							Bukkit.getServer().getPluginManager().registerEvents(listener, builder.plugin);
							new BukkitRunnable() { // If they haven't taken fall damage within 10 seconds cancel one-time immunity
								@Override
								public void run() {
									new BukkitRunnable() {
										@Override
										public void run() {
											EntityDamageEvent.getHandlerList().unregister(listener);
										}
									}.runTask(builder.plugin);
								}
							}.runTaskLaterAsynchronously(builder.plugin, 200L);
						} else {
							player.setAllowFlight(true);
							player.setVelocity(player.getVelocity().add(new Vector(0d, 1, 0d)));
							builder.sendMessage(player, ConfiguredMessage.FLIGHT_ON);
							new BukkitRunnable() {
								@Override
								public void run() {
									player.setFlying(true);
								}
							}.runTaskLater(builder.plugin, 1L);
						}
						return;
					}

					if (args.length == 1) {
						PlayerSearch search = PlayerSearch.look(args[0]);
						if (search.isValid()) {
							if (search.isOnline()) {
								Player target = search.getPlayer();
								assert target != null;

								if (target.getGameMode() != GameMode.SURVIVAL) {
									builder.sendMessage(player, "");
									return;
								}
								if (target.getAllowFlight()) {
									target.setFlying(false);
									target.setAllowFlight(false);
									builder.sendMessage(target, ConfiguredMessage.FLIGHT_OFF);
									final Listener listener = new Listener() {
										@EventHandler
										public void onNextFallDamage(EntityDamageEvent e) {
											if (e.getEntityType() != EntityType.PLAYER) {
												return;
											}
											if (e.getCause() != EntityDamageEvent.DamageCause.FALL) {
												return;
											}
											final Player checkPlayer = (Player) e.getEntity();
											if (checkPlayer.equals(target)) {
												e.setCancelled(true);
											}
											e.getHandlers().unregister(this);
										}
									};
									Bukkit.getServer().getPluginManager().registerEvents(listener, builder.plugin);
									new BukkitRunnable() { // If they haven't taken fall damage within 10 seconds cancel one-time immunity
										@Override
										public void run() {
											new BukkitRunnable() {
												@Override
												public void run() {
													EntityDamageEvent.getHandlerList().unregister(listener);
												}
											}.runTask(builder.plugin);
										}
									}.runTaskLaterAsynchronously(builder.plugin, 200L);
								} else {
									target.setAllowFlight(true);
									target.setVelocity(target.getVelocity().add(new Vector(0d, 1, 0d)));
									builder.sendMessage(target, ConfiguredMessage.FLIGHT_ON);
									new BukkitRunnable() {
										@Override
										public void run() {
											target.setFlying(true);
										}
									}.runTaskLater(builder.plugin, 1L);
								}

							} else {
								// TODO: player not online
							}
						} else {
							// TODO: player isnt found.
						}
					}

				}).next((builder, sender, commandLabel, args) -> {
			PlayerSearch search = PlayerSearch.look(sender);
			search.sendMessage(ConfiguredMessage.MUST_BE_PLAYER);
		}).read((builder, sender, commandLabel, args) -> new ArrayList<>());

		CommandMapper.load(InternalCommandData.KICK_COMMAND, () -> kickTab = TabCompletion.build(InternalCommandData.KICK_COMMAND.getLabel()))
				.apply((builder, player, commandLabel, args) -> {
					if (args.length == 0) {
						builder.sendUsage(player);
						return;
					}

					if (args.length == 1) {
						PlayerSearch search = PlayerSearch.look(args[0]);
						if (builder.testPermission(player)) {
							if (search.isValid()) {

								OfflinePlayer target = search.getOfflinePlayer();

								if (search.kick(KickReason.next()
										.input(1, MyEssentialsAPI.getInstance().getPrefix())
										.input(2, "&c&oYou were kicked.")
										.input(3, "&eReason: &rNone specified."), false)) {
									builder.sendMessage(player, "Target kicked");
								} else {
									builder.sendMessage(player, "Target is offline.");
								}

							} else {
								builder.sendMessage(player, "&c&oTarget " + args[0] + " was not found.");
								return;
							}
							return;
						}
						return;
					}

					StringBuilder stringBuilder = new StringBuilder();
					for (int i = 1; i < args.length; i++) {
						stringBuilder.append(args[i]).append(" ");
					}
					if (builder.testPermission(player)) {
						String get = stringBuilder.toString().trim();

						PlayerSearch search = PlayerSearch.look(args[0]);
						if (search.isValid()) {

							if (search.kick((KickReason.next()
									.input(1, MyEssentialsAPI.getInstance().getPrefix())
									.input(2, "&c&oYou were kicked.")
									.input(3, "&eReason: &r" + get)), false)) {
								builder.sendMessage(player, "Target kicked for '" + get + "'");
							} else {
								builder.sendMessage(player, "Target is offline.");
							}

						} else {
							builder.sendMessage(player, "&c&oTarget " + args[0] + " was not found.");
						}
					}

				}).next((builder, sender, commandLabel, args) -> {
			if (args.length == 0) {
				builder.sendUsage(sender);
				return;
			}

			if (args.length == 1) {
				PlayerSearch search = PlayerSearch.look(args[0]);
				if (builder.testPermission(sender)) {
					if (search.isValid()) {

						OfflinePlayer target = search.getOfflinePlayer();

						if (search.kick(KickReason.next()
								.input(1, MyEssentialsAPI.getInstance().getPrefix())
								.input(2, "&c&oYou were kicked.")
								.input(3, "&eReason: &rNone specified."), true)) {
							builder.sendMessage(sender, "Target kicked");
						} else {
							builder.sendMessage(sender, "Target is offline.");
						}

					} else {
						builder.sendMessage(sender, "&c&oTarget " + args[0] + " was not found.");
						return;
					}
					return;
				}
				return;
			}

			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 1; i < args.length; i++) {
				stringBuilder.append(args[i]).append(" ");
			}
			if (builder.testPermission(sender)) {
				String get = stringBuilder.toString().trim();

				PlayerSearch search = PlayerSearch.look(args[0]);
				if (search.isValid()) {

					if (search.kick((KickReason.next()
							.input(1, MyEssentialsAPI.getInstance().getPrefix())
							.input(2, "&c&oYou were kicked.")
							.input(3, "&eReason: &r" + get)), true)) {
						builder.sendMessage(sender, "Target kicked for '" + get + "'");
					} else {
						builder.sendMessage(sender, "Target is offline.");
					}

				} else {
					builder.sendMessage(sender, "&c&oTarget " + args[0] + " was not found.");
				}
			}
		}).read((builder, sender, commandLabel, args) -> {
			return kickTab.forArgs(args)
					.level(1)
					.completeAt(builder.getData().getLabel())
					.filter(() -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()))
					.collect()
					.get(1);
		});


		CommandMapper.from(InternalCommandData.TRANSITION_COMMAND)
				.apply((builder, player, commandLabel, args) -> Schedule.sync(() -> {
					if (!canStop(player.getWorld(), 13000, 24000)) {
						if (!sent) {
							i = (int) player.getWorld().getTime();
							sent = true;
						}
						player.getWorld().setTime(i);
						Bukkit.broadcastMessage("Setting time to " + i);
						i += 20;
					} else {
						builder.sendMessage(player, "&cIts not dark enough yet.");
					}
				}).cancelAfter(task -> {
					if (canStop(player.getWorld(), 13000, 24000)) {
						i = 0;
						sent = false;
						Bukkit.broadcastMessage("Task cancelled");
						task.cancel();
					}
				}).repeat(0, 1))
				.next((builder, sender, commandLabel, args) -> {

				})
				.read((builder, sender, commandLabel, args) -> {
					return null;
				});

/*
		CommandMapper.from(InternalCommandData.FLY_COMMAND)
				.apply((builder, player, commandLabel, args) -> {

				}).read((builder, sender, commandLabel, args) -> {

		});

		CommandMapper.from(InternalCommandData.FLY_COMMAND)
				.apply((builder, player, commandLabel, args) -> {

				}).read((builder, sender, commandLabel, args) -> {

		});

		CommandMapper.from(InternalCommandData.FLY_COMMAND)
				.apply((builder, player, commandLabel, args) -> {

				}).read((builder, sender, commandLabel, args) -> {

		});

		CommandMapper.from(InternalCommandData.FLY_COMMAND)
				.apply((builder, player, commandLabel, args) -> {

				}).read((builder, sender, commandLabel, args) -> {

		});

		CommandMapper.from(InternalCommandData.FLY_COMMAND)
				.apply((builder, player, commandLabel, args) -> {

				}).read((builder, sender, commandLabel, args) -> {

		});

		CommandMapper.from(InternalCommandData.FLY_COMMAND)
				.apply((builder, player, commandLabel, args) -> {

				}).read((builder, sender, commandLabel, args) -> {

		});

 */

	}

}
