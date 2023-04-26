/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.api;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.data.RegistryData;
import com.github.sanctum.labyrinth.task.Procedure;
import com.github.sanctum.myessentials.model.CommandData;
import com.github.sanctum.myessentials.model.CommandInput;
import com.github.sanctum.panther.annotation.Note;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class EssentialsAddonQuery {

	private static final PantherMap<String, EssentialsAddon> addonMap = new PantherEntryMap<>();

	private static final List<String> dataLog = new ArrayList<>();

	/**
	 * Get the collection of persistently picked up addons.
	 * <p>
	 * *NOTE: If the addon doesn't persist it won't be in cache.
	 *
	 * @return A collection of enabled/disabled registered addons.
	 */
	public static Collection<EssentialsAddon> getKnownAddons() {
		return addonMap.values().stream().collect(Collectors.toList());
	}

	/**
	 * Find a persistently picked up addon by its name.
	 *
	 * @param name The name of the addon to find.
	 * @return An {@link EssentialsAddon} object.
	 */
	public static EssentialsAddon find(String name) {
		return addonMap.get(name);
	}

	public static void cache(@NotNull EssentialsAddon addon) {
		addonMap.put(addon.getName(), addon);
	}

	/**
	 * Get a stream of currently registered but disabled addons.
	 *
	 * @return All disabled persistent addons.
	 */
	public static Stream<EssentialsAddon> getDisabledAddons() {
		return addonMap.values().stream().filter(e -> !e.isActive());
	}

	/**
	 * Get the list of currently registered but enabled addons.
	 *
	 * @return All enabled persistent addons.
	 */
	public static Stream<EssentialsAddon> getEnabledAddons() {
		return addonMap.values().stream().filter(EssentialsAddon::isActive);
	}

	/**
	 * Get the list of currently persistent registered addons.
	 *
	 * @return All persistent registered addons.
	 */
	public static Stream<EssentialsAddon> getRegisteredAddons() {
		return addonMap.values().stream();
	}

	/**
	 * Manually un-register listeners from an addon.
	 * *NOTE: Requires initial pickup and registration.
	 *
	 * @param e The addon to un-register.
	 */
	public static boolean disable(EssentialsAddon e) {
		if (!e.isActive()) return false;
		dataLog.clear();
		MyEssentialsAPI.getInstance().logInfo("- Queueing removal of " + '"' + e.getName() + '"' + " addon information.");
		dataLog.add("[Essentials] - Queueing removal of " + '"' + e.getName() + '"' + " addon information.");
		e.onDisable();
		List<Listener> a = HandlerList.getRegisteredListeners(JavaPlugin.getProvidingPlugin(EssentialsAddon.class)).stream()
				.filter(r -> e.getContext().getListeners().contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
		int count = 0;
		if (!a.isEmpty()) {
			dataLog.add(" - Unregistering addon from handler-list.");
			for (Listener l : a) {
				HandlerList.unregisterAll(l);
				count++;
			}
			if (count > 0) {
				MyEssentialsAPI.getInstance().logInfo("- (+" + count + ") Listener(s) successfully un-registered");
				dataLog.add(" - (+" + count + ") Listener(s) found and un-registered");
			}
		} else {
			MyEssentialsAPI.getInstance().logInfo("- Failed to un-register events. None currently running.");
			dataLog.add(" - Failed to un-register events. None currently running.");
		}
		for (CommandData command : e.getContext().getCommands().keySet()) {
			Command c = MyEssentialsAPI.getInstance().getRegistration(command);
			if (c == null) continue;
			if (c.isRegistered()) {
				MyEssentialsAPI.getInstance().unregisterCommand(c);
				MyEssentialsAPI.getInstance().logInfo(() -> "- Successfully un-registered command " + c.getClass().getSimpleName());
				dataLog.add(" - Successfully un-registered command " + c.getClass().getSimpleName());
			} else {
				MyEssentialsAPI.getInstance().logInfo(() -> "- Failed to un-register command " + c.getClass().getSimpleName());
				dataLog.add(" - Failed to un-register command " + c.getClass().getSimpleName());
			}
		}
		return true;
	}

	/**
	 * Manually register listeners from an addon.
	 * *NOTE: Requires initial pickup.
	 *
	 * @param e The addon to register.
	 */
	public static boolean enable(EssentialsAddon e) {
		if (e.isActive()) return false;
		dataLog.clear();
		MyEssentialsAPI.getInstance().logInfo(() -> "- Queueing pickup for " + '"' + e.getName() + '"' + " addon information.");
		dataLog.add("[Essentials] - Queueing pickup for " + '"' + e.getName() + '"' + " addon information.");
		e.onEnable();
		List<Listener> a = HandlerList.getRegisteredListeners(JavaPlugin.getProvidingPlugin(EssentialsAddon.class)).stream().sequential().filter(r -> e.getContext().getListeners().contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
		int count = 0;
		for (Listener add : e.getContext().getListeners()) {
			if (a.contains(add)) {
				MyEssentialsAPI.getInstance().logInfo("- (+1) Listener failed to register. Already registered and skipping.");
				dataLog.add(" - (+1) Listener failed to register. Already registered and skipping.");
			} else {
				Bukkit.getPluginManager().registerEvents(add, JavaPlugin.getProvidingPlugin(EssentialsAddon.class));
				count++;
			}
		}
		int count2 = 0;
		for (Class<? extends CommandInput> command : e.getContext().getCommands().values()) {
			try {
				command.getDeclaredConstructor().newInstance();
				count2++;
			} catch (Exception ex) {
				MyEssentialsAPI.getInstance().logInfo("- (+1) Command failed to register. Already registered and skipping.");
				dataLog.add(" - (+1) Command failed to register. Already registered and skipping.");
			}
		}
		if (count > 0) {
			MyEssentialsAPI.getInstance().logInfo("- (+" + count + ") Listener(s) successfully re-registered");
			dataLog.add(" - (+" + count + ") Listener(s) found and re-registered");
		}
		if (count2 > 0) {
			MyEssentialsAPI.getInstance().logInfo("- (+" + count2 + ") Command(s) successfully re-registered");
			dataLog.add(" - (+" + count2 + ") Command(s) found and re-registered");
		}
		return true;
	}

	/**
	 * Register an addon but don't activate listeners ignoring persistence.
	 *
	 * <p>
	 * This will register an addon without checking if its persistent or not
	 * but it won't register any of the listeners within it, to do that
	 * apply {@link EssentialsAddonQuery#register(Class)} directly after this method using
	 * the {@link EssentialsAddonQuery#find(String)} method to re-acquire the addon instance.
	 *
	 * @param aClass The {@link EssentialsAddon} class to pickup.
	 */
	public static void pickup(Class<? extends EssentialsAddon> aClass) {
		try {
			EssentialsAddon addon = aClass.getDeclaredConstructor().newInstance();
			try {
				addon.onLoad();
				addon.register();
			} catch (NoClassDefFoundError e) {
				LabyrinthProvider.getInstance().getLogger().warning(() -> "- You have outdated libraries. Additions for addon " + addon.getName() + " will not work.");
				LabyrinthProvider.getInstance().getLogger().warning("- It's possible this has no effect to you as of this moment so you may be safe to ignore this message.");
			}
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			MyEssentialsAPI.getInstance().logSevere(() -> "- Unable to cast EssentialsAddon to the class " + aClass.getName() + ". This likely means you are not implementing the EssentialsAddon interface for your event class properly.");
			e.printStackTrace();
		}
	}

	/**
	 * Register an addon but don't activate listeners ignoring persistence.
	 *
	 * <p>
	 * This will register an addon without checking if its persistent or not
	 * but it won't register any of the listeners within it, to do that
	 * apply {@link EssentialsAddonQuery#register(Class)} directly after this method using
	 * the {@link EssentialsAddonQuery#find(String)} method to re-acquire the addon instance.
	 *
	 * @param addon The addon to pickup.
	 */
	public static void pickup(EssentialsAddon addon) {
		try {
			addon.onLoad();
			addon.register();
		} catch (NoClassDefFoundError e) {
			LabyrinthProvider.getInstance().getLogger().warning(() -> "- You have outdated libraries. Additions for addon " + addon.getName() + " will not work.");
			LabyrinthProvider.getInstance().getLogger().warning("- It's possible this has no effect to you as of this moment so you may be safe to ignore this message.");
		}
	}

	/**
	 * Register addons in a given package but don't activate listeners ignoring persistence.
	 *
	 * <p>
	 * This will register all found addons in a given package location without checking if they're persistent or not
	 * but it won't register any of the listeners within it, to do that
	 * you need to loop through the total collection of addons using {@link EssentialsAddonQuery#getKnownAddons()}
	 * find the addon's you're looking for and register the listeners using {@link EssentialsAddonQuery#enable(EssentialsAddon)}
	 *
	 * @param plugin      The plugin thats providing the addons.
	 * @param packageName The package location where the {@link EssentialsAddon} addons are located.
	 */
	public static void pickupAll(@NotNull final Plugin plugin, @NotNull final String packageName) {
		Set<Class<?>> classes = Sets.newHashSet();
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(URLDecoder.decode(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8"));
		} catch (IOException e) {
			throw new IllegalStateException("Unable to access jar!", e);
		}
		for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
			String className = jarEntry.getName().replace("/", ".");
			if (className.startsWith(packageName) && className.endsWith(".class")) {
				Class<?> clazz;
				try {
					clazz = Class.forName(className.substring(0, className.length() - 6));
				} catch (ClassNotFoundException e) {
					MyEssentialsAPI.getInstance().logSevere("- Unable to find class" + className + "! Double check package location. See the error below for more information.");
					break;
				}
				if (EssentialsAddon.class.isAssignableFrom(clazz)) {
					classes.add(clazz);
				}
			}
		}
		for (Class<?> aClass : classes) {
			try {
				EssentialsAddon cycle = (EssentialsAddon) aClass.getDeclaredConstructor().newInstance();
				try {
					cycle.onLoad();
					cycle.register();
				} catch (NoClassDefFoundError e) {
					LabyrinthProvider.getInstance().getLogger().warning("- You have outdated libraries. Additions for addon " + cycle.getName() + " will not work.");
					LabyrinthProvider.getInstance().getLogger().warning("- It's possible this has no effect to you as of this moment so you may be safe to ignore this message.");
				}
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				MyEssentialsAPI.getInstance().logSevere("- Unable to cast EssentialsAddon to the class " + aClass.getName() + ". This likely means you are not implementing the EssentialsAddon interface for your event class properly.");
				e.printStackTrace();
				break;
			}
		}
	}

	/**
	 * Register an addon and all of its listeners.
	 *
	 * <p>
	 * You'll want to primarily use this method as it both pick's up the addon and activates it.
	 * No extra steps are required like with {@link EssentialsAddonQuery#pickup(Class)} or {@link EssentialsAddonQuery#pickupAll(Plugin, String)}
	 *
	 * @param addon The addon to activate.
	 */
	public static void register(EssentialsAddon addon) {
		addon.onLoad();
		addon.register();
		if (addon.isPersistent()) {
			MyEssentialsAPI.getInstance().logInfo(" ");
			MyEssentialsAPI.getInstance().logInfo("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			MyEssentialsAPI.getInstance().logInfo("- Addon: " + addon.getName());
			MyEssentialsAPI.getInstance().logInfo("- Author(s): " + Arrays.toString(addon.getAuthors()));
			MyEssentialsAPI.getInstance().logInfo("- Description: " + addon.getDescription());
			MyEssentialsAPI.getInstance().logInfo("- Persistent: (" + addon.isPersistent() + ")");
			MyEssentialsAPI.getInstance().logInfo("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			MyEssentialsAPI.getInstance().logInfo(" ");
			MyEssentialsAPI.getInstance().logInfo("- Listeners: (" + addon.getContext().getListeners().size() + ")");
			addon.onEnable();
			for (Listener addition : addon.getContext().getListeners()) {
				boolean registered = HandlerList.getRegisteredListeners(JavaPlugin.getProvidingPlugin(EssentialsAddon.class)).stream().anyMatch(r -> r.getListener().equals(addition));
				if (!registered) {
					MyEssentialsAPI.getInstance().logInfo("- [" + addon.getName() + "] (+1) Listener " + addition.getClass().getSimpleName() + " loaded");
					Bukkit.getPluginManager().registerEvents(addition, JavaPlugin.getProvidingPlugin(EssentialsAddon.class));
				} else {
					MyEssentialsAPI.getInstance().logInfo("- [" + addon.getName() + "] (-1) Listener " + addition.getClass().getSimpleName() + " already loaded. Skipping.");
				}
			}
			for (Class<? extends CommandInput> command : addon.getContext().getCommands().values()) {
				try {
					command.getDeclaredConstructor().newInstance();
					MyEssentialsAPI.getInstance().logInfo("- [" + addon.getName() + "] (+1) Command " + command.getSimpleName() + " loaded");
				} catch (Exception ex) {
					MyEssentialsAPI.getInstance().logInfo("- (-1) Command " + command.getSimpleName() + " failed to register. Already registered and skipping.");
					dataLog.add(" - (+1) Command " + command.getSimpleName() + " failed to register. Already registered and skipping.");
				}
			}
			addon.active = true;
		} else {
			MyEssentialsAPI.getInstance().logInfo(" ");
			MyEssentialsAPI.getInstance().logInfo("- Addon: " + addon.getName());
			MyEssentialsAPI.getInstance().logInfo("- Author(s): " + Arrays.toString(addon.getAuthors()));
			MyEssentialsAPI.getInstance().logInfo("- Description: " + addon.getDescription());
			MyEssentialsAPI.getInstance().logInfo("- Persistent: (" + addon.isPersistent() + ")");
			addon.remove();
			MyEssentialsAPI.getInstance().logInfo(" ");
			MyEssentialsAPI.getInstance().logInfo("- Listeners: (" + addon.getContext().getListeners().size() + ")");
			for (Listener addition : addon.getContext().getListeners()) {
				MyEssentialsAPI.getInstance().logInfo("- [" + addon.getName() + "] (-1) Listener " + addition.getClass().getSimpleName() + " failed to load due to no persistence.");
			}
			for (Class<? extends CommandInput> command : addon.getContext().getCommands().values()) {
				MyEssentialsAPI.getInstance().logInfo("- [" + addon.getName() + "] (-1) Command " + command.getSimpleName() + " failed to load due to no persistence.");
			}
		}
		MyEssentialsAPI.getInstance().logInfo("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
	}

	/**
	 * Register an addon and all of its listeners.
	 *
	 * <p>
	 * You'll want to primarily use this method as it both pick's up the addon and activates it.
	 * No extra steps are required like with {@link EssentialsAddonQuery#pickup(Class)} or {@link EssentialsAddonQuery#pickupAll(Plugin, String)}
	 *
	 * @param aClass The {@link EssentialsAddon} class to activate.
	 */
	public static void register(Class<? extends EssentialsAddon> aClass) {
		try {
			EssentialsAddon addon = aClass.getDeclaredConstructor().newInstance();
			register(addon);
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			MyEssentialsAPI.getInstance().logSevere("- Unable to cast EventCycle to the class " + aClass.getName() + ". This likely means you are not implementing the EssentialsAddon abstraction for your main class properly.");
			e.printStackTrace();
		}
	}

	/**
	 * Register addons in a given package and all of their listeners.
	 *
	 * <p>
	 * You'll want to primarily use this method as it both pick's up addons in a specified package location of your plugin
	 * and activates them.
	 * <p>
	 * No extra steps are required like with {@link EssentialsAddonQuery#pickup(Class)} or {@link EssentialsAddonQuery#pickupAll(Plugin, String)}
	 *
	 * @param plugin      The plugin that's providing the addons
	 * @param packageName The package location where the {@link EssentialsAddon} addons are located.
	 */
	public static void registerAll(@NotNull final Plugin plugin, @NotNull final String packageName) {
		RegistryData<EssentialsAddon> data = new Registry<>(EssentialsAddon.class).source(plugin)
				.filter(packageName)
				.operate(addon -> {
				});
		MyEssentialsAPI.getInstance().logInfo("- Found (" + data.getData().size() + ") addon(s)");
		for (EssentialsAddon e : data.getData()) {
			register(e);
		}
	}

	@Note("You don't need to use this!")
	private static void runInjectionProcedures(JavaPlugin plugin) {
		if (!plugin.getName().equals("myEssentials")) throw new IllegalArgumentException("Invalid plugin instance!");
		Procedure.request(() -> JavaPlugin.class).next(instance -> {
			File file = FileList.search(instance).get("dummy", "Addons").getRoot().getParent().getParentFile();
			int amount = 0;
			List<EssentialsAddon> toBeRegied = new ArrayList<>();
			for (File f : file.listFiles()) {
				if (f.isDirectory()) continue;
				try {
					EssentialsAddon addon = new EssentialsClassLoader(f).getMainClass();
					instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					instance.getLogger().info("- Injected: " + addon.getName() + " v" + addon.getVersion());
					instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					toBeRegied.add(addon);
					amount++;
				} catch (IOException | InvalidAddonException e) {
					e.printStackTrace();
				}
			}
			instance.getLogger().info("- (" + amount + ") addon(s) were injected into cache.");
			toBeRegied.forEach(EssentialsAddonQuery::register);
		}).run(plugin).deploy();

	}

	/**
	 * Get the data-log, When addon management occurs you can view progress reports from this.
	 *
	 * @return The most recent progress report.
	 */
	public static String[] getDataLog() {
		return dataLog.toArray(new String[0]);
	}

}
