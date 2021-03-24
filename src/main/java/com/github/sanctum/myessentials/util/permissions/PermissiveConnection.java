package com.github.sanctum.myessentials.util.permissions;

import com.github.sanctum.myessentials.api.AddonQuery;
import com.github.sanctum.myessentials.api.EssentialsAddon;
import java.util.Map;
import java.util.UUID;
import org.bukkit.OfflinePlayer;

public class PermissiveConnection {

	public static boolean trusted() {
		return AddonQuery.find("myPermissions") != null;
	}

	public static int getWeight(OfflinePlayer player) {
		if (AddonQuery.find("myPermissions") != null) {
			EssentialsAddon addon = AddonQuery.find("myPermissions");
			Map<Object, Object> map = addon.getData();
			Map<String, Integer> GROUP_MAP = (Map<String, Integer>) map.get("GROUP");
			Map<UUID, String> USER_MAP = (Map<UUID, String>) map.get("USER");
			return GROUP_MAP.get(USER_MAP.get(player.getUniqueId()));
		}
		return -1;
	}

	public static int getWeight(String group) {
		if (AddonQuery.find("myPermissions") != null) {
			EssentialsAddon addon = AddonQuery.find("myPermissions");
			Map<Object, Object> map = addon.getData();
			Map<String, Integer> GROUP_MAP = (Map<String, Integer>) map.get("GROUP");
			return GROUP_MAP.get(group);
		}
		return -1;
	}

	public static String getGroup(OfflinePlayer player) {
		if (AddonQuery.find("myPermissions") != null) {
			EssentialsAddon addon = AddonQuery.find("myPermissions");
			Map<Object, Object> map = addon.getData();
			Map<UUID, String> USER_MAP = (Map<UUID, String>) map.get("USER");
			return USER_MAP.get(player.getUniqueId());
		}
		return "Unknown";
	}


}
