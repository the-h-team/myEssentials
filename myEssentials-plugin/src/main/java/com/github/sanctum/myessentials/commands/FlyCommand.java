package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlyCommand extends CommandOutput {
	public FlyCommand() {
		super(InternalCommandData.FLY_COMMAND);
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return null;
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (!testPermission(player)) {
			return true;
		}
		if (args.length == 0) {
			if (player.getGameMode() != GameMode.SURVIVAL) {
				sendMessage(player, ConfiguredMessage.TRY_IN_SURVIVAL);
				return true;
			}
			if (player.getAllowFlight()) {
				player.setFlying(false);
				player.setAllowFlight(false);
				sendMessage(player, ConfiguredMessage.FLIGHT_OFF);
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
				Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
				new BukkitRunnable() { // If they haven't taken fall damage within 10 seconds cancel one-time immunity
					@Override
					public void run() {
						new BukkitRunnable() {
							@Override
							public void run() {
								EntityDamageEvent.getHandlerList().unregister(listener);
							}
						}.runTask(plugin);
					}
				}.runTaskLaterAsynchronously(plugin, 200L);
			} else {
				player.setAllowFlight(true);
				player.setVelocity(player.getVelocity().add(new Vector(0d, 1, 0d)));
				sendMessage(player, ConfiguredMessage.FLIGHT_ON);
				new BukkitRunnable() {
					@Override
					public void run() {
						player.setFlying(true);
					}
				}.runTaskLater(plugin, 1L);
			}
			return true;
		}

		if (args.length == 1) {
			PlayerSearch search = PlayerSearch.look(args[0]);
			if (search.isValid()) {
				if (search.isOnline()) {
					Player target = search.getPlayer();
					assert target != null;

					if (target.getGameMode() != GameMode.SURVIVAL) {
						sendMessage(player, "&cThis user is already in a gamemode that allows them to fly.");
						return true;
					}
					if (target.getAllowFlight()) {
						target.setFlying(false);
						target.setAllowFlight(false);
						sendMessage(target, ConfiguredMessage.FLIGHT_OFF);
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
						Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
						new BukkitRunnable() { // If they haven't taken fall damage within 10 seconds cancel one-time immunity
							@Override
							public void run() {
								new BukkitRunnable() {
									@Override
									public void run() {
										EntityDamageEvent.getHandlerList().unregister(listener);
									}
								}.runTask(plugin);
							}
						}.runTaskLaterAsynchronously(plugin, 200L);
					} else {
						target.setAllowFlight(true);
						target.setVelocity(target.getVelocity().add(new Vector(0d, 1, 0d)));
						sendMessage(target, ConfiguredMessage.FLIGHT_ON);
						new BukkitRunnable() {
							@Override
							public void run() {
								target.setFlying(true);
							}
						}.runTaskLater(plugin, 1L);
					}

				} else {
					// player not online
					sendMessage(player, ConfiguredMessage.PLAYER_MUST_BE_ONLINE);
				}
			} else {
				// player isn't found.
				sendMessage(player, ConfiguredMessage.TARGET_NOT_FOUND.replace(args[0]));
			}
		}
		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		PlayerSearch search = PlayerSearch.look(sender);
		search.sendMessage(ConfiguredMessage.MUST_BE_PLAYER);
		return true;
	}
}
