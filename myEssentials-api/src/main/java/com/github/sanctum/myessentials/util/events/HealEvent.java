package com.github.sanctum.myessentials.util.events;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for healing events.
 */
public abstract class HealEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    protected final Player target;
    protected final CommandSender healer;
    protected double amount;

    protected HealEvent(@Nullable CommandSender healer, @NotNull Player target, double amount) {
        if (amount > 20) {
            throw new IllegalArgumentException("Amounts over twenty go past Minecraft's limitations. Try with a lower amount.");
        }
        this.healer = healer;
        this.target = target;
        this.amount = amount;
    }

    public @NotNull Player getTarget() {
        return target;
    }

    public @Nullable CommandSender getHealer() {
        return healer;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
