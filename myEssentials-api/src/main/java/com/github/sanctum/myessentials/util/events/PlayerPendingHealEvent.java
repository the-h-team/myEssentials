package com.github.sanctum.myessentials.util.events;

import com.github.sanctum.labyrinth.library.Applicable;
import com.github.sanctum.myessentials.util.moderation.PlayerHealingProcessor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The event which a target player is about to be healed, here you can configure information to be passed to the resulting event.
 */
public class PlayerPendingHealEvent extends PlayerHealingProcessor {

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final CommandSender sender;

	private Player target;

	private double amount;

	protected PlayerPendingHealEvent(@Nullable CommandSender healer, @NotNull Player target, double amount) {
		this.sender = healer;
		this.target = target;

		if (amount > 20) {
			throw new IllegalArgumentException("Amount's over twenty go past minecraft's limitations. Try with a lower amount.");
		}

		this.amount = amount;
	}

	public @NotNull Player getTarget() {
		return target;
	}

	public @Nullable CommandSender getHealer() {
		return sender;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Override
	public double getAmount() {
		return amount;
	}

	@Override
	protected Applicable patch() {
		return null;
	}

	public static @NotNull HandlerList getHandlerList() {
		return HANDLER_LIST;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}

	@Override
	public void setTarget(Player target) {
		this.target = target;
	}
}
