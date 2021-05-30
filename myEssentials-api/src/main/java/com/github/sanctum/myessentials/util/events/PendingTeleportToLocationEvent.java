/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen. However, this class indirectly extends a component
 *  of Bukkit API, and thus its license must be LGPL-compatible.
 */
package com.github.sanctum.myessentials.util.events;

import com.github.sanctum.myessentials.util.teleportation.TeleportRequest;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Encapsulates requested teleportation to a fixed location.
 */
public final class PendingTeleportToLocationEvent extends PendingTeleportEvent {
	private static final HandlerList HANDLERS = new HandlerList();

	public PendingTeleportToLocationEvent(@Nullable TeleportRequest request, @NotNull Player who, @NotNull Location location) {
		super(request, who, location);
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
