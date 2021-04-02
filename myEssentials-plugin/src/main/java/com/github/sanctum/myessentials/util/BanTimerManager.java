package com.github.sanctum.myessentials.util;

import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class BanTimerManager {

	public static void updateStorage() {
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			PlayerSearch search = PlayerSearch.look(player);
			if (search.getBanTimer() != null) {
				// Update the timer to the current remaining time in hard storage
				search.getBanTimer().update();
			}
		}
	}

	public static void renewTimers() {
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			PlayerSearch search = PlayerSearch.look(player);
			// Simply making the call to the timer will check hard storage and create a new cached instance based off
			// the remaining time of the previous usage.
			search.getBanTimer();
		}
	}

}
