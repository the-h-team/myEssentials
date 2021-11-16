package com.github.sanctum.myessentials.listeners;

import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityEventListener implements Listener {

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

}
