/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.listeners;

import com.github.sanctum.myessentials.Essentials;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerEventListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void onTeleport(PlayerTeleportEvent e) {
		if (!e.getFrom().equals(e.getTo())) {
			Essentials.getInstance().previousLocation.put(e.getPlayer().getUniqueId(), e.getPlayer().getLocation());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onWorldChange(PlayerChangedWorldEvent e) {
		Essentials.getInstance().previousLocation.put(e.getPlayer().getUniqueId(), e.getPlayer().getLocation());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onDeath(PlayerDeathEvent e) {
		Essentials.getInstance().previousLocation.put(e.getEntity().getUniqueId(), e.getEntity().getLocation());
	}


}
