/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.listeners;

import com.github.sanctum.labyrinth.data.Region;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.commands.PowertoolCommand;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.moderation.KickReason;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public final class PlayerEventListener implements Listener {
	private static PlayerEventListener instance;
	private final Map<UUID, Boolean> taskScheduled = new HashMap<>();
	private final AtomicReference<Location> teleportLocation = new AtomicReference<>();

	private final Map<UUID, Location> prevLocations = new HashMap<>();

	{
		instance = this;
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
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (e.getItem() != null) {
				ItemStack i = e.getItem();
				if (i.hasItemMeta()) {
					if (i.getItemMeta().getPersistentDataContainer().has(PowertoolCommand.KEY, PersistentDataType.STRING)) {
						e.setCancelled(true);
						String command = i.getItemMeta().getPersistentDataContainer().get(PowertoolCommand.KEY, PersistentDataType.STRING);
						Bukkit.dispatchCommand(e.getPlayer(), command);

					}
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

	public Map<UUID, Location> getPrevLocations() {
		return Collections.unmodifiableMap(prevLocations);
	}

	public static PlayerEventListener getInstance() {
		return instance;
	}

}
