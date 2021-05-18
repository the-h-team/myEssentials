package com.github.sanctum.myessentials.util.events;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when feeding a valid player.
 */
public final class PlayerFeedEvent extends HealEvent {
	private static final HandlerList HANDLER_LIST = new HandlerList();

	public PlayerFeedEvent(PlayerPendingFeedEvent e) {
		super(e.healer, e.target, e.amount);
	}

	public int getAmountReal() {
		return (int) this.amount;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}

	public static @NotNull HandlerList getHandlerList() {
		return HANDLER_LIST;
	}
}
