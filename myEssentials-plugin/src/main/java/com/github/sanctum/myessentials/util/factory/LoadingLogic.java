package com.github.sanctum.myessentials.util.factory;

import com.github.sanctum.labyrinth.annotation.Note;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.data.JsonAdapter;
import com.github.sanctum.labyrinth.data.container.LabyrinthCollection;
import com.github.sanctum.labyrinth.data.container.LabyrinthList;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.api.EssentialsAddonQuery;
import com.github.sanctum.myessentials.model.kit.Kit;
import com.github.sanctum.myessentials.model.warp.Warp;
import com.github.sanctum.myessentials.model.warp.WarpHolder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class LoadingLogic {

	private static LoadingLogic instance;
	private final Essentials plugin;

	LoadingLogic(@NotNull Essentials plugin) {
		this.plugin = plugin;
	}

	public void onEnable() {
		if (System.getProperty("OLD") != null && System.getProperty("OLD").equals("TRUE")) {
			plugin.getLogger().severe("- RELOAD DETECTED!...");
			plugin.getLogger().severe("      ██╗");
			plugin.getLogger().severe("  ██╗██╔╝");
			plugin.getLogger().severe("  ╚═╝██║ ");
			plugin.getLogger().severe("  ██╗██║ ");
			plugin.getLogger().severe("  ╚═╝╚██╗");
			plugin.getLogger().severe("      ╚═╝");
			plugin.getLogger().severe("- (You are not supported in the case of corrupt data)");
			plugin.getLogger().severe("- (Reloading is NEVER safe and you should always restart instead.)");
		} else {
			System.setProperty("OLD", "FALSE");
			injectAddons();
			loadWarps();
			loadKits();
		}
	}

	public void onDisable() {
		if (System.getProperty("OLD").equals("FALSE")) {
			System.setProperty("OLD", "TRUE");
		}
		EssentialsAddonQuery.getKnownAddons().forEach(a -> {
			a.remove();
			a.getLogger().info("- Disabling addon " + '"' + a.getName() + '"');
		});

		LabyrinthCollection<Warp> warps = new LabyrinthList<>();

		plugin.getWarpHolders().forEach(h -> warps.addAll(h.getAll()));

		warps.addAll(plugin.getWarps());

		FileList list = FileList.search(plugin);
		FileManager w = list.get("warps", "Data", FileType.JSON);
		w.write(t -> t.set("warps", warps.stream().toArray(Warp[]::new)));
		FileManager kits = list.get("kits", "Data", FileType.JSON);
		kits.write(t -> t.set("kits", plugin.getKits().stream().toArray(Kit[]::new)));

	}

	private void injectAddons() {
		Arrays.stream(EssentialsAddonQuery.class.getDeclaredMethods()).filter((m) -> Modifier.isStatic(m.getModifiers()) && Modifier.isProtected(m.getModifiers()) && m.getName().equals("runInjectionProcedures")).findFirst().ifPresent(m -> {
			try {
				try {
					m.setAccessible(true);
				} catch (Exception ignored) {
				}
				m.invoke(null, plugin);
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		});
	}

	private void loadWarps() {
		JsonAdapter.register(Warp.class);
		FileList list = FileList.search(plugin);
		FileManager warps = list.get("warps", "Data", FileType.JSON);
		if (warps.getRoot().exists()) {
			Warp[] w = warps.read(c -> c.getNode("warps").get(Warp[].class));
			if (w != null) {
				for (Warp warp : w) {
					if (warp.getOwner() != null) {
						OfflinePlayer pl = (OfflinePlayer) warp.getOwner();
						WarpHolder holder = plugin.getWarpHolder(pl);
						holder.add(warp);
					} else {
						plugin.loadWarp(warp);
					}
				}
			}
		}
	}

	private void loadKits() {
		JsonAdapter.register(Kit.class);
		FileList list = FileList.search(plugin);
		FileManager kits = list.get("kits", "Data", FileType.JSON);
		if (kits.getRoot().exists()) {
			Kit[] k = kits.read(c -> c.getNode("kits").get(Kit[].class));
			if (k != null) {
				for (Kit kit : k) {
					plugin.loadKit(kit);
				}
			}
		}
	}

	@Note("You don't need to use this!")
	public static LoadingLogic get(Plugin plugin) {
		if (!(plugin instanceof Essentials)) throw new IllegalArgumentException("Invalid plugin instance!");
		return instance == null ? (instance = new LoadingLogic((Essentials) plugin)) : instance;
	}
}
