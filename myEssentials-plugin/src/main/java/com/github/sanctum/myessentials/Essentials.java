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
import com.github.sanctum.myessentials.api.AddonQuery;
import com.github.sanctum.myessentials.api.CommandData;
import com.github.sanctum.myessentials.api.EssentialsAddon;
import com.github.sanctum.myessentials.api.Messenger;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.listeners.PlayerEventListener;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.CommandImpl;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.CommandRegistration;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.MessengerImpl;
import com.github.sanctum.myessentials.util.teleportation.TeleportRunner;
import com.github.sanctum.myessentials.util.teleportation.TeleportRunnerImpl;
import com.github.sanctum.myessentials.util.teleportation.TeleportationManager;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
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

    @Override
    public void onEnable() {
        instance = this;
        if (System.getProperty("OLD") != null && System.getProperty("OLD").equals("TRUE")) {
            getLogger().severe("- RELOAD DETECTED! Shutting down...");
            getLogger().severe("      ██╗");
            getLogger().severe("  ██╗██╔╝");
            getLogger().severe("  ╚═╝██║ ");
            getLogger().severe("  ██╗██║ ");
            getLogger().severe("  ╚═╝╚██╗");
            getLogger().severe("      ╚═╝");
            getLogger().severe("- (You are not supported in the case of corrupt data)");
            getLogger().severe("- (Reloading is NEVER safe and you should always restart instead.)");
            FileManager file = getFileList().find("ignore", "");
            String location = new Date().toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE);
            List<String> toAdd = new ArrayList<>(file.getConfig().getStringList(location));
            toAdd.add("RELOAD DETECTED! Shutting down...");
            toAdd.add("      ██╗");
            toAdd.add("  ██╗██╔╝");
            toAdd.add("  ╚═╝██║ ");
            toAdd.add("  ██╗██║ ");
            toAdd.add("  ╚═╝╚██╗");
            toAdd.add("      ╚═╝");
            toAdd.add("(You are not supported in the case of corrupt data)");
            toAdd.add("(Reloading is NEVER safe and you should always restart instead.)");
            file.getConfig().set(location, toAdd);
            file.saveConfig();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            System.setProperty("OLD", "FALSE");
        }
        Bukkit.getServicesManager().register(MyEssentialsAPI.class, this, this, ServicePriority.Normal);
        this.teleportRunner = new TeleportRunnerImpl(this);
        this.messenger = new MessengerImpl(this);
        EventBuilder.compileFields(this, "com.github.sanctum.myessentials.listeners");
        InternalCommandData.defaultOrReload(this);
        ConfiguredMessage.loadProperties(this);
        CommandRegistration.compileFields(this, "com.github.sanctum.myessentials.commands");
        TeleportationManager.registerListeners(this);
        try {
            injectAddons();
        } catch (Exception e) {
            getLogger().severe("- An unexpected file type was found in the addon folder, remove it then restart.");
        }
    }

    private void injectAddons() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Set<Class<?>> classes = Sets.newHashSet();
        FileManager check = getAddonFile("Test", "");
        File parent = check.getFile().getParentFile();
        for (File f : parent.listFiles()) {
            if (f.isFile()) {
                JarFile test = new JarFile(f);
                URLClassLoader classLoader = (URLClassLoader) getClassLoader();
                Class<?> urlClassLoaderClass = URLClassLoader.class;
                Method method = urlClassLoaderClass.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(classLoader, f.toURI().toURL());
                for (JarEntry jarEntry : Collections.list(test.entries())) {
                    String entry = jarEntry.getName().replace("/", ".");
                    if (entry.endsWith(".class")) {
                        Class<?> clazz = null;
                        final String substring = entry.substring(0, entry.length() - 6);
                        try {
                            clazz = Class.forName(substring);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        assert clazz != null;
                        if (EssentialsAddon.class.isAssignableFrom(clazz)) {
                            classes.add(clazz);
                        }
                    }
                }
            }
        }
        for (Class<?> aClass : classes) {
            try {
                final EssentialsAddon addon = (EssentialsAddon) aClass.getDeclaredConstructor().newInstance();
                AddonQuery.register(addon);
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e) {
                // unable to load addon
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        TeleportationManager.unregisterListeners();
        if (System.getProperty("OLD").equals("FALSE")) {
            System.setProperty("OLD", "TRUE");
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
    public Messenger getMessenger() {
        return messenger;
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
