/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.util.permissions;

import com.github.sanctum.myessentials.api.AddonQuery;
import com.github.sanctum.myessentials.api.EssentialsAddon;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

@SuppressWarnings("unchecked")
public final class PermissiveConnection {

	// Utility class - no instantiation
	private PermissiveConnection() {
		throw new IllegalStateException("This class should not be instantiated!");
	}

	public static boolean trusted() {
		return AddonQuery.find("myPermissions") != null;
	}

	public static synchronized int getWeight(OfflinePlayer player) {
		if (AddonQuery.find("myPermissions") != null) {
			EssentialsAddon addon = AddonQuery.find("myPermissions");
			Map<Object, Object> map = addon.getData();

			Map<String, Integer> GROUP_MAP = Collections.unmodifiableMap((Map<String, Integer>) map.get("GROUP"));

			Map<UUID, String> USER_MAP = (Map<UUID, String>) map.get("USER");

			return GROUP_MAP.get(USER_MAP.get(player.getUniqueId()));
		}
		return -1;
	}

	public static synchronized int getWeight(String group) {
		if (AddonQuery.find("myPermissions") != null) {
			EssentialsAddon addon = AddonQuery.find("myPermissions");
			Map<Object, Object> map = addon.getData();
			Map<String, Integer> GROUP_MAP = Collections.unmodifiableMap((Map<String, Integer>) map.get("GROUP"));

			return GROUP_MAP.get(group);
		}
		return -1;
	}

	public static synchronized String getGroup(OfflinePlayer player) {
		if (AddonQuery.find("myPermissions") != null) {
			EssentialsAddon addon = AddonQuery.find("myPermissions");
			Map<Object, Object> map = addon.getData();
			Map<UUID, String> USER_MAP = Collections.unmodifiableMap((Map<UUID, String>) map.get("USER"));
			return USER_MAP.get(player.getUniqueId());
		}
		return "Unknown";
	}


}
