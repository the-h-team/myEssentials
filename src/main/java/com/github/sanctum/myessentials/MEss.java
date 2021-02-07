package com.github.sanctum.myessentials;

import com.github.sanctum.myessentials.commands.HomeCommand;
import com.github.sanctum.myessentials.data.PlayerData;
import com.github.sanctum.myessentials.commands.FlyCommand;
import com.github.sanctum.myessentials.commands.SetHomeCommand;
import com.github.sanctum.myessentials.commands.TpaCommand;
import com.github.sanctum.myessentials.model.CommandData;
import com.github.sanctum.myessentials.util.Messaging;
import com.github.sanctum.myessentials.util.TeleportUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class MEss extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        CommandData.reloadConfig(this);
        Messaging.loadProperties(this);
        loadCommands();
        TeleportUtil.registerListeners(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        PlayerData.clearCache();
        TeleportUtil.unregisterListeners();
    }

    private void loadCommands() {
        new FlyCommand();
        new HomeCommand();
        new SetHomeCommand();
        new TpaCommand();
    }
}
