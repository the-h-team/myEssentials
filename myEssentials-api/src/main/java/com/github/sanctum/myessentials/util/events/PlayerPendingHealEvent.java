package com.github.sanctum.myessentials.util.events;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a target player is about to be healed.
 * <p>
 * Here, you can modify information to be passed to the final event
 * or cancel the healing altogether.
 */
public final class PlayerPendingHealEvent extends HealEvent implements Cancellable {
	private static final HandlerList HANDLER_LIST = new HandlerList();

	protected boolean cancelled;

	public PlayerPendingHealEvent(@Nullable CommandSender healer, @NotNull Player target, double amount) {
		super(healer, target, amount);
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}

	public static @NotNull HandlerList getHandlerList() {
		return HANDLER_LIST;
	}
}
