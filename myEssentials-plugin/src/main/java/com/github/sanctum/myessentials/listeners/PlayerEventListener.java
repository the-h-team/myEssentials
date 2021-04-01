/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.listeners;

import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.util.moderation.KickReason;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerEventListener implements Listener {
	private static PlayerEventListener instance;

	{
		instance = this;
	}

	private final Map<UUID, Location> prevLocations = new HashMap<>();

	@EventHandler(priority = EventPriority.NORMAL)
	public void onLogin(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		PlayerSearch search = PlayerSearch.look(p);
		if (search.getBanTimer() != null) {
			Cooldown timer = search.getBanTimer();
			if (!timer.isComplete()) {
				e.disallow(PlayerLoginEvent.Result.KICK_BANNED, KickReason.next().input(1, MyEssentialsAPI.getInstance().getPrefix()).input(2, "&c&oTemporarily banned.").input(3, "&6Expires: " + timer.fullTimeLeft()).input(4, search.getBanEntry().orElse(null).getReason()).toString());
			} else {
				PlayerSearch.look(p).unban();
				Cooldown.remove(timer);
				e.allow();
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onTeleport(PlayerTeleportEvent e) {
		if (!e.getFrom().equals(e.getTo())) {
			prevLocations.put(e.getPlayer().getUniqueId(), e.getPlayer().getLocation());
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
