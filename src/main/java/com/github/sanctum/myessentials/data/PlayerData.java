package com.github.sanctum.myessentials.data;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerData {
    private static final JavaPlugin PLUGIN = JavaPlugin.getProvidingPlugin(PlayerData.class);
    private static final File DATA_FOLDER = new File(PLUGIN.getDataFolder(), "players");
    private static final Map<Player, PlayerData> PLAYERS = new HashMap<>();
    private final UUID uuid;
    private final File dataFile;
    private YamlConfiguration configuration;

    private PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.dataFile = new File(DATA_FOLDER, uuid.toString() + ".yml");
        reloadFromDisk();
    }

    public Map<String, Location> getHomes() {
        if (!configuration.isSet("homes")) {
            return Collections.emptyMap();
        }
        final Map<String, Location> locations = new HashMap<>();
        for (String home : Objects.requireNonNull(configuration.getConfigurationSection("homes")).getKeys(false)) {
            locations.put(home, configuration.getLocation("homes." + home));
        }
        return locations;
    }

    public void saveHome(String name, Location location) {
        configuration.set("homes." + name, location);
        saveAsync();
    }

    void saveAsync() {
        new BukkitRunnable() {
            @Override
            public void run() {
                save();
            }
        }.runTaskAsynchronously(PLUGIN);
    }

    private synchronized void save() {
        try {
            configuration.save(dataFile);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create player datafile for " + uuid, e);
        }
    }

    synchronized void reloadFromDisk() {
        configuration = dataFile.exists() ? YamlConfiguration.loadConfiguration(dataFile) : new YamlConfiguration();
    }

    public static PlayerData getPlayerData(Player player) {
        return PLAYERS.computeIfAbsent(player, PlayerData::new);
    }

    public static void clearCache() {
        PLAYERS.clear();
    }
}
