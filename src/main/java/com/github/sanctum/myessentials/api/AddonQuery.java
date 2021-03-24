package com.github.sanctum.myessentials.api;

import com.github.sanctum.labyrinth.Labyrinth;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.util.CommandRegistration;
import com.google.common.collect.Sets;
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
import org.jetbrains.annotations.NotNull;

public class AddonQuery {

	private static final Collection<EssentialsAddon> ESSENTIALS_ADDONS = new HashSet<>();

	private static final List<String> DATA_LOG = new ArrayList<>();

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
		return getKnownAddons().stream().filter(c -> c.getAddonName().equals(name)).findFirst().orElse(null);
	}

	/**
	 * Get the list of currently registered but disabled addons by name.
	 *
	 * @return All disabled persistent addons by name.
	 */
	public static List<String> getDisabledAddons() {
		List<String> array = new ArrayList<>();
		for (EssentialsAddon e : ESSENTIALS_ADDONS) {
			List<Listener> a = HandlerList.getRegisteredListeners(Essentials.getInstance()).stream().sequential().filter(r -> e.getListeners().contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
			if (a.isEmpty()) {
				array.add(e.getAddonName());
			}
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
			List<Listener> a = HandlerList.getRegisteredListeners(Essentials.getInstance()).stream().sequential().filter(r -> e.getListeners().contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
			if (!a.isEmpty()) {
				array.add(e.getAddonName());
			}
		}
		return array;
	}

	/**
	 * Get the list of currently persistent registered addons by name.
	 *
	 * @return All persistent registered addons.
	 */
	public static List<String> getRegisteredAddons() {
		return CompletableFuture.supplyAsync(() -> getKnownAddons().stream().sequential().map(EssentialsAddon::getAddonName).collect(Collectors.toList())).join();
	}

	/**
	 * Manually un-register listeners from an addon.
	 * *NOTE: Requires initial pickup and registration.
	 *
	 * @param e The addon to un-register.
	 */
	public static void unregisterAll(EssentialsAddon e) {
		DATA_LOG.clear();
		Essentials.getInstance().getLogger().info("- Queueing removal of " + '"' + e.getAddonName() + '"' + " addon information.");
		DATA_LOG.add("[Essentials] - Queueing removal of " + '"' + e.getAddonName() + '"' + " addon information.");
		List<Listener> a = HandlerList.getRegisteredListeners(Essentials.getInstance()).stream().sequential().filter(r -> e.getListeners().contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
		int count = 0;
		if (!a.isEmpty()) {
			DATA_LOG.add(" - Unregistering addon from handler-list.");
			for (Listener l : a) {
				HandlerList.unregisterAll(l);
				count++;
			}
			if (count > 0) {
				Essentials.getInstance().getLogger().info("- (+" + count + ") Listener(s) successfully un-registered");
				DATA_LOG.add(" - (+" + count + ") Listener(s) found and un-registered");
			}
		} else {
			Essentials.getInstance().getLogger().info("- Failed to un-register events. Addon not currently running.");
			DATA_LOG.add(" - Failed to un-register events. Addon not currently running.");
		}
		for (String command : e.getCommands().keySet()) {
			Command c = CommandBuilder.getRegistration(command);
			c.unregister(CommandBuilder.getCommandMap());
		}
	}

	/**
	 * Manually register listeners from an addon.
	 * *NOTE: Requires initial pickup.
	 *
	 * @param e The addon to register.
	 */
	public static void registerAll(EssentialsAddon e) {
		DATA_LOG.clear();
		Essentials.getInstance().getLogger().info("- Queueing pickup for " + '"' + e.getAddonName() + '"' + " addon information.");
		DATA_LOG.add("[Essentials] - Queueing pickup for " + '"' + e.getAddonName() + '"' + " addon information.");
		List<Listener> a = HandlerList.getRegisteredListeners(Essentials.getInstance()).stream().sequential().filter(r -> e.getListeners().contains(r.getListener())).map(RegisteredListener::getListener).collect(Collectors.toList());
		int count = 0;
		for (Listener add : e.getListeners()) {
			if (a.contains(add)) {
				Essentials.getInstance().getLogger().info("- (+1) Listener failed to register. Already registered and skipping.");
				DATA_LOG.add(" - (+1) Listener failed to register. Already registered and skipping.");
			} else {
				Bukkit.getPluginManager().registerEvents(add, Essentials.getInstance());
				count++;
			}
		}
		int count2 = 0;
		for (Class<? extends CommandBuilder> command : e.getCommands().values()) {
			try {
				CommandRegistration.inject(command);
				count2++;
			} catch (Exception ex) {
				Essentials.getInstance().getLogger().info("- (+1) Command failed to register. Already registered and skipping.");
				DATA_LOG.add(" - (+1) Command failed to register. Already registered and skipping.");
			}
		}
		if (count > 0) {
			Essentials.getInstance().getLogger().info("- (+" + count + ") Listener(s) successfully re-registered");
			DATA_LOG.add(" - (+" + count + ") Listener(s) found and re-registered");
		}
		if (count2 > 0) {
			Essentials.getInstance().getLogger().info("- (+" + count2 + ") Command(s) successfully re-registered");
			DATA_LOG.add(" - (+" + count2 + ") Command(s) found and re-registered");
		}
	}

	/**
	 * Register an addon but don't activate listeners ignoring persistence.
	 *
	 * <p>
	 * This will register an addon without checking if its persistent or not
	 * but it won't register any of the listeners within it, to do that
	 * apply {@link AddonQuery#register(Class)} directly after this method using
	 * the {@link AddonQuery#find(String)} method to re-acquire the addon instance.
	 *
	 * @param aClass The {@link EssentialsAddon} class to pickup.
	 */
	public static void pickup(Class<? extends EssentialsAddon> aClass) {
		try {
			EssentialsAddon addon = aClass.newInstance();
			try {
				addon.apply();
				addon.register();
			} catch (NoClassDefFoundError e) {
				Labyrinth.getInstance().getLogger().warning("- You have outdated libraries. Additions for addon " + addon.getAddonName() + " will not work.");
				Labyrinth.getInstance().getLogger().warning("- It's possible this has no effect to you as of this moment so you may be safe to ignore this message.");
			}
		} catch (InstantiationException | IllegalAccessException e) {
			Essentials.getInstance().getLogger().severe("- Unable to cast EssentialsAddon to the class " + aClass.getName() + ". This likely means you are not implementing the EssentialsAddon interface for your event class properly.");
			e.printStackTrace();
		}
	}

	/**
	 * Register addons in a given package but don't activate listeners ignoring persistence.
	 *
	 * <p>
	 * This will register all found addons in a given package location without checking if they're persistent or not
	 * but it won't register any of the listeners within it, to do that
	 * you need to loop through the total collection of addons using {@link AddonQuery#getKnownAddons()}
	 * find the addon's you're looking for and register the listeners using {@link AddonQuery#registerAll(EssentialsAddon)}
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
			e.printStackTrace();
		}
		for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
			String className = jarEntry.getName().replace("/", ".");
			if (className.startsWith(packageName) && className.endsWith(".class")) {
				Class<?> clazz;
				try {
					clazz = Class.forName(className.substring(0, className.length() - 6));
				} catch (ClassNotFoundException e) {
					Essentials.getInstance().getLogger().severe("- Unable to find class" + className + "! Double check package location. See the error below for more information.");
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
					cycle.apply();
					cycle.register();
				} catch (NoClassDefFoundError e) {
					Labyrinth.getInstance().getLogger().warning("- You have outdated libraries. Additions for addon " + cycle.getAddonName() + " will not work.");
					Labyrinth.getInstance().getLogger().warning("- It's possible this has no effect to you as of this moment so you may be safe to ignore this message.");
				}
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				Essentials.getInstance().getLogger().severe("- Unable to cast EssentialsAddon to the class " + aClass.getName() + ". This likely means you are not implementing the EssentialsAddon interface for your event class properly.");
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
	 * No extra steps are required like with {@link AddonQuery#pickup(Class)} or {@link AddonQuery#pickupAll(Plugin, String)}
	 *
	 * @param cycle The {@link EssentialsAddon} class to activate.
	 */
	public static void register(Class<? extends EssentialsAddon> cycle) {
		try {
			EssentialsAddon addon = cycle.newInstance();
			addon.apply();
			addon.register();
			if (addon.persist()) {

				Essentials.getInstance().getLogger().info(" ");
				Essentials.getInstance().getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				Essentials.getInstance().getLogger().info("- Addon: " + addon.getAddonName());
				Essentials.getInstance().getLogger().info("- Author(s): " + Arrays.toString(addon.getAuthors()));
				Essentials.getInstance().getLogger().info("- Description: " + addon.getAddonDescription());
				Essentials.getInstance().getLogger().info("- Persistent: (" + addon.persist() + ")");
				Essentials.getInstance().getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				Essentials.getInstance().getLogger().info(" ");
				Essentials.getInstance().getLogger().info("- Listeners: (" + addon.getListeners().size() + ")");
				for (Listener addition : addon.getListeners()) {
					boolean registered = HandlerList.getRegisteredListeners(Essentials.getInstance()).stream().anyMatch(r -> r.getListener().equals(addition));
					if (!registered) {
						Essentials.getInstance().getLogger().info("- [" + addon.getAddonName() + "] (+1) EventHandler " + addition.getClass().getSimpleName() + " loaded");
						Bukkit.getPluginManager().registerEvents(addition, Essentials.getInstance());
					} else {
						Essentials.getInstance().getLogger().info("- [" + addon.getAddonName() + "] (-1) EventHandler " + addition.getClass().getSimpleName() + " already loaded. Skipping.");
					}
				}
				for (Class<? extends CommandBuilder> command : addon.getCommands().values()) {
					try {
						CommandRegistration.inject(command);
						Essentials.getInstance().getLogger().info("- [" + addon.getAddonName() + "] (+1) Command " + command.getSimpleName() + " loaded");
					} catch (Exception ex) {
						Essentials.getInstance().getLogger().info("- (-1) Command " + command.getSimpleName() + " failed to register. Already registered and skipping.");
						DATA_LOG.add(" - (+1) Command " + command.getSimpleName() + " failed to register. Already registered and skipping.");
					}
				}
			} else {
				Essentials.getInstance().getLogger().info(" ");
				Essentials.getInstance().getLogger().info("- Addon: " + addon.getAddonName());
				Essentials.getInstance().getLogger().info("- Author(s): " + Arrays.toString(addon.getAuthors()));
				Essentials.getInstance().getLogger().info("- Description: " + addon.getAddonDescription());
				Essentials.getInstance().getLogger().info("- Persistent: (" + addon.persist() + ")");
				addon.remove();
				Essentials.getInstance().getLogger().info(" ");
				Essentials.getInstance().getLogger().info("- Listeners: (" + addon.getListeners().size() + ")");
				for (Listener addition : addon.getListeners()) {
					Essentials.getInstance().getLogger().info("- [" + addon.getAddonName() + "] (-1) Listener " + addition.getClass().getSimpleName() + " failed to load due to no persistence.");
				}
				for (Class<? extends CommandBuilder> command : addon.getCommands().values()) {
					Essentials.getInstance().getLogger().info("- [" + addon.getAddonName() + "] (-1) Command " + command.getSimpleName() + " failed to load due to no persistence.");
				}
			}
		} catch (InstantiationException | IllegalAccessException e) {
			Essentials.getInstance().getLogger().severe("- Unable to cast EventCycle to the class " + cycle.getName() + ". This likely means you are not implementing the EventCycle interface for your event class properly.");
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
	 * No extra steps are required like with {@link AddonQuery#pickup(Class)} or {@link AddonQuery#pickupAll(Plugin, String)}
	 *
	 * @param plugin The plugin that's providing the addons
	 * @param packageName The package location where the {@link EssentialsAddon} addons are located.
	 */
	public static void registerAll(@NotNull final Plugin plugin, @NotNull final String packageName) {
		Set<Class<?>> classes = Sets.newHashSet();
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(URLDecoder.decode(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
			String className = jarEntry.getName().replace("/", ".");
			if (className.startsWith(packageName) && className.endsWith(".class")) {
				Class<?> clazz;
				try {
					clazz = Class.forName(className.substring(0, className.length() - 6));
				} catch (ClassNotFoundException e) {
					Essentials.getInstance().getLogger().severe("- Unable to find class" + className + "! Double check package location. See the error below for more information.");
					break;
				}
				if (EssentialsAddon.class.isAssignableFrom(clazz)) {
					classes.add(clazz);
				}
			}
		}
		Collection<EssentialsAddon> additions = new HashSet<>();
		for (Class<?> aClass : classes) {
			try {
				EssentialsAddon addon = (EssentialsAddon) aClass.getDeclaredConstructor().newInstance();
				addon.apply();
				addon.register();
				additions.add(addon);
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				Essentials.getInstance().getLogger().severe("- Unable to cast EventCycle to the class " + aClass.getName() + ". This likely means you are not implementing the EventCycle interface for your event class properly.");
				e.printStackTrace();
				break;
			}
		}
		Essentials.getInstance().getLogger().info("- Found (" + additions.size() + ") event cycle(s)");
		for (EssentialsAddon e : additions) {
			if (e.persist()) {

				Essentials.getInstance().getLogger().info(" ");
				Essentials.getInstance().getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				Essentials.getInstance().getLogger().info("- Addon: " + e.getAddonName());
				Essentials.getInstance().getLogger().info("- Author(s): " + Arrays.toString(e.getAuthors()));
				Essentials.getInstance().getLogger().info("- Description: " + e.getAddonDescription());
				Essentials.getInstance().getLogger().info("- Persistent: (" + e.persist() + ")");
				Essentials.getInstance().getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				Essentials.getInstance().getLogger().info(" ");
				Essentials.getInstance().getLogger().info("- Listeners: (" + e.getListeners().size() + ")");
				for (Listener addition : e.getListeners()) {
					boolean registered = HandlerList.getRegisteredListeners(Essentials.getInstance()).stream().anyMatch(r -> r.getListener().equals(addition));
					if (!registered) {
						Essentials.getInstance().getLogger().info("- [" + e.getAddonName() + "] (+1) Listener " + addition.getClass().getSimpleName() + " loaded");
						Bukkit.getPluginManager().registerEvents(addition, Essentials.getInstance());
					} else {
						Essentials.getInstance().getLogger().info("- [" + e.getAddonName() + "] (-1) Listener " + addition.getClass().getSimpleName() + " already loaded. Skipping.");
					}
				}
				for (Class<? extends CommandBuilder> command : e.getCommands().values()) {
					try {
						CommandRegistration.inject(command);
						Essentials.getInstance().getLogger().info("- [" + e.getAddonName() + "] (+1) Command " + command.getSimpleName() + " loaded");
					} catch (Exception ex) {
						Essentials.getInstance().getLogger().info("- (-1) Command " + command.getSimpleName() + " failed to register. Already registered and skipping.");
						DATA_LOG.add(" - (+1) Command " + command.getSimpleName() + " failed to register. Already registered and skipping.");
					}
				}
			} else {
				Essentials.getInstance().getLogger().info(" ");
				Essentials.getInstance().getLogger().info("- Addon: " + e.getAddonName());
				Essentials.getInstance().getLogger().info("- Author(s): " + Arrays.toString(e.getAuthors()));
				Essentials.getInstance().getLogger().info("- Description: " + e.getAddonDescription());
				Essentials.getInstance().getLogger().info("- Persistent: (" + e.persist() + ")");
				e.remove();
				Essentials.getInstance().getLogger().info(" ");
				Essentials.getInstance().getLogger().info("- Listeners: (" + e.getListeners().size() + ")");
				for (Listener addition : e.getListeners()) {
					Essentials.getInstance().getLogger().info("- [" + e.getAddonName() + "] (-1) Listener " + addition.getClass().getSimpleName() + " failed to load due to no persistence.");
				}
				for (Class<? extends CommandBuilder> command : e.getCommands().values()) {
					Essentials.getInstance().getLogger().info("- [" + e.getAddonName() + "] (-1) Command " + command.getSimpleName() + " failed to load due to no persistence.");
				}
			}

		}
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
