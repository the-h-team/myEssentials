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
package com.github.sanctum.myessentials.util;

import com.github.sanctum.myessentials.Essentials;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Objects;
import java.util.Properties;

/**
 * Provides configurable messages.
 */
public enum ConfiguredMessage implements ProvidedMessage {
    MUST_BE_PLAYER("must-be-player"),
    TRY_IN_SURVIVAL("try-in-survival"),
    // Fly messages
    FLIGHT_OFF("flight-off"),
    FLIGHT_ON("flight-on");

    private final String key;

    private static Properties properties;

    ConfiguredMessage(String s) {
        this.key = s;
    }

    @Override
    @Nullable
    public String get() {
        return properties.getProperty(key);
    }

    @Override
    public @NotNull String toString() {
        final String s = get();
        return (s != null) ? ChatColor.translateAlternateColorCodes('&', s) : "null";
    }

    public static void loadProperties(Essentials essentials) {
        final Properties defaults = new Properties();
        final InputStream resource = essentials.getResource("messages.properties");
        try {
            defaults.load(new InputStreamReader(Objects.requireNonNull(resource)));
        } catch (IOException e) {
            throw new IllegalStateException("Messages missing from the .jar!", e);
        }
        final File file = new File(essentials.getDataFolder(), "messages.properties");
        if (file.exists()) {
            properties = new Properties(defaults);
            try {
                properties.load(new FileInputStream(file));
            } catch (IOException e) {
                essentials.getLogger().severe("Unable to load external copy of messages.properties");
                e.printStackTrace();
            }
            return;
        }
        properties = defaults;
    }
}
