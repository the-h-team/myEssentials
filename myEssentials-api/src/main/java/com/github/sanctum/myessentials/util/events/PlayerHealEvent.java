package com.github.sanctum.myessentials.util.events;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when healing a valid player.
 */
public final class PlayerHealEvent extends HealEvent {
	private static final HandlerList HANDLER_LIST = new HandlerList();

	public PlayerHealEvent(PlayerPendingHealEvent e) {
		super(e.healer, e.target, e.amount);
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}

	public static @NotNull HandlerList getHandlerList() {
		return HANDLER_LIST;
	}
}
