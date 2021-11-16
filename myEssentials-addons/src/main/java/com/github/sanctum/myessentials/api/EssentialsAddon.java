/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.api;

import com.github.sanctum.labyrinth.library.Deployable;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.CommandData;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.event.Listener;

public abstract class EssentialsAddon {

	boolean active;
	private final ClassLoader classLoader;
	private final AddonLoaderContext context;

	public EssentialsAddon() {
		ClassLoader loader = this.getClass().getClassLoader();
		if (!(loader instanceof EssentialsAddonClassLoader) && !MyEssentialsAPI.class.getClassLoader().equals(loader))
			throw new InvalidAddonStateException("Addon not provided by " + EssentialsAddonClassLoader.class);
		this.classLoader = loader;
		this.context = new AddonLoaderContext() {
			private final List<Listener> listeners = new ArrayList<>();
			private final Map<CommandData, Class<? extends CommandBuilder>> commands = new HashMap<>();

			@Override
			public EssentialsAddon loadAddon(File jar) throws IOException, InvalidAddonException {
				return new EssentialsAddonClassLoader(jar, EssentialsAddon.this).addon;
			}

			@Override
			public Deployable<Void> enableAddon(EssentialsAddon addon) {
				return Deployable.of(null, unused -> EssentialsAddonQuery.enable(addon));
			}

			@Override
			public Deployable<Void> disableAddon(EssentialsAddon addon) {
				return Deployable.of(null, unused -> EssentialsAddonQuery.disable(addon));
			}

			@Override
			public Collection<Listener> getListeners() {
				return listeners;
			}

			@Override
			public Map<CommandData, Class<? extends CommandBuilder>> getCommands() {
				return commands;
			}

			@Override
			public void stage(Listener listener) {
				listeners.add(listener);
			}

			@Override
			public <T extends CommandBuilder> void stage(CommandData data, Class<T> t) {
				commands.put(data, t);
			}


		};
	}

	public EssentialsAddon(AddonLoaderContext context) {
		ClassLoader loader = this.getClass().getClassLoader();
		if (!(loader instanceof EssentialsAddonClassLoader) && !MyEssentialsAPI.class.getClassLoader().equals(loader))
			throw new InvalidAddonStateException("Addon not provided by " + EssentialsAddonClassLoader.class);
		this.classLoader = loader;
		this.context = context;
	}

	protected abstract void onLoad();

	protected abstract void onEnable();

	protected abstract void onDisable();

	public abstract boolean isStaged();

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
		return MyEssentialsAPI.getInstance().getExecutorHandler().getPlugin().getLogger();
	}

	public final ClassLoader getClassLoader() {
		return this.classLoader;
	}

	public final AddonLoaderContext getContext() {
		return context;
	}

	public boolean isActive() {
		return active;
	}

	final void register() {
		EssentialsAddonQuery.getKnownAddons().add(this);
	}

	/**
	 * Manually un-register all corresponding listeners from this addon.
	 */
	public final void remove() {
		EssentialsAddonQuery.disable(this);
		Schedule.sync(() -> EssentialsAddonQuery.getKnownAddons().removeIf(c -> c.getName().equals(getName()))).wait(1);
	}
}
