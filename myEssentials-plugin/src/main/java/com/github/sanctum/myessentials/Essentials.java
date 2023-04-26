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
import com.github.sanctum.labyrinth.library.CommandUtils;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.listeners.PlayerEventListener;
import com.github.sanctum.myessentials.model.CommandData;
import com.github.sanctum.myessentials.model.CommandImpl;
import com.github.sanctum.myessentials.model.CommandInput;
import com.github.sanctum.myessentials.model.IExecutorHandler;
import com.github.sanctum.myessentials.model.Messenger;
import com.github.sanctum.myessentials.model.executor.IExecutorCommandBase;
import com.github.sanctum.myessentials.model.kit.Kit;
import com.github.sanctum.myessentials.model.warp.Warp;
import com.github.sanctum.myessentials.model.warp.WarpHolder;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.SignEdit;
import com.github.sanctum.myessentials.util.factory.LoadingLogic;
import com.github.sanctum.myessentials.util.factory.MessengerImpl;
import com.github.sanctum.myessentials.util.teleportation.TeleportRunner;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import com.github.sanctum.panther.container.PantherSet;
import com.github.sanctum.panther.event.Vent;
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
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Essentials extends JavaPlugin implements MyEssentialsAPI, Vent.Host {

	private static final Map<CommandData, Command> commandMap = new HashMap<>();
	final PantherMap<UUID, Kit.Holder> kitHolderMap = new PantherEntryMap<>();
	final PantherMap<UUID, WarpHolder> warpHolderMap = new PantherEntryMap<>();
	final PantherMap<String, Kit> kitMap = new PantherEntryMap<>();
	final PantherMap<String, Warp> WARPS = new PantherEntryMap<>();

	public final Set<CommandData> registeredCommands = new HashSet<>();

	TeleportRunner teleportRunner;
	MessengerImpl messenger;
	IExecutorHandler executor;

	@Override
	public void onEnable() {
		Bukkit.getServicesManager().register(MyEssentialsAPI.class, this, this, ServicePriority.Normal);
		LoadingLogic.get(this).onEnable();
	}


	@Override
	public void onDisable() {
		try {
			LoadingLogic.get(this).onDisable();
		} catch (Exception e) {
			getLogger().severe("- Reload detected.");
		}
	}

	public void setExecutor(IExecutorHandler executor) {
		this.executor = executor;
	}

	public void setMessenger(MessengerImpl messenger) {
		this.messenger = messenger;
	}

	public void setTeleportRunner(TeleportRunner teleportRunner) {
		this.teleportRunner = teleportRunner;
	}

	@Override
	public Command registerCommand(CommandInput commandBuilder) {
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
					for (int i = 0; i < kit.getInventory().length; i++) {
						ItemStack item = kit.getInventory()[i];
						if (item.getType() != Material.AIR) {
							ItemStack inside = pl.getInventory().getItem(i);
							if (inside != null) {
								if (inside.getType() == Material.AIR) {
									pl.getInventory().setItem(i, item);
								} else {
									LabyrinthProvider.getInstance().getItemComposter().add(item, pl);
								}
							} else {
								pl.getInventory().setItem(i, item);
							}
						}
					}
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
			final PantherCollection<Warp> warps = new PantherSet<>();

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
			public @NotNull PantherCollection<Warp> getAll() {
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
	public PantherCollection<Kit.Holder> getKitHolders() {
		return kitHolderMap.values();
	}

	@Override
	public PantherCollection<WarpHolder> getWarpHolders() {
		return warpHolderMap.values();
	}

	@Override
	public PantherCollection<Kit> getKits() {
		return kitMap.values();
	}

	@Override
	public PantherCollection<Warp> getWarps() {
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
