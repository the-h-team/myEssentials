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

import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.data.RegistryData;
import com.github.sanctum.labyrinth.event.EventBuilder;
import com.github.sanctum.labyrinth.gui.InventoryRows;
import com.github.sanctum.labyrinth.gui.shared.SharedBuilder;
import com.github.sanctum.labyrinth.gui.shared.SharedMenu;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.listeners.PlayerEventListener;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.CommandData;
import com.github.sanctum.myessentials.model.CommandImpl;
import com.github.sanctum.myessentials.model.InjectedExecutorHandler;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.model.Messenger;
import com.github.sanctum.myessentials.model.action.IExecutorCalculating;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import com.github.sanctum.myessentials.util.SignWrapper;
import com.github.sanctum.myessentials.util.factory.MessengerImpl;
import com.github.sanctum.myessentials.util.factory.ReloadUtil;
import com.github.sanctum.myessentials.util.teleportation.TeleportRunner;
import com.github.sanctum.myessentials.util.teleportation.TeleportRunnerImpl;
import com.github.sanctum.myessentials.util.teleportation.TeleportationManager;
import java.io.IOException;
import java.lang.reflect.Field;
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
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public final class Essentials extends JavaPlugin implements MyEssentialsAPI {

	private static final Map<CommandData, Command> REGISTRATIONS = new HashMap<>();
	public static final CommandMap SERVER_COMMAND_MAP;
	public static final Map<String, Command> KNOWN_COMMANDS_MAP;
	private static Essentials instance;

	public final Set<CommandData> registeredCommands = new HashSet<>();

	private TeleportRunner teleportRunner;
	private MessengerImpl messenger;
	private InjectedExecutorHandler executor;

	@Override
	public void onLoad() {
		executor = new InjectedExecutorHandler(this);
	}

	@Override
	public void onEnable() {
		instance = this;
		Bukkit.getServicesManager().register(MyEssentialsAPI.class, this, this, ServicePriority.Normal);
		ReloadUtil.get(this).onEnable(getClassLoader());
		SharedMenu bin = SharedBuilder.create(this, "My-Bin", StringUtils.use("&6&nDonation Bin.").translate(), InventoryRows.THREE.getSlotCount());
		bin.addOption(SharedMenu.Option.CANCEL_HOTBAR);
		bin.setItem(0, () -> {
			ItemStack item = new ItemStack(Material.HEART_OF_THE_SEA);
			ItemMeta meta = item.getItemMeta();
			assert meta != null;
			meta.setDisplayName(StringUtils.use("&4Close.").translate());
			item.setItemMeta(meta);
			return item;
		}, click -> {
			click.getWhoClicked().closeInventory();
			click.cancel();
		});
		this.teleportRunner = new TeleportRunnerImpl(this);
		this.messenger = new MessengerImpl(this);
		try {
			new Registry<>(Listener.class)
					.source(this)
					.pick("com.github.sanctum.myessentials.listeners")
					.operate(EventBuilder::register);
		} catch (IOException e) {
			e.printStackTrace();
		}
		InternalCommandData.defaultOrReload(this);
		ConfiguredMessage.loadProperties(this);
		OptionLoader.renewRemainingBans();
		OptionLoader.checkConfig();
		try {
			RegistryData<CommandBuilder> data = new Registry<>(CommandBuilder.class)
					.source(this)
					.pick("com.github.sanctum.myessentials.commands")
					.operate(builder -> {
					});

			getLogger().info("- (" + data.getData().size() + ") Unique commands registered.");

		} catch (IOException e) {
			e.printStackTrace();
		}
		TeleportationManager.registerListeners(this);
		Commands.register();
	}


	@Override
	public void onDisable() {
		try {
			ReloadUtil.get(this).onDisable();
			TeleportationManager.unregisterListeners();
			OptionLoader.recordRemainingBans();
		} catch (Exception e) {
			getLogger().severe("- Reload detected.");
		}
	}

	@Override
	public Command registerCommand(CommandBuilder commandBuilder) {
		final Command command = new CommandImpl(commandBuilder);
		SERVER_COMMAND_MAP
				.register(commandBuilder.commandData.getLabel(),
						JavaPlugin.getProvidingPlugin(commandBuilder.commandData.getClass()).getName(), command);
		REGISTRATIONS.put(commandBuilder.commandData, command);
		return command;
	}

	@Override
	public void unregisterCommand(Command command) {
		KNOWN_COMMANDS_MAP.remove(command.getName());
		for (String alias : command.getAliases()) {
			if (KNOWN_COMMANDS_MAP.containsKey(alias) && KNOWN_COMMANDS_MAP.get(alias).getAliases().contains(alias)) {
				KNOWN_COMMANDS_MAP.remove(alias);
			}

		}
		for (Map.Entry<CommandData, List<IExecutorCalculating<? extends CommandSender>>> entry : executor.getExecutorCalculations().entrySet()) {
			if (entry.getKey().getLabel().equals(command.getLabel())) {
				Schedule.sync(() -> executor
						.removeCompletions(entry.getKey())
						.removeConsoleCalculation(entry.getKey())
						.removePlayerCalculation(entry.getKey())
				).run();
				break;
			}
		}
		command.unregister(SERVER_COMMAND_MAP);
	}

	@Override
	public Command getRegistration(CommandData commandData) {
		return REGISTRATIONS.get(commandData);
	}

	@Override
	public InjectedExecutorHandler getExecutorHandler() {
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
		return PlayerEventListener.getInstance().getPrevLocations().get(player.getUniqueId());
	}

	@Override
	public @Nullable Location getPreviousLocationOffline(UUID uuid) {
		return PlayerEventListener.getInstance().getPrevLocations().get(uuid);
	}

	@Override
	public FileManager getAddonFile(String name, String directory) {
		return getFileList().find(name, "/Addons/" + directory + "/");
	}

	@Override
	public TeleportRunner getTeleportRunner() {
		return teleportRunner;
	}

	@Override
	public SignWrapper wrapSign(Block b) {
		return new SignWrapper(b);
	}

	@Override
	public Messenger getMessenger() {
		return messenger;
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
		return REGISTRATIONS.keySet().stream()
				.map(data -> data.getUsage() + " &r- " + data.getDescription())
				.collect(Collectors.toCollection(LinkedList::new));
	}

	public static JavaPlugin getInstance() {
		return instance;
	}

	// Prepare the command management utilities.
	static {
		try {
			final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			commandMapField.setAccessible(true);
			SERVER_COMMAND_MAP = (SimpleCommandMap) commandMapField.get(Bukkit.getServer());
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException("Unable to retrieve commandMap field on Server class!");
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Unable to access commandMap field on Server class!");
		}
		try {
			final Field knownCommandsField;
			knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
			knownCommandsField.setAccessible(true);
			//noinspection unchecked
			KNOWN_COMMANDS_MAP = (Map<String, Command>) knownCommandsField.get(SERVER_COMMAND_MAP);
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException("Unable to retrieve knownCommands field from SimpleCommandMap class!");
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Unable to access knownCommands field on server's CommandMap!");
		}
	}
}
