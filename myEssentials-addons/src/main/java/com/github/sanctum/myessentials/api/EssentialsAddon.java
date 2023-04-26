/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.api;

import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.myessentials.model.CommandData;
import com.github.sanctum.myessentials.model.CommandInput;
import com.github.sanctum.panther.util.Deployable;
import com.github.sanctum.panther.util.HUID;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class EssentialsAddon {

	boolean active;
	private final ClassLoader classLoader;
	private final EssentialsAddonContext context;

	public EssentialsAddon() {
		ClassLoader loader = this.getClass().getClassLoader();
		if (!(loader instanceof EssentialsClassLoader) && !MyEssentialsAPI.class.getClassLoader().equals(loader))
			throw new InvalidAddonStateException("Addon not provided by " + EssentialsClassLoader.class);
		this.classLoader = loader;
		this.context = new EssentialsAddonContext() {
			private final List<Listener> listeners = new ArrayList<>();
			private final Map<CommandData, Class<? extends CommandInput>> commands = new HashMap<>();

			@Override
			public EssentialsAddon loadAddon(File jar) throws IOException, InvalidAddonException {
				return new EssentialsClassLoader(jar, EssentialsAddon.this).getMainClass();
			}

			@Override
			public Deployable<Void> enableAddon(EssentialsAddon addon) {
				return Deployable.of(() -> {
					EssentialsAddonQuery.enable(addon);
				}, 0);
			}

			@Override
			public Deployable<Void> disableAddon(EssentialsAddon addon) {
				return Deployable.of(() -> {
					EssentialsAddonQuery.disable(addon);
				}, 0);
			}

			@Override
			public Collection<Listener> getListeners() {
				return listeners;
			}

			@Override
			public Map<CommandData, Class<? extends CommandInput>> getCommands() {
				return commands;
			}

			@Override
			public void stage(Listener listener) {
				listeners.add(listener);
			}

			@Override
			public <T extends CommandInput> void stage(CommandData data, Class<T> t) {
				commands.put(data, t);
			}


		};
	}

	public EssentialsAddon(EssentialsAddonContext context) {
		ClassLoader loader = this.getClass().getClassLoader();
		if (!(loader instanceof EssentialsClassLoader) && !MyEssentialsAPI.class.getClassLoader().equals(loader))
			throw new InvalidAddonStateException("Addon not provided by " + EssentialsClassLoader.class);
		this.classLoader = loader;
		this.context = context;
	}

	protected abstract void onLoad();

	protected abstract void onEnable();

	protected abstract void onDisable();

	public abstract boolean isPersistent();

	/**
	 * Get the name of this addon.
	 *
	 * @return The name of the addon.
	 */
	public abstract String getName();

	/**
	 * Get the addon's unique id.
	 *
	 * @return The addon's unique id.
	 */
	public HUID getId() {
		return HUID.randomID();
	}

	/**
	 * Get the addon's version.
	 *
	 * @return The addon's version.
	 */
	public abstract String getVersion();

	/**
	 * Get the creator list for this addon.
	 *
	 * @return The list of authors for the addon.
	 */
	public abstract String[] getAuthors();

	/**
	 * Get the description of this addon.
	 *
	 * @return The addon description.
	 */
	public abstract String getDescription();

	public final Logger getLogger() {
		return JavaPlugin.getProvidingPlugin(MyEssentialsAPI.class).getLogger();
	}

	public final ClassLoader getClassLoader() {
		return this.classLoader;
	}

	public final EssentialsAddonContext getContext() {
		return context;
	}

	public boolean isActive() {
		return active;
	}

	final void register() {
		EssentialsAddonQuery.cache(this);
	}

	/**
	 * Manually un-register all corresponding listeners from this addon.
	 */
	public final void remove() {
		EssentialsAddonQuery.disable(this);
		TaskScheduler.of(() -> EssentialsAddonQuery.getKnownAddons().removeIf(c -> c.getName().equals(getName()))).scheduleLater(1L);
	}
}
