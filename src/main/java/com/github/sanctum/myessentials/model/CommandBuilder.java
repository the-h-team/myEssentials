package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.api.CommandData;

import java.util.LinkedList;

import com.github.sanctum.myessentials.util.ProvidedMessage;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

public abstract class CommandBuilder extends Command {
    protected final Plugin plugin = JavaPlugin.getProvidingPlugin(getClass());
    protected final CommandData commandData;
    private static final LinkedList<InternalCommandData> internalMap = new LinkedList<>();

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
        try {
            final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            final CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            commandMap.register(getLabel(), plugin.getName(), this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
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

    public static LinkedList<String> getCommandList(Player p) {
        return internalMap.stream()
                .filter(data -> p.hasPermission(Objects.requireNonNull(data.getPermissionNode())))
                .map(data -> data.getUsage() + " &r- " + data.getDescription())
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public static LinkedList<String> getCommandList() {
        return internalMap.stream()
                .map(data -> data.getUsage() + " &r- " + data.getDescription())
                .collect(Collectors.toCollection(LinkedList::new));
    }

}
