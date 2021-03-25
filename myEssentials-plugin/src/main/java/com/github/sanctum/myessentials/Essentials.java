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
import com.github.sanctum.labyrinth.event.EventBuilder;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.api.CommandData;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.CommandImpl;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.CommandRegistration;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.teleportation.TeleportationManager;

import java.lang.reflect.Field;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class Essentials extends JavaPlugin implements MyEssentialsAPI {

    private static final Map<CommandData, Command> REGISTRATIONS = new HashMap<>();
    public static final CommandMap SERVER_COMMAND_MAP;
    public static final Map<String, Command> KNOWN_COMMANDS_MAP;
    private static Essentials instance;

    public final Set<CommandData> registeredCommands = new HashSet<>();

    public final Map<UUID, Location> previousLocation = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getServicesManager().register(MyEssentialsAPI.class, this, this, ServicePriority.Normal);
        EventBuilder events = new EventBuilder(this);
        InternalCommandData.defaultOrReload(this);
        ConfiguredMessage.loadProperties(this);
        CommandRegistration.compileFields(this, "com.github.sanctum.myessentials.commands");
        TeleportationManager.registerListeners(this);
        events.compileFields("com.github.sanctum.myessentials.listeners");
    }

    @Override
    public void onDisable() {
        TeleportationManager.unregisterListeners();
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
        command.unregister(SERVER_COMMAND_MAP);
    }

    @Override
    public Command getRegistration(CommandData commandData) {
        return REGISTRATIONS.get(commandData);
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
    public Location getPreviousLocation(UUID id) {
        return previousLocation.get(id);
    }

    @Override
    public FileManager getAddonFile(String name, String directory) {
        return getFileList().find(name, "/Addons/" + directory + "/");
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

    public static Essentials getInstance() {
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