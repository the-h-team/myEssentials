package com.github.sanctum.myessentials.model;

import com.github.sanctum.myessentials.MEss;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public enum CommandData {
    FLY_COMMAND("fly-command"),
    HOME_COMMAND("home-command"),
    SETHOME_COMMAND("sethome-command"),
    TPA_COMMAND("tpa-command");

    public String configNode;

    private static Configuration configuration;

    CommandData(String configNode) {
        this.configNode = configNode;
    }

    public String getLabel() {
        return configuration.getString(configNode + ".command");
    }

    public String getDescription() {
        return configuration.getString(configNode + ".description");
    }

    public String getPermissionNode() {
        return configuration.getString(configNode + ".permission");
    }

    public boolean testNoPermission(CommandSender sender) {
        return !sender.hasPermission(getPermissionNode());
    }

    public static void reloadConfig(MEss plugin) {
        final InputStream resource = plugin.getResource("Commands.yml");
        if (resource == null) throw new IllegalStateException("Commands.yml missing from the jar!");
        final Configuration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(resource));
        final File file = new File(plugin.getDataFolder(), "Commands.yml");
        if (file.exists()) {
            configuration = YamlConfiguration.loadConfiguration(file);
        } else {
            configuration = new YamlConfiguration();
        }
        configuration.addDefaults(defaults);
    }
}
