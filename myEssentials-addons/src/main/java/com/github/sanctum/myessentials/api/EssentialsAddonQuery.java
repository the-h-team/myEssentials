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
import com.github.sanctum.labyrinth.annotation.Note;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.data.RegistryData;
import com.github.sanctum.labyrinth.task.Procedure;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.CommandData;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class EssentialsAddonQuery {

	private static final Collection<EssentialsAddon> ESSENTIALS_ADDONS = new HashSet<>();

	private static final List<String> DATA_LOG = new ArrayList<>();

	private static EssentialsAddonQuery instance;
	private final MyEssentialsAPI api = MyEssentialsAPI.getInstance();
	private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(api.getClass());

	// lazy grab the api+plugin
	private EssentialsAddonQuery() {
		instance = this;
	}

	/**
	 * Get the collection of persistently picked up addons.
	 *
	 * *NOTE: If the addon doesn't persist it won't be in cache.
	 *
	 * @return A collection of enabled/disabled registered addons.
	 */
	public static Collection<EssentialsAddon> getKnownAddons() {
		return CompletableFuture.supplyAsync(() -> ESSENTIALS_ADDONS).join();
	}

	/**
	 * Find a persistently picked up addon by its name.
	 *
	 * @param name The name of the addon to find.
	 * @return An {@link EssentialsAddon} object.
	 */
	public static EssentialsAddon find(String name) {
		return getKnownAddons().stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
	}

	/**
	 * Get the list of currently registered but disabled addons by name.
	 *
	 * @return All disabled persistent addons by name.
	 */
	public static List<String> getDisabledAddons() {
		List<String> array = new ArrayList<>();
		for (EssentialsAddon e : ESSENTIALS_ADDONS) {
			if (!e.isActive()) array.add(e.getName());
		}
		return array;
	}

	/**
	 * Get the list of currently registered but enabled addons by name.
	 *
	 * @return All enabled persistent addons by name.
	 */
	public static List<String> getEnabledAddons() {
		List<String> array = new ArrayList<>();
		for (EssentialsAddon e : ESSENTIALS_ADDONS) {
			if (e.isActive()) array.add(e.getName());
		}
		return array;
	}

	/**
	 * Get the list of currently persistent registered addons by name.
	 *
	 * @return All persistent registered addons.
	 */
	public static List<String> getRegisteredAddons() {
		return CompletableFuture.supplyAsync(() -> getKnownAddons().stream().sequential().map(EssentialsAddon::getName).collect(Collectors.toList())).join();
	}

	/**
	 * Manually un-register listeners from an addon.
	 * *NOTE: Requires initial pickup and registration.
	 *
	 * @param e The addon to un-register.
	 */
	public static boolean disable(EssentialsAddon e) {
		if (!e.isActive()) return false;
		DATA_LOG.clear();
		if (instance == null) new EssentialsAddonQuery();
		instance.api.logInfo("- Queueing removal of " + '"' + e.getName() + '"' + " addon information.");
		DATA_LOG.add("[Essentials] - Queueing removal of " + '"' + e.getName() + '"' + " addon information.");
		e.onDisable();
		List<Listener> a = HandlerList.getRegisteredListeners(instance.plugin).stream()
				.filter(r -> e.getContext().getListeners().contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
		int count = 0;
		if (!a.isEmpty()) {
			DATA_LOG.add(" - Unregistering addon from handler-list.");
			for (Listener l : a) {
				HandlerList.unregisterAll(l);
				count++;
			}
			if (count > 0) {
				instance.api.logInfo("- (+" + count + ") Listener(s) successfully un-registered");
				DATA_LOG.add(" - (+" + count + ") Listener(s) found and un-registered");
			}
		} else {
			instance.api.logInfo("- Failed to un-register events. None currently running.");
			DATA_LOG.add(" - Failed to un-register events. None currently running.");
		}
		for (CommandData command : e.getContext().getCommands().keySet()) {
			Command c = instance.api.getRegistration(command);
			if (c == null) continue;
			if (c.isRegistered()) {
				instance.api.unregisterCommand(c);
				instance.api.logInfo(() -> "- Successfully un-registered command " + c.getClass().getSimpleName());
				DATA_LOG.add(" - Successfully un-registered command " + c.getClass().getSimpleName());
			} else {
				instance.api.logInfo(() -> "- Failed to un-register command " + c.getClass().getSimpleName());
				DATA_LOG.add(" - Failed to un-register command " + c.getClass().getSimpleName());
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
		DATA_LOG.clear();
		if (instance == null) new EssentialsAddonQuery();
		instance.api.logInfo(() -> "- Queueing pickup for " + '"' + e.getName() + '"' + " addon information.");
		DATA_LOG.add("[Essentials] - Queueing pickup for " + '"' + e.getName() + '"' + " addon information.");
		e.onEnable();
		List<Listener> a = HandlerList.getRegisteredListeners(instance.plugin).stream().sequential().filter(r -> e.getContext().getListeners().contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
		int count = 0;
		for (Listener add : e.getContext().getListeners()) {
			if (a.contains(add)) {
				instance.api.logInfo("- (+1) Listener failed to register. Already registered and skipping.");
				DATA_LOG.add(" - (+1) Listener failed to register. Already registered and skipping.");
			} else {
				Bukkit.getPluginManager().registerEvents(add, instance.plugin);
				count++;
			}
		}
		int count2 = 0;
		for (Class<? extends CommandBuilder> command : e.getContext().getCommands().values()) {
			try {
				command.getDeclaredConstructor().newInstance();
				count2++;
			} catch (Exception ex) {
				instance.api.logInfo("- (+1) Command failed to register. Already registered and skipping.");
				DATA_LOG.add(" - (+1) Command failed to register. Already registered and skipping.");
			}
		}
		if (count > 0) {
			instance.api.logInfo("- (+" + count + ") Listener(s) successfully re-registered");
			DATA_LOG.add(" - (+" + count + ") Listener(s) found and re-registered");
		}
		if (count2 > 0) {
			instance.api.logInfo("- (+" + count2 + ") Command(s) successfully re-registered");
			DATA_LOG.add(" - (+" + count2 + ") Command(s) found and re-registered");
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
			EssentialsAddon addon = aClass.newInstance();
			try {
				addon.onLoad();
				addon.register();
			} catch (NoClassDefFoundError e) {
				LabyrinthProvider.getInstance().getLogger().warning(() -> "- You have outdated libraries. Additions for addon " + addon.getName() + " will not work.");
				LabyrinthProvider.getInstance().getLogger().warning("- It's possible this has no effect to you as of this moment so you may be safe to ignore this message.");
			}
		} catch (InstantiationException | IllegalAccessException e) {
			if (instance == null) new EssentialsAddonQuery();
			instance.api.logSevere(() -> "- Unable to cast EssentialsAddon to the class " + aClass.getName() + ". This likely means you are not implementing the EssentialsAddon interface for your event class properly.");
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
	 * @param plugin The plugin thats providing the addons.
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
		if (instance == null) new EssentialsAddonQuery();
		for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
			String className = jarEntry.getName().replace("/", ".");
			if (className.startsWith(packageName) && className.endsWith(".class")) {
				Class<?> clazz;
				try {
					clazz = Class.forName(className.substring(0, className.length() - 6));
				} catch (ClassNotFoundException e) {
					instance.api.logSevere("- Unable to find class" + className + "! Double check package location. See the error below for more information.");
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
				instance.api.logSevere("- Unable to cast EssentialsAddon to the class " + aClass.getName() + ". This likely means you are not implementing the EssentialsAddon interface for your event class properly.");
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
		if (instance == null) new EssentialsAddonQuery();
		if (addon.isStaged()) {
			instance.api.logInfo(" ");
			instance.api.logInfo("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			instance.api.logInfo("- Addon: " + addon.getName());
			instance.api.logInfo("- Author(s): " + Arrays.toString(addon.getAuthors()));
			instance.api.logInfo("- Description: " + addon.getDescription());
			instance.api.logInfo("- Persistent: (" + addon.isStaged() + ")");
			instance.api.logInfo("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			instance.api.logInfo(" ");
			instance.api.logInfo("- Listeners: (" + addon.getContext().getListeners().size() + ")");
			addon.onEnable();
			for (Listener addition : addon.getContext().getListeners()) {
				boolean registered = HandlerList.getRegisteredListeners(instance.plugin).stream().anyMatch(r -> r.getListener().equals(addition));
				if (!registered) {
					instance.api.logInfo("- [" + addon.getName() + "] (+1) Listener " + addition.getClass().getSimpleName() + " loaded");
					Bukkit.getPluginManager().registerEvents(addition, instance.plugin);
				} else {
					instance.api.logInfo("- [" + addon.getName() + "] (-1) Listener " + addition.getClass().getSimpleName() + " already loaded. Skipping.");
				}
			}
			for (Class<? extends CommandBuilder> command : addon.getContext().getCommands().values()) {
				try {
					command.getDeclaredConstructor().newInstance();
					instance.api.logInfo("- [" + addon.getName() + "] (+1) Command " + command.getSimpleName() + " loaded");
				} catch (Exception ex) {
					instance.api.logInfo("- (-1) Command " + command.getSimpleName() + " failed to register. Already registered and skipping.");
					DATA_LOG.add(" - (+1) Command " + command.getSimpleName() + " failed to register. Already registered and skipping.");
				}
			}
			addon.active = true;
		} else {
			instance.api.logInfo(" ");
			instance.api.logInfo("- Addon: " + addon.getName());
			instance.api.logInfo("- Author(s): " + Arrays.toString(addon.getAuthors()));
			instance.api.logInfo("- Description: " + addon.getDescription());
			instance.api.logInfo("- Persistent: (" + addon.isStaged() + ")");
			addon.remove();
			instance.api.logInfo(" ");
			instance.api.logInfo("- Listeners: (" + addon.getContext().getListeners().size() + ")");
			for (Listener addition : addon.getContext().getListeners()) {
				instance.api.logInfo("- [" + addon.getName() + "] (-1) Listener " + addition.getClass().getSimpleName() + " failed to load due to no persistence.");
			}
			for (Class<? extends CommandBuilder> command : addon.getContext().getCommands().values()) {
				instance.api.logInfo("- [" + addon.getName() + "] (-1) Command " + command.getSimpleName() + " failed to load due to no persistence.");
			}
		}
		instance.api.logInfo("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
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
			instance.api.logSevere("- Unable to cast EventCycle to the class " + aClass.getName() + ". This likely means you are not implementing the EssentialsAddon abstraction for your main class properly.");
			e.printStackTrace();
		}
	}

	/**
	 * Register addons in a given package and all of their listeners.
	 *
	 * <p>
	 * You'll want to primarily use this method as it both pick's up addons in a specified package location of your plugin
	 * and activates them.
	 *
	 * No extra steps are required like with {@link EssentialsAddonQuery#pickup(Class)} or {@link EssentialsAddonQuery#pickupAll(Plugin, String)}
	 *
	 * @param plugin The plugin that's providing the addons
	 * @param packageName The package location where the {@link EssentialsAddon} addons are located.
	 */
	public static void registerAll(@NotNull final Plugin plugin, @NotNull final String packageName) {

		RegistryData<EssentialsAddon> data = new Registry<>(EssentialsAddon.class)
				.source(plugin)
				.pick(packageName)
				.operate(addon -> {
				});
		instance.api.logInfo("- Found (" + data.getData().size() + ") addon(s)");
		for (EssentialsAddon e : data.getData()) {
			register(e);
		}

	}

	@Note("You don't need to use this!")
	protected static void runInjectionProcedures(JavaPlugin plugin) {
		if (!plugin.getName().equals("myEssentials")) throw new IllegalArgumentException("Invalid plugin instance!");
		Procedure.request(() -> JavaPlugin.class).next(instance -> {
			File file = FileList.search(instance).get("dummy", "Addons").getRoot().getParent().getParentFile();
			int amount = 0;
			List<EssentialsAddon> toBeRegied = new ArrayList<>();
			for (File f : file.listFiles()) {
				if (f.isDirectory()) continue;
				try {
					EssentialsAddon addon = new EssentialsAddonClassLoader(f).addon;
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
		return DATA_LOG.toArray(new String[0]);
	}

}
