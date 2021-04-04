package com.github.sanctum.myessentials.util.moderation;

import com.github.sanctum.labyrinth.library.Applicable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public abstract class PlayerHealingProcessor extends Event {


	public abstract void setTarget(Player target);

	public abstract double getAmount();

	protected abstract Applicable patch();
}
