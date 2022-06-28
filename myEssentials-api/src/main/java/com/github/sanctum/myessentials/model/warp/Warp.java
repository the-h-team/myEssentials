package com.github.sanctum.myessentials.model.warp;

import com.github.sanctum.labyrinth.data.JsonAdapter;
import com.github.sanctum.labyrinth.data.NodePointer;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.ServerOperator;
import org.jetbrains.annotations.NotNull;

@NodePointer(value = "Warp", type = DefaultWarp.class)
public interface Warp extends Nameable, JsonAdapter<Warp> {

	ServerOperator getOwner();

	@NotNull String getName();

	@NotNull Location getLocation();

	default void teleport(Player player) {
		player.teleport(getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
	}

	@Override
	default JsonElement write(Warp warp) {
		JsonObject location = new JsonObject();
		location.addProperty("name", warp.getName());
		if (warp.getOwner() instanceof OfflinePlayer) {
			location.addProperty("owner", ((OfflinePlayer) warp.getOwner()).getUniqueId().toString());
		}
		location.add("loc", JsonAdapter.get(Location.class).write(warp.getLocation()));
		return location;
	}

	@Override
	default Warp read(Map<String, Object> object) {
		String name = object.get("name").toString();
		Location loc = JsonAdapter.get(Location.class).read((Map<String, Object>) object.get("loc"));
		OfflinePlayer owner = object.containsKey("owner") ? Bukkit.getOfflinePlayer(UUID.fromString(object.get("owner").toString())) : null;
		return new DefaultWarp(owner, name, loc);
	}

	@Override
	default Class<Warp> getClassType() {
		return Warp.class;
	}
}
