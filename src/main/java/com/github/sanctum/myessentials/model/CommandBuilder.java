package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;

public abstract class CommandBuilder extends BukkitCommand {
    protected static final Plugin PLUGIN = JavaPlugin.getProvidingPlugin(CommandBuilder.class);
    protected final CommandData commandData;

    public CommandBuilder(CommandData commandData) {
        super(commandData.getLabel());
        setDescription(commandData.getDescription());
        setPermission(commandData.getPermissionNode());
        setPermissionMessage(ChatColor.RED + "You do not have permission to perform this command!");
        this.commandData = commandData;
        try {
            final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            final CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            commandMap.register(getLabel(), this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected void sendMessage(CommandSender sender, ConfiguredMessage message) {
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

    public abstract boolean playerView(Player p, @NotNull String s, @NotNull String[] strings);

    public abstract boolean consoleView(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings);

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
        if (!(sender instanceof Player)) {
            return consoleView(sender, s, strings);
        }
        return playerView((Player) sender, s, strings);
    }
}
