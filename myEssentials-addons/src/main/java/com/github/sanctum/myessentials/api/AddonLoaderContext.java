package com.github.sanctum.myessentials.api;

import com.github.sanctum.labyrinth.library.Deployable;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.CommandData;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.bukkit.event.Listener;

public interface AddonLoaderContext {

	EssentialsAddon loadAddon(File jar) throws IOException, InvalidAddonException;

	Deployable<Void> enableAddon(EssentialsAddon addon);

	Deployable<Void> disableAddon(EssentialsAddon addon);

	/**
	 * Get the collection of listeners this addon is attempting to register
	 *
	 * @return The collection of listeners for this addon.
	 */
	Collection<Listener> getListeners();

	/**
	 * Get the collection of command classes to be registered.
	 *
	 * @return The collection of {@link CommandBuilder} classes for this addon.
	 */
	Map<CommandData, Class<? extends CommandBuilder>> getCommands();

	void stage(Listener listener);

	<T extends CommandBuilder> void stage(CommandData data, Class<T> t);

}
