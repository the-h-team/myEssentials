package com.github.sanctum.myessentials.util;

import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class CooldownManager {

	public static void updateStorage() {
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			PlayerSearch search = PlayerSearch.look(player);
			if (search.getBanTimer() != null) {
				search.getBanTimer().update();
			}
		}
	}

	public static void renewTimers() {
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			PlayerSearch search = PlayerSearch.look(player);
			search.getBanTimer();
		}
	}

}
