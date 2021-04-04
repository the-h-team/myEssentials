package com.github.sanctum.myessentials.util.events;

import com.github.sanctum.labyrinth.library.Applicable;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.myessentials.util.moderation.PlayerHealingProcessor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Calling this event on a target player will heal them the specified amount.
 * <p>
 * The only way to edit the heal amount without constructor access is by listening to the
 * {@link PlayerPendingHealEvent} event.
 */
public class PlayerHealEvent extends PlayerHealingProcessor {

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final CommandSender healer;

	private double amount;

	private Applicable applicable;

	/**
	 * Specify the parties involved within the healing event. Command sender can be console, player or null.
	 *
	 * @param healer Specify who is doing the healing or return null to simply tell them they've been healed.
	 * @param target Specify who is getting healed.
	 * @param amount Specify the amount of health the player will gain.
	 */
	public PlayerHealEvent(@Nullable CommandSender healer, @NotNull Player target, double amount) {
		if (amount > 20) {
			throw new IllegalArgumentException("Amount's over twenty go past minecraft's limitations. Try with a lower amount.");
		}
		PlayerPendingHealEvent event = new PlayerPendingHealEvent(healer, target, amount);
		Bukkit.getPluginManager().callEvent(event);
		this.amount = event.getAmount();
		this.healer = event.getHealer();
		final Plugin plugin = JavaPlugin.getProvidingPlugin(getClass());
		double s = target.getHealth() + event.getAmount();
		target.setHealth(s < 20 ? s : 20);
		if (healer != null) {
			if (healer instanceof Player) {
				Player heal = (Player) healer;
				Message.form(target).send("&r[&2" + plugin + "&r] Player " + heal.getName() + " healed your wounds.");
			} else {
				Message.form(target).send("&r[&2" + plugin + "&r] &c&oConsole has healed your wounds.");
			}
		} else {
			Message.form(target).send("&r[&2" + plugin + "&r] Your wounds have been healed.");
		}
	}

	/**
	 * Specify the parties involved within the healing event. Command sender can be console, player or null.
	 *
	 * @param healer Specify who is doing the healing or return null to simply tell them they've been healed.
	 * @param amount Specify the amount of health the player will gain.
	 */
	public PlayerHealEvent(@Nullable CommandSender healer, double amount) {
		this.healer = healer;
		this.amount = amount;
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
		if (amount > 20) {
			throw new IllegalArgumentException("Amount's over twenty go past minecraft's limitations. Try with a lower amount.");
		}
		PlayerPendingHealEvent event = new PlayerPendingHealEvent(this.healer, target, this.amount);
		Bukkit.getPluginManager().callEvent(event);
		this.amount = event.getAmount();
		final Plugin plugin = JavaPlugin.getProvidingPlugin(getClass());
		double s = target.getHealth() + event.getAmount();
		this.applicable = (Applicable) () -> {
			target.setHealth(s < 20 ? s : 20);
			if (healer != null) {
				if (healer instanceof Player) {
					Player heal = (Player) healer;
					Message.form(target).send("&r[&2" + plugin + "&r] Player " + heal.getName() + " healed your wounds.");
				} else {
					Message.form(target).send("&r[&2" + plugin + "&r] &c&oConsole has healed your wounds.");
				}
			} else {
				Message.form(target).send("&r[&2" + plugin + "&r] Your wounds have been healed.");
			}
		};
	}

	@Override
	public double getAmount() {
		return this.amount;
	}

	@Override
	protected Applicable patch() {
		return this.applicable;
	}
}
