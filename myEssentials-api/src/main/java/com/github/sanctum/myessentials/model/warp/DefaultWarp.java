package com.github.sanctum.myessentials.model.warp;

import com.github.sanctum.labyrinth.library.HUID;
import org.bukkit.Location;
import org.bukkit.permissions.ServerOperator;
import org.jetbrains.annotations.NotNull;

public class DefaultWarp implements Warp {

	private ServerOperator owner;
	private final String name;
	private final Location location;

	public DefaultWarp() {
		this.name = HUID.randomID().toString();
		this.location = null;
	}

	public DefaultWarp(ServerOperator owner, String name, Location location) {
		this.owner = owner;
		this.name = name;
		this.location = location;
	}

	@Override
	public ServerOperator getOwner() {
		return owner;
	}

	@Override
	public @NotNull String getName() {
		return name;
	}

	@Override
	public @NotNull Location getLocation() {
		return location;
	}
}
