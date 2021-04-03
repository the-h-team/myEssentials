package com.github.sanctum.myessentials.util.moderation;

import com.github.sanctum.labyrinth.library.Applicable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class PlayerHealingProcessor extends Event {
	@Override
	public @NotNull HandlerList getHandlers() {
		return null;
	}

	public abstract void setTarget(Player target);

	public abstract double getAmount();

	protected abstract Applicable patch();
}
