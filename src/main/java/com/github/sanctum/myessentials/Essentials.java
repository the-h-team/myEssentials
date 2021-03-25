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
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.CommandRegistration;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.teleportation.TeleportationManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public final class Essentials extends JavaPlugin implements MyEssentialsAPI {

    public final Set<CommandData> registeredCommands = new HashSet<>();

    public final Map<UUID, Location> previousLocation = new HashMap<>();

    private static Essentials instance;

    @Override
    public void onEnable() {
        instance = this;
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

    public static Essentials getInstance() {
        return instance;
    }
}
