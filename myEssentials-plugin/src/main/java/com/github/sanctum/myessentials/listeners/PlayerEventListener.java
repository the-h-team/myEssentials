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
import com.github.sanctum.myessentials.util.events.PlayerPendingHealEvent;
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

public final class PlayerEventListener implements Listener {
	private static PlayerEventListener instance;

	private final Map<UUID, Location> prevLocations = new HashMap<>();

	{
		instance = this;
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
						.input(3, "&c&oTemporarily banned.")
						.input(4, "")
						.input(5, "&cReason:&r " + search.getBanEntry().orElse(null).getReason())
						.input(6, "")
						.input(7, "&6Expires: " + timer.fullTimeLeft())
						.toString());
			} else {
				PlayerSearch.look(p).unban();
				Cooldown.remove(timer);
				e.allow();
			}
		}
	}

	@EventHandler
	public void onHeal(PlayerPendingHealEvent e) {
		e.setAmount(2);
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
