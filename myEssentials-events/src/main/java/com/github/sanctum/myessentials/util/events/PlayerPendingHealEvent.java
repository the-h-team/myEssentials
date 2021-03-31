package com.github.sanctum.myessentials.util.events;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerPendingHealEvent extends Event {

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final CommandSender sender;

	private final Player target;

	private double amount;

	protected PlayerPendingHealEvent(@Nullable CommandSender healer, @NotNull Player target, double amount) {
		this.sender = healer;
		this.target = target;

		if (amount > 20) {
			throw new IllegalArgumentException("Amount's over twenty go past minecraft's limitations. Try with a lower amount.");
		}

		this.amount = amount;
		final Plugin plugin = JavaPlugin.getProvidingPlugin(getClass());
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

	public double getHealAmount() {
		return amount;
	}

	public static @NotNull HandlerList getHandlerList() {
		return HANDLER_LIST;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}
}
