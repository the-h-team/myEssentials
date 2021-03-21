package com.github.sanctum.myessentials;

import com.github.sanctum.labyrinth.event.EventBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.BaseExecutor;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.TeleportHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Essentials extends JavaPlugin {

    @Override
    public void onEnable() {
        InternalCommandData.defaultOrReload(this);
        ConfiguredMessage.loadProperties(this);
        TeleportHandler.registerListeners(this);
        BaseExecutor.compileFields(this, "com.github.sanctum.myessentials.commands");
        new EventBuilder(this).
                compileFields("com.github.sanctum.myessentials.listeners");
    }

    @Override
    public void onDisable() {
        TeleportHandler.unregisterListeners();
    }

    public static Essentials getInstance() {
        return (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
    }
}
