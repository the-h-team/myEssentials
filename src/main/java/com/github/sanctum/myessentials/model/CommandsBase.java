package com.github.sanctum.myessentials.model;

import com.github.sanctum.myessentials.util.Messaging;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public abstract class CommandsBase extends BukkitCommand {
    protected static final Plugin PLUGIN = JavaPlugin.getProvidingPlugin(CommandsBase.class);
    protected final CommandData commandData;

    public CommandsBase(CommandData commandData) {
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

    protected void sendMessage(CommandSender sender, Messaging message) {
        sender.sendMessage(message.toString());
    }

}
