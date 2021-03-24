package com.github.sanctum.myessentials.api;

import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.model.CommandBuilder;
import java.util.Collection;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;

public abstract class EssentialsAddon {

	public abstract boolean persist();

	/**
	 * Get the addon's unique id.
	 *
	 * @return The addon's unique id.
	 */
	public HUID getId() {
		return HUID.randomID();
	}

	public abstract EssentialsAddon getInstance();

	/**
	 * Get the creator list for this addon.
	 *
	 * @return The list of authors for the addon.
	 */
	public abstract String[] getAuthors();

	/**
	 * Get the name of this addon.
	 *
	 * @return The name of the addon.
	 */
	public abstract String getAddonName();

	/**
	 * Get the description of this addon.
	 *
	 * @return The addon description.
	 */
	public abstract String getAddonDescription();

	/**
	 * Get the collection of listeners this addon is attempting to register
	 *
	 * @return The collection of listeners for this addon.
	 */
	public abstract Collection<Listener> getListeners();

	/**
	 * Get the collection of command classes to be registered.
	 *
	 * @return The collection of {@link CommandBuilder} classes for this addon.
	 */
	public abstract Collection<Class<? extends CommandBuilder>> getCommands();

	protected abstract void apply();

	protected final void register() {
		AddonQuery.getKnownAddons().add(getInstance());
	}

	/**
	 * Manually un-register all corresponding listeners from this addon.
	 */
	public final void remove() {
		Essentials.getInstance().getLogger().info("- Scheduling addon " + '"' + getAddonName() + '"' + " for removal.");
		for (RegisteredListener l : HandlerList.getRegisteredListeners(Essentials.getInstance())) {
			if (getListeners().contains(l.getListener())) {
				HandlerList.unregisterAll(l.getListener());
			}
		}
		Schedule.sync(() -> AddonQuery.getKnownAddons().removeIf(c -> c.getAddonName().equals(getAddonName()))).wait(1);
	}

}
