package com.github.sanctum.myessentials.listeners;

import com.github.sanctum.labyrinth.afk.AFK;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AFKEventListener implements Listener {

	public Optional<AFK> supply(Player player, int away, int kick) {
		if (AFK.found(player)) {
			return Optional.ofNullable(AFK.from(player));
		} else {
			return Optional.of(AFK.Initializer.next(player)
					.handle(Vent.Subscription.Builder.target(AFK.StatusChange.class).assign(Vent.Priority.HIGH).from(Essentials.getInstance()).assign((e, subscription) -> {

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

					}))
					.stage(a -> a.getRecording().getMinutes() >= away, b -> b.getRecording().getMinutes() >= kick));
		}
	}


	@EventHandler
	public void afkInit(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		supply(p, 5, 15).ifPresent(afk -> afk.getPlayer().sendTitle(StringUtils.use("&eTry not to AFK too long!").translate(), "", 20, 45, 20));

	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		AFK afk = AFK.from(p);

		if (afk != null) {

			if (afk.getStatus() == AFK.Status.AWAY) {
				p.setDisplayName(p.getName());
				Bukkit.broadcastMessage(StringUtils.use(MyEssentialsAPI.getInstance().getPrefix() + " &7Player &b" + p.getName() + " &7is no longer AFK").translate());
			}

			afk.cancel();
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
