/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.util.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Encapsulates requested teleportation to a Player.
 */
public final class MEssPendingTeleportToPlayerEvent extends MEssPendingTeleportEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    public MEssPendingTeleportToPlayerEvent(@NotNull Player who, @NotNull Player target) {
        super(who, target);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
