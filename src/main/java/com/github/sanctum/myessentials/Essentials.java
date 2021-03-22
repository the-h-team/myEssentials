package com.github.sanctum.myessentials;

import com.github.sanctum.labyrinth.event.EventBuilder;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.api.CommandData;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.teleportation.TeleportationManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public final class Essentials extends JavaPlugin implements MyEssentialsAPI {
    private final Set<CommandData> registeredCommands = new HashSet<>();

    @Override
    public void onEnable() {
        InternalCommandData.defaultOrReload(this);
        ConfiguredMessage.loadProperties(this);
        TeleportationManager.registerListeners(this);
        new EventBuilder(this).
                compileFields("com.github.sanctum.myessentials.listeners");
    }

    @Override
    public void onDisable() {
        TeleportationManager.unregisterListeners();
    }
}
