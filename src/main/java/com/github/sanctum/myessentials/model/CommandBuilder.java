package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.api.CommandData;
import com.github.sanctum.myessentials.util.ProvidedMessage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class CommandBuilder extends Command {
	protected final Plugin plugin = JavaPlugin.getProvidingPlugin(getClass());
	protected final CommandData commandData;
	private static final LinkedList<InternalCommandData> internalMap = new LinkedList<>();
	private static final Map<CommandData, Command> commandMapping = new HashMap<>();

	public CommandBuilder(CommandData commandData) {
		super(commandData.getLabel());
		if (commandData instanceof InternalCommandData) {
			internalMap.add((InternalCommandData) commandData);
		} else {
			Essentials.getInstance().registeredCommands.add(commandData);
		}
		setDescription(commandData.getDescription());
		setPermission(commandData.getPermissionNode());
		setPermissionMessage(ChatColor.RED + "You do not have permission to perform this command!");
		this.commandData = commandData;
		SimpleCommandMap commandMap = getCommandMap();
		assert commandMap != null;
		commandMap.register(getLabel(), plugin.getName(), this);
		commandMapping.put(commandData, this);
	}

	protected void sendMessage(CommandSender sender, ProvidedMessage message) {
		if (!(sender instanceof Player)) {
			new Message("[Essentials]").info(message.toString());
		} else {
			new Message((Player) sender, "[&2Essentials&r]").send(message.toString());
		}
	}

	protected void sendMessage(CommandSender sender, String text) {
		if (!(sender instanceof Player)) {
			new Message("[Essentials]").info(text);
		} else {
			new Message((Player) sender, "[&2Essentials&r]").send(text);
		}
	}

	protected String color(String text) {
		return StringUtils.translate(text);
	}

	public abstract boolean playerView(@NotNull Player player, @NotNull String s, @NotNull String[] strings);

	public abstract boolean consoleView(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings);

	@Override
	public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
		if (!(sender instanceof Player)) {
			return consoleView(sender, s, strings);
		}
		return playerView((Player) sender, s, strings);
	}

	public static SimpleCommandMap getCommandMap() {
		try {
			final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			commandMapField.setAccessible(true);
			return (SimpleCommandMap) commandMapField.get(Bukkit.getServer());
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Command getRegistration(CommandData data) {
		return commandMapping.getOrDefault(data, null);
	}

	@SuppressWarnings("unchecked")
	public static void unregister(Command command) {
		try {
			SimpleCommandMap commandMap = getCommandMap();
			assert commandMap != null;
			Field knownMap = commandMap.getClass().getSuperclass().getDeclaredField("knownCommands");
			knownMap.setAccessible(true);
			HashMap<String, Command> knownCommands = (HashMap<String, Command>) knownMap.get(commandMap);
			knownCommands.remove(command.getName());
			for (String alias : command.getAliases()) {
				if (knownCommands.containsKey(alias) && knownCommands.get(alias).getAliases().contains(alias)) {
					knownCommands.remove(alias);
				}
			}
			command.unregister(commandMap);
			// doesnt remove tab completions
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static LinkedList<String> getCommandList(Player p) {
		LinkedList<String> append = internalMap.stream()
				.filter(data -> p.hasPermission(Objects.requireNonNull(data.getPermissionNode())))
				.map(data -> data.getUsage() + " &r- " + data.getDescription()).collect(Collectors.toCollection(LinkedList::new));
		try {
			SimpleCommandMap commandMap = getCommandMap();
			assert commandMap != null;
			Field knownMap = commandMap.getClass().getSuperclass().getDeclaredField("knownCommands");
			knownMap.setAccessible(true);
			HashMap<String, Command> knownCommands = (HashMap<String, Command>) knownMap.get(commandMap);
			final List<String> placement = new ArrayList<>();
			for (Map.Entry<String, Command> entry : knownCommands.entrySet()) {
                String text = "/&6" + entry.getValue().getName() + " &r- " + entry.getValue().getDescription();
				if (!append.contains(entry.getValue().getDescription().isEmpty() ? "/&6" + entry.getValue().getName() + "&r" : text)) {
					if (entry.getValue().getPermission() != null && p.hasPermission(entry.getValue().getPermission())) {
						append.add(entry.getValue().getDescription().isEmpty() ? "/&6" + entry.getValue().getName() + "&r" : text);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return append;
	}

	public static LinkedList<String> getCommandList() {
		return internalMap.stream()
				.map(data -> data.getUsage() + " &r- " + data.getDescription())
				.collect(Collectors.toCollection(LinkedList::new));
	}

}
