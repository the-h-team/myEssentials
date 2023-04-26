package com.github.sanctum.myessentials.util.factory;

import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.data.RegistryData;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.api.EssentialsAddonQuery;
import com.github.sanctum.myessentials.model.CommandInput;
import com.github.sanctum.myessentials.model.IExecutorHandler;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.model.kit.Kit;
import com.github.sanctum.myessentials.model.warp.Warp;
import com.github.sanctum.myessentials.model.warp.WarpHolder;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import com.github.sanctum.myessentials.util.teleportation.TeleportRunnerImpl;
import com.github.sanctum.myessentials.util.teleportation.TeleportationManager;
import com.github.sanctum.panther.annotation.Note;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherList;
import com.github.sanctum.panther.event.VentMap;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.file.JsonAdapter;
import com.github.sanctum.skulls.CustomHead;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Listener;
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
			plugin.setExecutor(new IExecutorHandler());
			injectAddons();
			loadWarps();
			loadKits();
			plugin.setTeleportRunner(new TeleportRunnerImpl(plugin));
			plugin.setMessenger(new MessengerImpl(plugin));
			new Registry<>(Listener.class).source(this).filter("com.github.sanctum.myessentials.listeners").operate(l -> VentMap.getInstance().subscribe(plugin, l));
			InternalCommandData.defaultOrReload(plugin);
			ConfiguredMessage.loadProperties(plugin);
			OptionLoader.renewRemainingBans();
			OptionLoader.checkConfig();
			RegistryData<CommandInput> data = new Registry<>(CommandInput.class)
					.source(this).filter("com.github.sanctum.myessentials.commands")
					.operate(builder -> {
					});

			plugin.getLogger().info("- (" + data.getData().size() + ") Unique commands registered.");
			TeleportationManager.registerListeners(plugin);

			FileManager man = plugin.getFileList().get("heads", "Data");

			if (!man.getRoot().exists()) {
				FileList.copy(plugin.getResource("heads.yml"), man.getRoot().getParent());
				man.getRoot().reload();
			}

			CustomHead.Manager.newLoader(man.getRoot()).look("My_heads").complete();
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

		PantherCollection<Warp> warps = new PantherList<>();

		plugin.getWarpHolders().forEach(h -> warps.addAll(h.getAll()));

		warps.addAll(plugin.getWarps());

		FileList list = FileList.search(plugin);
		FileManager w = list.get("warps", "Data", Configurable.Type.JSON);
		w.write(t -> t.set("warps", warps.stream().toArray(Warp[]::new)));
		FileManager kits = list.get("kits", "Data", Configurable.Type.JSON);
		kits.write(t -> t.set("kits", plugin.getKits().stream().toArray(Kit[]::new)));
		TeleportationManager.unregisterListeners();
		OptionLoader.recordRemainingBans();
	}

	private void injectAddons() {
		Arrays.stream(EssentialsAddonQuery.class.getDeclaredMethods()).filter((m) -> Modifier.isStatic(m.getModifiers()) && Modifier.isPrivate(m.getModifiers()) && m.getName().equals("runInjectionProcedures")).findFirst().ifPresent(m -> {
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
		FileManager warps = list.get("warps", "Data", Configurable.Type.JSON);
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
		FileManager kits = list.get("kits", "Data", Configurable.Type.JSON);
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
