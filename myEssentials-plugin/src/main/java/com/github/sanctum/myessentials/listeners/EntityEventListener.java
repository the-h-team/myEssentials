/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.listeners;

import com.github.sanctum.afk.AFK;
import com.github.sanctum.labyrinth.data.Region;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.events.PendingTeleportToPlayerEvent;
import com.github.sanctum.myessentials.util.moderation.KickReason;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import com.github.sanctum.myessentials.util.teleportation.TeleportRequest;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class EntityEventListener implements Listener {
	private final Map<UUID, Boolean> taskScheduled = new HashMap<>();
	private final AtomicReference<Location> teleportLocation = new AtomicReference<>();

	private static final Map<UUID, Location> prevLocations = new HashMap<>();

	public EntityEventListener() {

	}

	public void sendMessage(Player p, String text) {
		Message.form(p).send(text);
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

	@EventHandler(priority = EventPriority.NORMAL)
	public void onGodHit(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			PlayerSearch wrapper = PlayerSearch.look(p);
			if (wrapper.isInvincible()) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {

		if (e.getTo() != null) {
			if (e.getFrom().getX() != e.getTo().getX() && e.getFrom().getY() != e.getTo().getY() && e.getFrom().getZ() != e.getTo().getZ()) {
				TeleportRequest r = MyEssentialsAPI.getInstance().getTeleportRunner().getActiveRequests()
						.stream().filter(pr -> pr.getPlayerTeleporting().getUniqueId().equals(e.getPlayer().getUniqueId()))
						.findFirst()
						.orElse(null);

				if (r != null) {
					if (r.getStatus() == TeleportRequest.Status.ACCEPTED) {
						MyEssentialsAPI.getInstance().getTeleportRunner().cancelRequest(r);
						Message.form(e.getPlayer()).setPrefix(MyEssentialsAPI.getInstance().getPrefix()).send(ConfiguredMessage.TP_CANCELLED.get());
					}
				}

			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onTeleport(PendingTeleportToPlayerEvent e) {
		Player p = e.getPlayerToTeleport();

		Message.form(p).setPrefix(MyEssentialsAPI.getInstance().getPrefix()).send(ConfiguredMessage.STAND_STILL.get());

		e.setDelay(10 * 20L);
	}

	public Optional<AFK> supply(Player player, int away, int kick) {
		if (AFK.found(player)) {
			return Optional.ofNullable(AFK.from(player));
		} else {
			return Optional.of(AFK.Initializer.next(player)
					.handle(Essentials.getInstance(), () -> new AFK.Handler() {
						@Override
						@EventHandler
						public void execute(AFK.StatusChange e) {
							Player p = e.getAfk().getPlayer();
							switch (e.getStatus()) {
								case AWAY:
									Bukkit.broadcastMessage(StringUtils.use(MyEssentialsAPI.getInstance().getPrefix() + " &7Player &b" + p.getName() + " &7is now AFK").translate());
									p.setDisplayName(StringUtils.use("&7*AFK&r " + p.getDisplayName()).translate());
									break;
								case RETURNING:
									p.setDisplayName(p.getName());
									Bukkit.broadcastMessage(StringUtils.use(MyEssentialsAPI.getInstance().getPrefix() + " &7Player &b" + p.getName() + " &7is no longer AFK").translate());
									e.getAfk().saturate();
									break;
								case REMOVABLE:
									Bukkit.broadcastMessage(StringUtils.use(MyEssentialsAPI.getInstance().getPrefix() + " &c&oPlayer &b" + p.getName() + " &c&owas kicked for being AFK too long.").translate());
									p.kickPlayer(StringUtils.use(MyEssentialsAPI.getInstance().getPrefix() + "\n" + "&c&oAFK too long.\n&c&oKicking to ensure safety :)").translate());
									e.getAfk().cancel();
									break;
							}
						}

						@EventHandler
						public void onLeave(PlayerQuitEvent e) {
							Player p = e.getPlayer();

							AFK afk = AFK.from(p);

							if (afk != null) {

								if (afk.getStatus() == AFK.Status.AWAY) {
									afk.saturate();
									p.setDisplayName(p.getName());
									Bukkit.broadcastMessage(StringUtils.use(MyEssentialsAPI.getInstance().getPrefix() + " &7Player &b" + p.getName() + " &7is no longer AFK").translate());
								}
							}

						}

						@EventHandler
						public void onCustomChannel(AsyncPlayerChatEvent e) {
							Player p = e.getPlayer();
							AFK afk = AFK.from(p);
							if (afk != null) {
								if (afk.getStatus() == AFK.Status.AWAY) {
									afk.saturate();
									p.setDisplayName(p.getName());
									Bukkit.broadcastMessage(StringUtils.use(MyEssentialsAPI.getInstance().getPrefix() + " &7Player &b" + p.getName() + " &7is no longer AFK").translate());
								}
							}
						}

						@EventHandler
						public void onCommand(PlayerCommandPreprocessEvent e) {
							Player p = e.getPlayer();
							AFK afk = AFK.from(p);

							if (afk != null) {
								if (afk.getStatus() == AFK.Status.AWAY) {
									afk.saturate();
									p.setDisplayName(p.getName());
									Bukkit.broadcastMessage(StringUtils.use(MyEssentialsAPI.getInstance().getPrefix() + " &7Player &b" + p.getName() + " &7is no longer AFK").translate());
								}
							}
						}

					})
					.stage(a -> TimeUnit.SECONDS.toMinutes(a.getWatch().interval(Instant.now()).getSeconds()) >= away, b -> TimeUnit.SECONDS.toMinutes(b.getWatch().interval(Instant.now()).getSeconds()) >= kick));
		}
	}


	@EventHandler
	public void afkInit(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		supply(p, 5, 15).ifPresent(afk -> {

		});

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onLogin(PlayerLoginEvent e) {
		Player p = e.getPlayer();

		PlayerSearch search = PlayerSearch.look(p);
		Cooldown timer = search.getBanTimer("&r(D&r)&e{DAYS} &r(H&r)&e{HOURS} &r(M&r)&e{MINUTES} &r(S&r)&e{SECONDS}").orElse(null);
		if (timer != null) {
			if (!timer.isComplete()) {
				e.disallow(PlayerLoginEvent.Result.KICK_BANNED, KickReason.next()
						.input(1, MyEssentialsAPI.getInstance().getPrefix())
						.input(2, " ")
						.input(3, ConfiguredMessage.LOGIN_TEMP_BANNED.toString())
						.input(4, "")
						.input(5, ConfiguredMessage.LOGIN_BANNED_REASON.replace(search.getBanEntry().map(BanEntry::getReason).orElse("null")))
						.input(6, "")
						.input(7, ConfiguredMessage.LOGIN_BAN_EXPIRES.replace(timer.fullTimeLeft()))
						.toString());
			} else {
				PlayerSearch.look(p).unban();
				Cooldown.remove(timer);
				e.allow();
			}
		}

		if (e.getResult() == PlayerLoginEvent.Result.ALLOWED) {
			Schedule.sync(() -> {
				if (Region.spawn().isPresent()) {
					if (Region.spawn().get().contains(e.getPlayer())) {
						if (p.getLocation().getBlock().getType() == Material.ORANGE_CARPET) {
							if (!taskScheduled.containsKey(p.getUniqueId())) {
								taskScheduled.put(p.getUniqueId(), true);

								Schedule.sync(() -> {
									int x = random(10500);
									int z = random(3500);
									int y = 150;
									teleportLocation.set(new Location(p.getWorld(), x, y, z));
									y = Objects.requireNonNull(teleportLocation.get().getWorld()).getHighestBlockYAt(teleportLocation.get());
									teleportLocation.get().setY(y);
									Message.form(p).action(MyEssentialsAPI.getInstance().getPrefix() + " Searching for suitable location...");
									p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 10, 1);

								}).cancelAfter(p).cancelAfter(task -> {
									if (taskScheduled.containsKey(p.getUniqueId()) && !taskScheduled.get(p.getUniqueId())) {
										sendMessage(p, ConfiguredMessage.SEARCH_INTERRUPTED.toString());
										task.cancel();
										return;
									}
									if (!taskScheduled.containsKey(p.getUniqueId())) {
										sendMessage(p, ConfiguredMessage.SEARCH_INTERRUPTED.toString());
										task.cancel();
										return;
									}
									if (teleportLocation.get() != null) {
										if (hasSurface(teleportLocation.get())) {
											p.teleport(teleportLocation.get());
											teleportLocation.set(null);
											sendMessage(p, ConfiguredMessage.TELEPORTED_SAFEST_LOCATION.replace(p.getWorld().getName()));
											taskScheduled.remove(p.getUniqueId());
											task.cancel();
										}
									}
								}).repeat(0, 3 * 20);
							}

						} else {
							if (taskScheduled.containsKey(p.getUniqueId()) && taskScheduled.get(p.getUniqueId())) {
								taskScheduled.remove(p.getUniqueId());
								sendMessage(p, ConfiguredMessage.STOPPING_SEARCH.toString());
							}
						}
					}
				}
			}).repeatReal(0, 2 * 20);
		}

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onTeleport(PlayerTeleportEvent e) {
		if (!e.getFrom().equals(e.getTo())) {
			prevLocations.put(e.getPlayer().getUniqueId(), e.getPlayer().getLocation());
		}
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if (StringUtils.use(e.getMessage()).containsIgnoreCase("/plugins")) {
			e.getPlayer().performCommand("pl");
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (e.getItem() != null) {
				ItemStack i = e.getItem();
				if (i.hasItemMeta()) {
					/*
					if (i.getItemMeta().getPersistentDataContainer().has(PowertoolCommand.KEY, PersistentDataType.STRING)) {
						e.setCancelled(true);
						String command = i.getItemMeta().getPersistentDataContainer().get(PowertoolCommand.KEY, PersistentDataType.STRING);
						Bukkit.dispatchCommand(e.getPlayer(), command);

					}

					 */
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onWorldChange(PlayerChangedWorldEvent e) {
		prevLocations.put(e.getPlayer().getUniqueId(), e.getPlayer().getLocation());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onDeath(PlayerDeathEvent e) {
		prevLocations.put(e.getEntity().getUniqueId(), e.getEntity().getLocation());
	}

	public static Map<UUID, Location> getPrevLocations() {
		return Collections.unmodifiableMap(prevLocations);
	}


}
