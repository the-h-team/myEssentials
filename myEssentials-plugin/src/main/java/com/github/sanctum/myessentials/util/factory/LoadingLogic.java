package com.github.sanctum.myessentials.util.factory;

import com.github.sanctum.labyrinth.annotation.Note;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.myessentials.api.EssentialsAddonQuery;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class LoadingLogic {

	private final Plugin plugin;

	LoadingLogic(@NotNull Plugin plugin) {
		this.plugin = plugin;
	}

	public void onEnable() {
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
			FileManager file = MyEssentialsAPI.getInstance().getFileList().get("ignore", FileType.JSON);
			String location = new Date().toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE);
			List<String> toAdd = new ArrayList<>(file.getRoot().getStringList(location));
			toAdd.add("RELOAD DETECTED! Shutting down...");
			toAdd.add("      ██╗");
			toAdd.add("  ██╗██╔╝");
			toAdd.add("  ╚═╝██║ ");
			toAdd.add("  ██╗██║ ");
			toAdd.add("  ╚═╝╚██╗");
			toAdd.add("      ╚═╝");
			toAdd.add("(You are not supported in the case of corrupt data)");
			toAdd.add("(Reloading is NEVER safe and you should always restart instead.)");
			file.write(t -> t.set(location, toAdd));
			Bukkit.getPluginManager().disablePlugin(plugin);
		} else {
			System.setProperty("OLD", "FALSE");
		}
		injectAddons();
	}

	public void onDisable() {
		if (System.getProperty("OLD").equals("FALSE")) {
			System.setProperty("OLD", "TRUE");
		}
		EssentialsAddonQuery.getKnownAddons().forEach(a -> {
			a.remove();
			plugin.getLogger().info("- Disabling addon " + '"' + a.getName() + '"');
		});
	}

	private void injectAddons() {
		Arrays.stream(EssentialsAddonQuery.class.getDeclaredMethods()).filter((m) -> Modifier.isStatic(m.getModifiers()) && Modifier.isProtected(m.getModifiers()) && m.getName().equals("runInjectionProcedures")).findFirst().ifPresent(m -> {
			try {
				try {
					m.setAccessible(true);
				} catch (Exception ignored) {
				}
				JavaPlugin pl = (JavaPlugin) plugin;
				m.invoke(null, pl);
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		});
	}

	@Note("You don't need to use this!")
	public static LoadingLogic get(Plugin plugin) {
		if (!plugin.getName().equals("myEssentials")) throw new IllegalArgumentException("Invalid plugin instance!");
		return new LoadingLogic(plugin);
	}
}
