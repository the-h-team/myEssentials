package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.permissions.Permissions;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public interface PermissionCheck {

	boolean hasWildcard(OfflinePlayer op);

	boolean has(OfflinePlayer op);

	boolean has(OfflinePlayer op, String... children);

	static PermissionCheck of(@NotNull String parent) {
		return new PermissionCheck() {

			final Permissions perms = LabyrinthProvider.getInstance().getServicesManager().load(Permissions.class);

			@Override
			public boolean hasWildcard(OfflinePlayer op) {
				return perms.getUser(op).getInheritance().test(parent + ".*");
			}

			@Override
			public boolean has(OfflinePlayer op) {
				return perms.getUser(op).getInheritance().test(parent);
			}

			@Override
			public boolean has(OfflinePlayer op, String... children) {
				boolean pass = true;
				for (String c : children) {
					if (pass && !perms.getUser(op).getInheritance().test(parent + "." + c)) {
						pass = false;
					}
				}
				return pass || hasWildcard(op);
			}
		};
	}

}
