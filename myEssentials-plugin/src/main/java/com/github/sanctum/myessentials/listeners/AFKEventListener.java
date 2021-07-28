package com.github.sanctum.myessentials.listeners;

import com.github.sanctum.labyrinth.afk.AFK;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AFKEventListener implements Listener {

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

}
