package com.github.sanctum.myessentials.util.factory;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class GroupFinder {

	private static Permission perm() {
		return Bukkit.getServicesManager().load(Permission.class);
	}

	public static String group(OfflinePlayer user, String world) {
		return perm() != null ? perm().getPrimaryGroup(world, user) : "";
	}

	public static String[] groups(OfflinePlayer user, String world) {
		return perm() != null ? perm().getPlayerGroups(world, user) : new String[0];
	}

}
