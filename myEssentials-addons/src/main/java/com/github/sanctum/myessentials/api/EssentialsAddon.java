/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.api;

import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.CommandData;
import java.util.Collection;
import java.util.Map;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class EssentialsAddon {

	public abstract boolean persist();

	public abstract boolean isStandalone();

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
	public abstract Map<CommandData, Class<? extends CommandBuilder>> getCommands();

	/**
	 * Get the collection of applied data from the addon.
	 *
	 * @return The collection of specified data from within the addon.
	 */
	public abstract Map<Object, Object> getData();

	protected abstract void apply();

	protected final void register() {
		AddonQuery.getKnownAddons().add(getInstance());
	}

	/**
	 * Manually un-register all corresponding listeners from this addon.
	 */
	public final void remove() {
		final MyEssentialsAPI instance = MyEssentialsAPI.getInstance();
		instance.logInfo("- Scheduling addon " + '"' + getAddonName() + '"' + " for removal.");
		for (RegisteredListener l : HandlerList.getRegisteredListeners(JavaPlugin.getProvidingPlugin(instance.getClass()))) {
			if (getListeners().contains(l.getListener())) {
				HandlerList.unregisterAll(l.getListener());
			}
		}
		Schedule.sync(() -> AddonQuery.getKnownAddons().removeIf(c -> c.getAddonName().equals(getAddonName()))).wait(1);
	}

}
