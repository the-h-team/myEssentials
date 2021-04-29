package com.github.sanctum.myessentials;

import com.github.sanctum.labyrinth.formatting.TabCompletion;
import com.github.sanctum.labyrinth.formatting.TabCompletionBuilder;
import com.github.sanctum.labyrinth.gui.shared.SharedMenu;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.CommandMapper;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.DateTimeCalculator;
import com.github.sanctum.myessentials.util.events.PlayerPendingHealEvent;
import com.github.sanctum.myessentials.util.gui.MenuManager;
import com.github.sanctum.myessentials.util.moderation.KickReason;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
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
	private static TabCompletionBuilder transitionTab;
	private static TabCompletionBuilder kickTabAll;
	private static TabCompletionBuilder banTab;
	private static TabCompletionBuilder healTab;
	private static TabCompletionBuilder dayTab;
	private static TabCompletionBuilder nightTab;
	private static TabCompletionBuilder staffTab;
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
								// player not online
								builder.sendMessage(player, ConfiguredMessage.PLAYER_MUST_BE_ONLINE);
							}
						} else {
							// player isn't found.
							builder.sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
						}
					}

				}).next((builder, sender, commandLabel, args) -> {
			PlayerSearch search = PlayerSearch.look(sender);
			search.sendMessage(ConfiguredMessage.MUST_BE_PLAYER);
		}).read((builder, sender, commandLabel, args) -> new ArrayList<>());

		CommandMapper.from(InternalCommandData.BAN_COMMAND, builder -> banTab = TabCompletion.build(builder.getData().getLabel()))
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

								if (search.ban(player.getName())) {
									builder.sendMessage(player, ConfiguredMessage.BANNED_TARGET);
								} else {
									builder.sendMessage(player, ConfiguredMessage.TARGET_ALREADY_BANNED);
								}

							} else {
								builder.sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
								return;
							}
							return;
						}
						return;
					}

					StringBuilder text = new StringBuilder();
					for (int i = 1; i < args.length; i++) {
						text.append(args[i]).append(" ");
					}
					String get = text.toString().trim();

					PlayerSearch search = PlayerSearch.look(args[0]);
					if (builder.testPermission(player)) {
						if (search.isValid()) {

							OfflinePlayer target = search.getOfflinePlayer();

							if (search.ban(player.getName(), get)) {
								builder.sendMessage(player, ConfiguredMessage.BANNED_REASON.replace(get));
							} else {
								builder.sendMessage(player, ConfiguredMessage.TARGET_ALREADY_BANNED);
							}

						} else {
							builder.sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
						}
					}
				})
				.next((builder, sender, commandLabel, args) -> {
					if (args.length == 0) {
						builder.sendUsage(sender);
						return;
					}

					if (args.length == 1) {
						PlayerSearch search = PlayerSearch.look(args[0]);
						if (builder.testPermission(sender)) {
							if (search.isValid()) {

								OfflinePlayer target = search.getOfflinePlayer();

								if (search.ban(sender.getName())) {
									builder.sendMessage(sender, ConfiguredMessage.BANNED_TARGET);
								} else {
									builder.sendMessage(sender, ConfiguredMessage.TARGET_ALREADY_BANNED);
								}

							} else {
								builder.sendMessage(sender, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
								return;
							}
							return;
						}
						return;
					}

					StringBuilder text = new StringBuilder();
					for (int i = 1; i < args.length; i++) {
						text.append(args[i]).append(" ");
					}
					String get = text.toString().trim();

					PlayerSearch search = PlayerSearch.look(args[0]);
					if (builder.testPermission(sender)) {
						if (search.isValid()) {

							OfflinePlayer target = search.getOfflinePlayer();

							if (search.ban(sender.getName(), get)) {
								builder.sendMessage(sender, ConfiguredMessage.BANNED_REASON.replace(get));
							} else {
								builder.sendMessage(sender, ConfiguredMessage.TARGET_ALREADY_BANNED);
							}

						} else {
							builder.sendMessage(sender, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
						}
					}
				})
				.read((builder, p, commandLabel, args) -> banTab.forArgs(args)
						.level(1)
						.completeAt(builder.getData().getLabel())
						.filter(() -> Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList()))
						.collect()
						.get(1));

		CommandMapper.from(InternalCommandData.UNBAN_COMMAND, builder -> unbanTab = TabCompletion.build(builder.getData().getLabel()))
				.apply((builder, player, commandLabel, args) -> {
					if (builder.testPermission(player)) {
						if (args.length == 1) {
							PlayerSearch search = PlayerSearch.look(args[0]);
							if (search.isValid()) {

								if (search.unban()) {
									builder.sendMessage(player, ConfiguredMessage.TARGET_UNBANNED);
								} else {
									builder.sendMessage(player, ConfiguredMessage.TARGET_NOT_BANNED);
								}

							} else {
								builder.sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
							}
						}
					}
				})
				.next((builder, player, commandLabel, args) -> {
					if (builder.testPermission(player)) {
						if (args.length == 1) {
							PlayerSearch search = PlayerSearch.look(args[0]);
							if (search.isValid()) {

								if (search.unban()) {
									builder.sendMessage(player, ConfiguredMessage.TARGET_UNBANNED);
								} else {
									builder.sendMessage(player, ConfiguredMessage.TARGET_NOT_BANNED);
								}

							} else {
								builder.sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
							}
						}
					}
				})
				.read((builder, sender, commandLabel, args) -> unbanTab.forArgs(args)
						.level(1)
						.completeAt(builder.getData().getLabel())
						.filter(() -> Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()))
						.collect().get(1));

		CommandMapper.from(InternalCommandData.KICK_COMMAND, builder -> kickTab = TabCompletion.build(builder.getData().getLabel()))
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
										.input(2, ConfiguredMessage.YOU_WERE_KICKED.toString())
										.input(3, ConfiguredMessage.DEFAULT_KICK_REASON.toString()), false)) {
									builder.sendMessage(player, ConfiguredMessage.TARGET_KICKED);
								} else {
									builder.sendMessage(player, ConfiguredMessage.TARGET_OFFLINE);
								}

							} else {
								builder.sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
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
									.input(2, ConfiguredMessage.YOU_WERE_KICKED.toString())
									.input(3, ConfiguredMessage.CUSTOM_KICK_REASON.replace(get))), false)) {
								builder.sendMessage(player, ConfiguredMessage.TARGET_KICKED_WITH_REASON.replace(get));
							} else {
								builder.sendMessage(player, ConfiguredMessage.TARGET_OFFLINE);
							}

						} else {
							builder.sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
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
								.input(2, ConfiguredMessage.YOU_WERE_KICKED.toString())
								.input(3, ConfiguredMessage.DEFAULT_KICK_REASON.toString()), true)) {
							builder.sendMessage(sender, ConfiguredMessage.TARGET_KICKED);
						} else {
							builder.sendMessage(sender, ConfiguredMessage.TARGET_OFFLINE);
						}

					} else {
						builder.sendMessage(sender, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
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
							.input(2, ConfiguredMessage.YOU_WERE_KICKED.toString())
							.input(3, ConfiguredMessage.CUSTOM_KICK_REASON.replace(get))), true)) {
						builder.sendMessage(sender, ConfiguredMessage.TARGET_KICKED_WITH_REASON.replace(get));
					} else {
						builder.sendMessage(sender, ConfiguredMessage.TARGET_OFFLINE);
					}

				} else {
					builder.sendMessage(sender, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
				}
			}
		}).read((builder, sender, commandLabel, args) -> kickTab.forArgs(args)
				.level(1)
				.completeAt(builder.getData().getLabel())
				.filter(() -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()))
				.collect()
				.get(1));

		CommandMapper.from(InternalCommandData.TEMPBAN_COMMAND, builder -> tempBanTab = TabCompletion.build(builder.getData().getLabel()))
				.apply((builder, player, commandLabel, args) -> {
					if (args.length == 0) {
						builder.sendUsage(player);
						return;
					}

					if (args.length == 1) {
						builder.sendUsage(player);
						return;
					}


					if (args.length == 2) {
						if (builder.testPermission(player)) {
							PlayerSearch search = PlayerSearch.look(args[0]);
							if (search.isValid()) {
								long result;
								try {
									result = DateTimeCalculator.parse(args[1].toUpperCase());
								} catch (DateTimeParseException e) {
									try {
										result = DateTimeCalculator.parseShort(args[1].toUpperCase());
									} catch (DateTimeParseException ex) {
										builder.sendMessage(player, ConfiguredMessage.INVALID_TIME_FORMAT);
										builder.sendMessage(player, ConfiguredMessage.TIME_EXAMPLE);
										return;
									}
								}

								if (search.ban(player.getName(), kick -> {
									kick.input(1, ConfiguredMessage.YOU_HAVE_BEEN_BANNED.toString());
									kick.input(2, ConfiguredMessage.BAN_EXPIRATION.replace(search.getBanTimer().fullTimeLeft()));
								}, result, false)) {
									builder.sendMessage(player, ConfiguredMessage.UNBAN_TIME_TO_SENDER.replace(search.getBanTimer().fullTimeLeft()));
								} else {
									if (search.getBanTimer() != null) {
										if (search.getBanTimer().isComplete()) {
											Cooldown.remove(search.getBanTimer());
											search.unban(false);
											Bukkit.dispatchCommand(player, commandLabel + " " + args[0] + " " + args[1]);
											return;
										}
										builder.sendMessage(player, ConfiguredMessage.TARGET_ALREADY_BANNED);
										builder.sendMessage(player, ConfiguredMessage.UNBANNED_TIME.replace(search.getBanTimer().fullTimeLeft()));
									}
								}
							}
						} else {
							if (builder.testPermission(player)) {
								builder.sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
								return;
							}
							return;
						}
						return;
					}

					StringBuilder sbuilder = new StringBuilder();
					for (int i = 2; i < args.length; i++) {
						sbuilder.append(args[i]).append(" ");
					}
					String get = sbuilder.toString().trim();

					long result;
					try {
						result = DateTimeCalculator.parse(args[1].toUpperCase());
					} catch (DateTimeParseException e) {
						try {
							result = DateTimeCalculator.parseShort(args[1].toUpperCase());
						} catch (DateTimeParseException ex) {
							builder.sendMessage(player, ConfiguredMessage.INVALID_TIME_FORMAT);
							builder.sendMessage(player, ConfiguredMessage.TIME_EXAMPLE);
							return;
						}
					}
					if (builder.testPermission(player)) {
						PlayerSearch search = PlayerSearch.look(args[0]);
						if (search.isValid()) {
							if (search.ban(player.getName(), kick -> {
								kick.input(1, ConfiguredMessage.YOU_HAVE_BEEN_BANNED.toString());
								final String replace = ConfiguredMessage.BAN_KICK_REASON.replace(get);
								kick.input(3, replace);
								kick.input(2, ConfiguredMessage.BAN_EXPIRATION.replace(search.getBanTimer().fullTimeLeft()));
								kick.reason(StringUtils.translate(replace));
							}, result, false)) {
								builder.sendMessage(player, ConfiguredMessage.UNBAN_TIME_TO_SENDER.replace(search.getBanTimer().fullTimeLeft()));
							} else {
								if (search.getBanTimer() != null) {
									if (search.getBanTimer().isComplete()) {
										Cooldown.remove(search.getBanTimer());
										search.unban(false);
										Bukkit.dispatchCommand(player, commandLabel + " " + args[0] + " " + args[1] + " " + get);
										return;
									}
									builder.sendMessage(player, ConfiguredMessage.TARGET_ALREADY_BANNED);
									builder.sendMessage(player, ConfiguredMessage.UNBANNED_TIME.replace(search.getBanTimer().fullTimeLeft()));
								}
							}

						} else {
							if (builder.testPermission(player)) {
								builder.sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
							}
						}
					}
				})
				.next((builder, sender, commandLabel, args) -> {
					if (args.length == 0) {
						builder.sendUsage(sender);
						return;
					}

					if (args.length == 1) {
						builder.sendUsage(sender);
						return;
					}


					if (args.length == 2) {
						if (builder.testPermission(sender)) {
							PlayerSearch search = PlayerSearch.look(args[0]);
							if (search.isValid()) {
								long banLength;
								try {
									banLength = DateTimeCalculator.parse(args[1].toUpperCase());
								} catch (DateTimeParseException e) {
									try {
										banLength = DateTimeCalculator.parseDays(args[1].toUpperCase());
									} catch (DateTimeParseException ex) {
										try {
											banLength = DateTimeCalculator.parseShort(args[1].toUpperCase());
										} catch (DateTimeParseException exc) {
											builder.sendMessage(sender, ConfiguredMessage.INVALID_TIME_CONSOLE);
											builder.sendMessage(sender, ConfiguredMessage.TIME_EXAMPLE);
											return;
										}
									}
								}
								if (search.ban(sender.getName(), kick -> {
									kick.input(1, ConfiguredMessage.YOU_HAVE_BEEN_BANNED.toString());
									kick.input(2, ConfiguredMessage.BAN_EXPIRATION.replace(search.getBanTimer().fullTimeLeft()));
								}, banLength, false)) {
									builder.sendMessage(sender, ConfiguredMessage.UNBAN_TIME_TO_SENDER.replace(search.getBanTimer().fullTimeLeft()));
								} else {
									if (search.getBanTimer() != null) {
										if (search.getBanTimer().isComplete()) {
											Cooldown.remove(search.getBanTimer());
											search.unban(false);
											Bukkit.dispatchCommand(sender, commandLabel + " " + args[0] + " " + args[1]);
											return;
										}
										builder.sendMessage(sender, ConfiguredMessage.TARGET_ALREADY_BANNED);
										builder.sendMessage(sender, ConfiguredMessage.UNBANNED_TIME.replace(search.getBanTimer().fullTimeLeft()));
									}
								}
							}
						} else {
							if (builder.testPermission(sender)) {
								builder.sendMessage(sender, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
								return;
							}
							return;
						}
						return;
					}

					StringBuilder sbuilder = new StringBuilder();
					for (int i = 2; i < args.length; i++) {
						sbuilder.append(args[i]).append(" ");
					}
					String get = sbuilder.toString().trim();

					long banLength;
					try {
						banLength = DateTimeCalculator.parse(args[1].toUpperCase());
					} catch (DateTimeParseException e) {
						try {
							banLength = DateTimeCalculator.parseDays(args[1].toUpperCase());
						} catch (DateTimeParseException ex) {
							try {
								banLength = DateTimeCalculator.parseShort(args[1].toUpperCase());
							} catch (DateTimeParseException exc) {
								builder.sendMessage(sender, ConfiguredMessage.INVALID_TIME_CONSOLE);
								builder.sendMessage(sender, ConfiguredMessage.TIME_EXAMPLE);
								return;
							}
						}
					}
					if (builder.testPermission(sender)) {
						PlayerSearch search = PlayerSearch.look(args[0]);
						if (search.isValid()) {
							if (search.ban(sender.getName(), kick -> {
								kick.input(1, ConfiguredMessage.YOU_HAVE_BEEN_BANNED.toString());
								final String replace = ConfiguredMessage.BAN_KICK_REASON.replace(get);
								kick.input(3, replace);
								kick.input(2, ConfiguredMessage.BAN_EXPIRATION.replace(search.getBanTimer().fullTimeLeft()));
								kick.reason(StringUtils.translate(replace));
							}, banLength, false)) {
								builder.sendMessage(sender, ConfiguredMessage.UNBAN_TIME_TO_SENDER.replace(search.getBanTimer().fullTimeLeft()));
							} else {
								if (search.getBanTimer() != null) {
									if (search.getBanTimer().isComplete()) {
										Cooldown.remove(search.getBanTimer());
										search.unban(false);
										Bukkit.dispatchCommand(sender, commandLabel + " " + args[0] + " " + args[1] + " " + get);
										return;
									}
									builder.sendMessage(sender, ConfiguredMessage.TARGET_ALREADY_BANNED);
									builder.sendMessage(sender, ConfiguredMessage.UNBANNED_TIME.replace(search.getBanTimer().fullTimeLeft()));
								}
							}

						} else {
							if (builder.testPermission(sender)) {
								builder.sendMessage(sender, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
							}
						}
					}
				})
				.read((builder, player, commandLabel, args) -> {
					if (args.length == 3) {
						return tempBanTab.forArgs(args)
								.level(3)
								.completeAt(builder.getData().getLabel())
								.filter(() -> Collections.singletonList(ConfiguredMessage.REASON.toString()))
								.collect()
								.get(3);
					}

					if (args.length == 2) {
						return tempBanTab.forArgs(args)
								.level(2)
								.completeAt(builder.getData().getLabel())
								.filter(() -> {
									List<String> result = new ArrayList<>(Arrays.asList("1s", "1m", "1h", "1d", "2s", "2m", "3h", "3d"));
									Collections.sort(result);
									return result;
								})
								.collect()
								.get(2);
					}

					return tempBanTab.forArgs(args)
							.level(1)
							.completeAt(builder.getData().getLabel())
							.filter(() -> Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList()))
							.collect()
							.get(1);
				});

		CommandMapper.from(InternalCommandData.DAY_COMMAND, builder -> dayTab = TabCompletion.build(builder.getData().getLabel()))
				.apply((builder, player, commandLabel, args) -> {
					if (args.length == 0) {
						if (builder.testPermission(player)) {
							player.getWorld().setTime(0);
							builder.sendMessage(player, ConfiguredMessage.SET_DAY);
							return;
						}
						return;
					}
					if (args.length == 1) {
						if (args[0].equalsIgnoreCase("morning")) {
							if (builder.testPermission(player)) {
								player.getWorld().setTime(0);
								builder.sendMessage(player, ConfiguredMessage.SET_MORNING);
								return;
							}
							return;
						}
						if (args[0].equalsIgnoreCase("noon")) {
							if (builder.testPermission(player)) {
								player.getWorld().setTime(6000);
								builder.sendMessage(player, ConfiguredMessage.SET_NOON);
								return;
							}
							return;
						}
						if (args[0].equalsIgnoreCase("afternoon")) {
							if (builder.testPermission(player)) {
								player.getWorld().setTime(9500);
								builder.sendMessage(player, ConfiguredMessage.SET_AFTERNOON);
								return;
							}
							return;
						}
						builder.sendUsage(player);
					}
				})
				.next((builder, sender, commandLabel, args) -> {

				})
				.read((builder, player, commandLabel, args) -> dayTab
						.forArgs(args)
						.level(1)
						.completeAt(builder.getData().getLabel())
						.filter(() -> Arrays.asList("morning", "noon", "afternoon"))
						.map("morning", () -> {
							Random r = new Random();
							if (r.nextBoolean()) {
								if (r.nextInt(28) < 6) {
									builder.sendMessage(player, ConfiguredMessage.DAY_VALUES_DESC);
								}
							}
						})
						.collect()
						.get(1));

		CommandMapper.from(InternalCommandData.NIGHT_COMMAND, builder -> nightTab = TabCompletion.build(builder.getData().getLabel()))
				.apply((builder, player, commandLabel, args) -> {
					if (args.length == 0) {
						if (builder.testPermission(player)) {
							player.getWorld().setTime(0);
							builder.sendMessage(player, ConfiguredMessage.SET_DAY);
							return;
						}
						return;
					}
					if (args.length == 1) {
						if (args[0].equalsIgnoreCase("night")) {
							if (builder.testPermission(player)) {
								player.getWorld().setTime(13000);
								builder.sendMessage(player, ConfiguredMessage.SET_NIGHT);
								return;
							}
							return;
						}
						if (args[0].equalsIgnoreCase("midnight")) {
							if (builder.testPermission(player)) {
								player.getWorld().setTime(18000);
								builder.sendMessage(player, ConfiguredMessage.SET_MIDNIGHT);
								return;
							}
							return;
						}
						if (args[0].equalsIgnoreCase("dusk")) {
							if (builder.testPermission(player)) {
								player.getWorld().setTime(22000);
								builder.sendMessage(player, ConfiguredMessage.SET_DUSK);
								return;
							}
							return;
						}
						builder.sendUsage(player);
					}
				})
				.next((builder, sender, commandLabel, args) -> {

				})
				.read((builder, player, commandLabel, args) -> nightTab
						.forArgs(args)
						.level(1)
						.completeAt(builder.getData().getLabel())
						.filter(() -> Arrays.asList("night", "midnight", "dusk"))
						.map("night", () -> {
							Random r = new Random();
							if (r.nextBoolean()) {
								if (r.nextInt(28) < 6) {
									builder.sendMessage(player, ConfiguredMessage.NIGHT_VALUES_DESC);
								}
							}
						})
						.collect()
						.get(1));

		CommandMapper.from(InternalCommandData.GOD_COMMAND)
				.apply((builder, player, commandLabel, args) -> {

				})
				.next((builder, sender, commandLabel, args) -> {

				})
				.read(CommandBuilder::tabComplete);

		CommandMapper.from(InternalCommandData.BIN_COMMAND)
				.apply((builder, player, commandLabel, args) -> {
					player.openInventory(MenuManager.Select.DONATION_BIN.share().getInventory());
				})
				.next((builder, sender, commandLabel, args) -> {
					builder.sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
				})
				.read(CommandBuilder::tabComplete);

		CommandMapper.from(InternalCommandData.FEED_COMMAND)
				.apply((builder, player, commandLabel, args) -> {

				})
				.next((builder, sender, commandLabel, args) -> {

				})
				.read(CommandBuilder::tabComplete);

		CommandMapper.from(InternalCommandData.INVSEE_COMMAND)
				.apply((builder, player, commandLabel, args) -> {
					if (args.length == 0) {
						builder.sendUsage(player);
						return;
					}

					if (args.length == 1) {

						if (Bukkit.getPlayer(args[0]) != null) {
							Player target = Bukkit.getPlayer(args[0]);

							if (player == target) {
								builder.sendMessage(player, ConfiguredMessage.INVSEE_DENY_SELF);
								return;
							}

							assert target != null;
							player.openInventory(SharedMenu.open(target));

						} else {
							builder.sendMessage(player, ConfiguredMessage.PLAYER_NOT_FOUND);
						}
					}
				})
				.next((builder, sender, commandLabel, args) -> builder.sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER))
				.read(CommandBuilder::tabComplete);

		CommandMapper.from(InternalCommandData.HEAL_COMMAND, builder -> healTab = TabCompletion.build(builder.getData().getLabel()))
				.apply((builder, player, commandLabel, args) -> {
					if (args.length == 0) {
						if (builder.testPermission(player)) {
							Bukkit.getPluginManager().callEvent(new PlayerPendingHealEvent(null, player, 20));
							return;
						}
						return;
					}

					if (args.length == 1) {
						PlayerSearch search = PlayerSearch.look(args[0]);
						if (search.isValid()) {
							if (search.isOnline()) {
								Player target = search.getPlayer();
								if (builder.testPermission(player)) {
									assert target != null;
									search.heal(player, 20);
									builder.sendMessage(player, ConfiguredMessage.HEAL_TARGET_MAXED.replace(target.getName()));
								}
							} else {
								if (builder.testPermission(player)) {
									builder.sendMessage(player, ConfiguredMessage.HEAL_TARGET_NOT_ONLINE.replace(search.getOfflinePlayer().getName()));
								}
							}
						} else {
							if (builder.testPermission(player)) {
								builder.sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
							}
						}
					}
				})
				.next((builder, sender, commandLabel, args) -> {

				})
				.read((builder, player, commandLabel, args) -> healTab.forArgs(args)
						.level(1)
						.completeAt(builder.getData().getLabel())
						.filter(() -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()))
						.collect()
						.get(1));


		CommandMapper.from(InternalCommandData.TRANSITION_COMMAND, builder -> transitionTab = TabCompletion.build(builder.getData().getLabel()))
				.apply((builder, player, commandLabel, args) -> {

					if (builder.testPermission(player)) {
						if (args.length == 0) {
							if (sent) {
								builder.sendMessage(player, ConfiguredMessage.TRANSITION_IN_PROGRESS);
								return;
							}
							if (canStop(player.getWorld(), 13000, 24000)) {
								builder.sendMessage(player, ConfiguredMessage.ALREADY_DAY);
								return;
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
									builder.sendMessage(player, ConfiguredMessage.SET_DAY);
								}
							}).cancelAfter(task -> {
								if (canStop(player.getWorld(), 13000, 24000)) {
									i = 0;
									sent = false;
									task.cancel();
								}
							}).repeat(0, 1);
							return;
						}
						if (args.length == 1) {
							switch (args[0].toLowerCase()) {
								case "day":
									if (canStop(player.getWorld(), 13000, 24000)) {
										builder.sendMessage(player, ConfiguredMessage.ALREADY_DAY);
										return;
									}
									if (sent) {
										builder.sendMessage(player, ConfiguredMessage.TRANSITION_IN_PROGRESS);
										return;
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
											builder.sendMessage(player, ConfiguredMessage.SET_DAY);
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
										builder.sendMessage(player, ConfiguredMessage.ALREADY_NIGHT);
										return;
									}
									if (sent) {
										builder.sendMessage(player, ConfiguredMessage.TRANSITION_IN_PROGRESS);
										return;
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
											builder.sendMessage(player, ConfiguredMessage.SET_NIGHT);
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
								builder.sendMessage(player, "&c" + e.getMessage());
								return;
							}
							if (sent) {
								builder.sendMessage(player, ConfiguredMessage.TRANSITION_IN_PROGRESS);
								return;
							}
							if (Integer.parseInt(args[1]) > 500) {
								builder.sendMessage(player, ConfiguredMessage.TRANSITION_TOO_FAST);
								return;
							}
							switch (args[0].toLowerCase()) {
								case "day":
									if (canStop(player.getWorld(), 13000, 24000)) {
										builder.sendMessage(player, ConfiguredMessage.ALREADY_DAY);
										return;
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
											builder.sendMessage(player, ConfiguredMessage.SET_DAY);
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
											builder.sendMessage(player, ConfiguredMessage.ALREADY_NIGHT);
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
											builder.sendMessage(player, ConfiguredMessage.SET_NIGHT);
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
				})
				.next((builder, sender, commandLabel, args) -> {
					if (builder.testPermission(sender)) {
						if (args.length == 0) {
							if (sent) {
								builder.sendMessage(sender, ConfiguredMessage.TRANSITION_IN_PROGRESS);
								return;
							}
							if (canStop(Bukkit.getWorlds().get(0), 13000, 24000)) {
								builder.sendMessage(sender, ConfiguredMessage.ALREADY_DAY);
								return;
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
									builder.sendMessage(sender, ConfiguredMessage.SET_DAY);
								}
							}).cancelAfter(task -> {
								if (canStop(Bukkit.getWorlds().get(0), 13000, 24000)) {
									i = 0;
									sent = false;
									task.cancel();
								}
							}).repeat(0, 1);
							return;
						}
						if (args.length == 1) {
							switch (args[0].toLowerCase()) {
								case "day":
									if (canStop(Bukkit.getWorlds().get(0), 13000, 24000)) {
										builder.sendMessage(sender, ConfiguredMessage.ALREADY_DAY);
										return;
									}
									if (sent) {
										builder.sendMessage(sender, ConfiguredMessage.TRANSITION_IN_PROGRESS);
										return;
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
											builder.sendMessage(sender, ConfiguredMessage.SET_DAY);
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
										builder.sendMessage(sender, ConfiguredMessage.ALREADY_NIGHT);
										return;
									}
									if (sent) {
										builder.sendMessage(sender, ConfiguredMessage.TRANSITION_IN_PROGRESS);
										return;
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
											builder.sendMessage(sender, ConfiguredMessage.SET_NIGHT);
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
								builder.sendMessage(sender, "&c" + e.getMessage());
								return;
							}
							if (sent) {
								builder.sendMessage(sender, ConfiguredMessage.TRANSITION_IN_PROGRESS);
								return;
							}
							if (Integer.parseInt(args[1]) > 500) {
								builder.sendMessage(sender, ConfiguredMessage.TRANSITION_TOO_FAST);
								return;
							}
							switch (args[0].toLowerCase()) {
								case "day":
									if (canStop(Bukkit.getWorlds().get(0), 13000, 24000)) {
										builder.sendMessage(sender, ConfiguredMessage.ALREADY_DAY);
										return;
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
											builder.sendMessage(sender, ConfiguredMessage.SET_DAY);
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
											builder.sendMessage(sender, ConfiguredMessage.ALREADY_NIGHT);
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
											builder.sendMessage(sender, ConfiguredMessage.SET_NIGHT);
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
				})
				.read((builder, sender, commandLabel, args) -> transitionTab.forArgs(args)
						.level(1)
						.completeAt(builder.getData().getLabel())
						.filter(() -> {
							List<String> list = new ArrayList<>();
							if (builder.testPermission(sender)) {
								list.add("day");
								list.add("night");
							}
							return list;
						})
						.collect().get(1));

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
