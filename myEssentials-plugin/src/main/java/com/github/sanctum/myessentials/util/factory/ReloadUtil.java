package com.github.sanctum.myessentials.util.factory;

import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.gui.InventoryRows;
import com.github.sanctum.labyrinth.gui.shared.SharedBuilder;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.api.AddonQuery;
import com.github.sanctum.myessentials.api.EssentialsAddon;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class ReloadUtil {

	private final Plugin plugin;

	private ReloadUtil(@NotNull Plugin plugin) {
		this.plugin = plugin;
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
		injectAddons(classLoader);
	}

	public void onDisable() {
		if (System.getProperty("OLD").equals("FALSE")) {
			System.setProperty("OLD", "TRUE");
		}
	}

	private void injectAddons(ClassLoader loader) {

		try {
			new Registry.File<>(EssentialsAddon.class)
					.use(Essentials.getInstance())
					.provide(loader)
					.from("Addons")
					.operate(addon -> {
						if (!AddonQuery.getRegisteredAddons().contains(addon.getAddonName())) {
							AddonQuery.register(addon);
						}
					});
		} catch (Exception e) {
			plugin.getLogger().severe("- An unexpected file type was found in the addon folder, remove it then restart.");
		}
	}

	public static ReloadUtil get(Plugin plugin) {
		return new ReloadUtil(plugin);
	}
}
