package com.github.sanctum.myessentials.util;

import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.gui.InventoryRows;
import com.github.sanctum.labyrinth.gui.shared.SharedBuilder;
import com.github.sanctum.myessentials.api.AddonQuery;
import com.github.sanctum.myessentials.api.EssentialsAddon;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ReloadImpl {

	private final Plugin plugin;

	protected ReloadImpl(@NotNull Plugin plugin) {
		this.plugin = plugin;
	}

	public static ReloadImpl get(Plugin plugin) {
		return new ReloadImpl(plugin);
	}

	public void onEnable(ClassLoader classLoader) {
		if (System.getProperty("OLD") != null && System.getProperty("OLD").equals("TRUE")) {
			plugin.getLogger().severe("- RELOAD DETECTED! Shutting down...");
			plugin.getLogger().severe("      ██╗");
			plugin.getLogger().severe("  ██╗██╔╝");
			plugin.getLogger().severe("  ╚═╝██║ ");
			plugin.getLogger().severe("  ██╗██║ ");
			plugin.getLogger().severe("  ╚═╝╚██╗");
			plugin.getLogger().severe("      ╚═╝");
			plugin.getLogger().severe("- (You are not supported in the case of corrupt data)");
			plugin.getLogger().severe("- (Reloading is NEVER safe and you should always restart instead.)");
			FileManager file = MyEssentialsAPI.getInstance().getFileList().find("ignore", "");
			String location = new Date().toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE);
			List<String> toAdd = new ArrayList<>(file.getConfig().getStringList(location));
			toAdd.add("RELOAD DETECTED! Shutting down...");
			toAdd.add("      ██╗");
			toAdd.add("  ██╗██╔╝");
			toAdd.add("  ╚═╝██║ ");
			toAdd.add("  ██╗██║ ");
			toAdd.add("  ╚═╝╚██╗");
			toAdd.add("      ╚═╝");
			toAdd.add("(You are not supported in the case of corrupt data)");
			toAdd.add("(Reloading is NEVER safe and you should always restart instead.)");
			file.getConfig().set(location, toAdd);
			file.saveConfig();
			Bukkit.getPluginManager().disablePlugin(plugin);
		} else {
			System.setProperty("OLD", "FALSE");
		}
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			SharedBuilder.create(plugin, "MyVault-" + player.getUniqueId().toString(), player.getName() + " Vault", InventoryRows.THREE.getSlotCount());
		}
		try {
			injectAddons(classLoader);
		} catch (Exception e) {
			plugin.getLogger().severe("- An unexpected file type was found in the addon folder, remove it then restart.");
		}
	}

	public void onDisable() {
		if (System.getProperty("OLD").equals("FALSE")) {
			System.setProperty("OLD", "TRUE");
		}
	}

	private void injectAddons(ClassLoader loader) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Set<Class<?>> classes = Sets.newHashSet();
		FileManager check = MyEssentialsAPI.getInstance().getAddonFile("Test", "");
		File parent = check.getFile().getParentFile();
		for (File f : parent.listFiles()) {
			if (f.isFile()) {
				JarFile test = new JarFile(f);
				URLClassLoader classLoader = (URLClassLoader) loader;
				Class<?> urlClassLoaderClass = URLClassLoader.class;
				Method method = urlClassLoaderClass.getDeclaredMethod("addURL", URL.class);
				method.setAccessible(true);
				method.invoke(classLoader, f.toURI().toURL());
				for (JarEntry jarEntry : Collections.list(test.entries())) {
					String entry = jarEntry.getName().replace("/", ".");
					if (entry.endsWith(".class")) {
						Class<?> clazz = null;
						final String substring = entry.substring(0, entry.length() - 6);
						try {
							clazz = Class.forName(substring);
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
						assert clazz != null;
						if (EssentialsAddon.class.isAssignableFrom(clazz)) {
							classes.add(clazz);
						}
					}
				}
			}
		}
		for (Class<?> aClass : classes) {
			try {
				final EssentialsAddon addon = (EssentialsAddon) aClass.getDeclaredConstructor().newInstance();
				if (!AddonQuery.getRegisteredAddons().contains(addon.getAddonName())) {
					AddonQuery.register(addon);
				}
			} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e) {
				// unable to load addon
				e.printStackTrace();
			}
		}
	}


}
