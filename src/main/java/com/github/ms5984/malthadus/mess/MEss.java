package com.github.ms5984.malthadus.mess;

import com.github.ms5984.malthadus.mess.commands.FlyCommand;
import com.github.ms5984.malthadus.mess.commands.HomeCommand;
import com.github.ms5984.malthadus.mess.commands.SetHomeCommand;
import com.github.ms5984.malthadus.mess.commands.TpaCommand;
import com.github.ms5984.malthadus.mess.data.PlayerData;
import com.github.ms5984.malthadus.mess.model.CommandData;
import com.github.ms5984.malthadus.mess.util.Messaging;
import com.github.ms5984.malthadus.mess.util.TeleportUtil;
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
