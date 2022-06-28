/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials, a derivative work inspired by the
 *  Essentials <http://ess3.net/> and EssentialsX <https://essentialsx.net/>
 *  projects, both licensed under the GPLv3.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.data.RegistryData;
import com.github.sanctum.labyrinth.data.container.LabyrinthCollection;
import com.github.sanctum.labyrinth.data.container.LabyrinthEntryMap;
import com.github.sanctum.labyrinth.data.container.LabyrinthMap;
import com.github.sanctum.labyrinth.data.container.LabyrinthSet;
import com.github.sanctum.labyrinth.library.CommandUtils;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.listeners.PlayerEventListener;
import com.github.sanctum.myessentials.model.CommandData;
import com.github.sanctum.myessentials.model.CommandImpl;
import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.IExecutorHandler;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.model.Messenger;
import com.github.sanctum.myessentials.model.executor.IExecutorCommandBase;
import com.github.sanctum.myessentials.model.kit.Kit;
import com.github.sanctum.myessentials.model.warp.Warp;
import com.github.sanctum.myessentials.model.warp.WarpHolder;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import com.github.sanctum.myessentials.util.SignEdit;
import com.github.sanctum.myessentials.util.factory.LoadingLogic;
import com.github.sanctum.myessentials.util.factory.MessengerImpl;
import com.github.sanctum.myessentials.util.teleportation.TeleportRunner;
import com.github.sanctum.myessentials.util.teleportation.TeleportRunnerImpl;
import com.github.sanctum.myessentials.util.teleportation.TeleportationManager;
import com.github.sanctum.skulls.CustomHead;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Essentials extends JavaPlugin implements MyEssentialsAPI {

	private static final Map<CommandData, Command> commandMap = new HashMap<>();
	final LabyrinthMap<UUID, Kit.Holder> kitHolderMap = new LabyrinthEntryMap<>();
	final LabyrinthMap<UUID, WarpHolder> warpHolderMap = new LabyrinthEntryMap<>();
	final LabyrinthMap<String, Kit> kitMap = new LabyrinthEntryMap<>();
	final LabyrinthMap<String, Warp> WARPS = new LabyrinthEntryMap<>();

	public final Set<CommandData> registeredCommands = new HashSet<>();

	private TeleportRunner teleportRunner;
	private MessengerImpl messenger;
	private IExecutorHandler executor;

	@Override
	public void onEnable() {
		Bukkit.getServicesManager().register(MyEssentialsAPI.class, this, this, ServicePriority.Normal);
		LoadingLogic.get(this).onEnable();
		this.executor = new IExecutorHandler();
		this.teleportRunner = new TeleportRunnerImpl(this);
		this.messenger = new MessengerImpl(this);
		new Registry<>(Listener.class).source(this).filter("com.github.sanctum.myessentials.listeners").operate(l -> LabyrinthProvider.getService(Service.VENT).subscribe(this, l));
		InternalCommandData.defaultOrReload(this);
		ConfiguredMessage.loadProperties(this);
		OptionLoader.renewRemainingBans();
		OptionLoader.checkConfig();
		RegistryData<CommandOutput> data = new Registry<>(CommandOutput.class)
				.source(this).filter("com.github.sanctum.myessentials.commands")
				.operate(builder -> {
				});

		getLogger().info("- (" + data.getData().size() + ") Unique commands registered.");
		TeleportationManager.registerListeners(this);

		FileManager man = getFileList().get("Heads", "Data");

		if (!man.getRoot().exists()) {
			FileList.copy(getResource("Heads.yml"), man.getRoot().getParent());
			man.getRoot().reload();
		}

		CustomHead.Manager.newLoader(man.getRoot()).look("My_heads").complete();

	}


	@Override
	public void onDisable() {
		try {
			LoadingLogic.get(this).onDisable();
			TeleportationManager.unregisterListeners();
			OptionLoader.recordRemainingBans();
		} catch (Exception e) {
			getLogger().severe("- Reload detected.");
		}
	}

	@Override
	public Command registerCommand(CommandOutput commandBuilder) {
		final Command command = new CommandImpl(commandBuilder);
		CommandUtils.read(e -> {
			CommandMap map = e.getKey();
			map.register(commandBuilder.commandData.getLabel(),
					getName(), command);
			return null;
		});
		commandMap.put(commandBuilder.commandData, command);
		return command;
	}

	@Override
	public void unregisterCommand(Command command) {
		CommandUtils.read(e -> {
			Map<String, Command> map = e.getValue();
			map.remove(command.getName());
			for (String alias : command.getAliases()) {
				if (map.containsKey(alias) && map.get(alias).getAliases().contains(alias)) {
					map.remove(alias);
				}

			}
			for (Map.Entry<CommandData, List<IExecutorCommandBase<? extends CommandSender>>> entry : executor.getExecutorCalculations().entrySet()) {
				if (entry.getKey().getLabel().equals(command.getLabel())) {
					TaskScheduler.of(() -> executor.removeCompletions(entry.getKey()).removeConsoleCalculation(entry.getKey()).removePlayerCalculation(entry.getKey())
					).schedule();
					break;
				}
			}
			command.unregister(e.getKey());
			return null;
		});
	}

	@Override
	public Command getRegistration(CommandData commandData) {
		return commandMap.get(commandData);
	}

	@Override
	public IExecutorHandler getExecutorHandler() {
		return executor;
	}

	@Override
	public Set<CommandData> getRegisteredCommands() {
		return registeredCommands;
	}

	@Override
	public FileList getFileList() {
		return FileList.search(this);
	}

	@Override
	public @Nullable Location getPreviousLocation(Player player) {
		return PlayerEventListener.getPrevLocations().get(player.getUniqueId());
	}

	@Override
	public @Nullable Location getPreviousLocationOffline(UUID uuid) {
		return PlayerEventListener.getPrevLocations().get(uuid);
	}

	@Override
	public FileManager getAddonFile(String name, String directory) {
		return getFileList().get(name, "/Addons/" + directory + "/");
	}

	@Override
	public TeleportRunner getTeleportRunner() {
		return teleportRunner;
	}

	@Override
	public SignEdit wrapSign(Block b) {
		return new SignEdit(b);
	}

	@Override
	public Messenger getMessenger() {
		return messenger;
	}

	@Override
	public Kit.Holder getKitHolder(@NotNull OfflinePlayer player) {
		return kitHolderMap.computeIfAbsent(player.getUniqueId(), new Kit.Holder() {

			final String name;
			final UUID id;
			Kit current;

			{
				this.id = player.getUniqueId();
				this.name = player.getName();
			}

			@Override
			public @NotNull String getName() {
				return name;
			}

			@Override
			public @NotNull UUID getId() {
				return id;
			}

			@Override
			public @Nullable Kit getCurrent() {
				return current;
			}

			@Override
			public boolean apply(@NotNull Kit kit) {
				Cooldown test = LabyrinthProvider.getService(Service.COOLDOWNS).getCooldown(kit.getName() + "-" + getName());
				if (test != null) {
					if (!test.isComplete()) {
						return false;
					}
					LabyrinthProvider.getInstance().remove(test);
				}
				current = kit;
				Player pl = Bukkit.getPlayer(id);
				if (pl != null) {
					if (kit.getCooldown() != null) {
						Cooldown n = new KitCooldownImpl(kit.getName() + "-" + getName(), kit.getCooldown().toSeconds());
						n.save();
					}
					pl.getInventory().setContents(kit.getInventory());
					if (kit.getHelmet() != null) {
						pl.getInventory().setHelmet(kit.getHelmet());
					}
					if (kit.getChestplate() != null) {
						pl.getInventory().setChestplate(kit.getChestplate());
					}
					if (kit.getLeggings() != null) {
						pl.getInventory().setLeggings(kit.getLeggings());
					}
					if (kit.getBoots() != null) {
						pl.getInventory().setBoots(kit.getBoots());
					}
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public WarpHolder getWarpHolder(@NotNull OfflinePlayer player) {
		return warpHolderMap.computeIfAbsent(player.getUniqueId(), () -> new WarpHolder() {

			final String name = player.getName();
			final UUID id = player.getUniqueId();
			final LabyrinthCollection<Warp> warps = new LabyrinthSet<>();

			@Override
			public @NotNull String getName() {
				return name;
			}

			@Override
			public UUID getId() {
				return id;
			}

			@Override
			public @Nullable Warp get(@NotNull String name) {
				return warps.stream().filter(w -> w.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
			}

			@Override
			public @NotNull LabyrinthCollection<Warp> getAll() {
				return warps;
			}

			@Override
			public void add(@NotNull Warp warp) {
				warps.add(warp);
			}

			@Override
			public void remove(@NotNull Warp warp) {
				warps.remove(warp);
			}
		});
	}

	@Override
	public LabyrinthCollection<Kit.Holder> getKitHolders() {
		return kitHolderMap.values();
	}

	@Override
	public LabyrinthCollection<WarpHolder> getWarpHolders() {
		return warpHolderMap.values();
	}

	@Override
	public LabyrinthCollection<Kit> getKits() {
		return kitMap.values();
	}

	@Override
	public LabyrinthCollection<Warp> getWarps() {
		return WARPS.values();
	}

	static class KitCooldownImpl extends Cooldown {

		final String id;
		final long time;

		KitCooldownImpl(String id, Number seconds) {
			this.id = id;
			this.time = abv(seconds.longValue());
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public long getCooldown() {
			return time;
		}
	}

	@Override
	public Kit getKit(@NotNull String name) {
		return kitMap.get(name);
	}

	@Override
	public Warp getWarp(@NotNull String name) {
		return this.WARPS.get(name);
	}

	@Override
	public void loadKit(@NotNull Kit kit) {
		kitMap.computeIfAbsent(kit.getName(), () -> kit);
	}

	@Override
	public void unloadKit(@NotNull Kit kit) {
		this.kitMap.remove(kit.getName());
	}

	@Override
	public void loadWarp(@NotNull Warp warp) {
		this.WARPS.put(warp.getName(), warp);
	}

	@Override
	public void unloadWarp(@NotNull Warp warp) {
		this.WARPS.remove(warp.getName());
	}

	@Override
	public String getPrefix() {
		return ConfiguredMessage.PREFIX.get();
	}

	@Override
	public void logInfo(String toLog) {
		getLogger().info(toLog);
	}

	@Override
	public void logInfo(Supplier<String> toLog) {
		getLogger().info(toLog);
	}

	@Override
	public void logSevere(String toLog) {
		getLogger().severe(toLog);
	}

	@Override
	public void logSevere(Supplier<String> toLog) {
		getLogger().severe(toLog);
	}

	public static LinkedList<String> getCommandList() {
		return commandMap.keySet().stream()
				.map(data -> "/" + data.getLabel() + " &r- " + data.getDescription())
				.collect(Collectors.toCollection(LinkedList::new));
	}

	public static JavaPlugin getInstance() {
		return JavaPlugin.getPlugin(Essentials.class);
	}

}
