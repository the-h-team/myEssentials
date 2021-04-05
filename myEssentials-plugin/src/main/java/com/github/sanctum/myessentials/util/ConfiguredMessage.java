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

import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.Essentials;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides configurable messages.
 */
public enum ConfiguredMessage implements ProvidedMessage {
    MUST_BE_PLAYER("Errors.must-be-player"),
    TRY_IN_SURVIVAL("Errors.try-in-survival"),
    // Fly messages
    FLIGHT_OFF("Commands.fly.flight-off"),
    FLIGHT_ON("Commands.fly.flight-on"),
    PREFIX("Info.prefix");

    private static YamlConfiguration yamlConfiguration;

    private final String key;

    ConfiguredMessage(String s) {
        this.key = s;
    }

    @Override
    @Nullable
    public String get() {
        return yamlConfiguration.getString("Messages.".concat(key));
    }

    @Override
    public @NotNull String toString() {
        final String s = get();
        return (s != null) ? StringUtils.translate(s) : "null";
    }

    public static void loadProperties(Essentials essentials) {
        final YamlConfiguration defaults = new YamlConfiguration();
        final InputStream resource = essentials.getResource("messages.yml");
        try {
            defaults.load(new InputStreamReader(Objects.requireNonNull(resource)));
        } catch (IOException e) {
            throw new IllegalStateException("Messages missing from the .jar!", e);
        } catch (InvalidConfigurationException e) {
            throw new IllegalStateException("messages.yml corrupted! Please check the jar.");
        }
        final File file = new File(essentials.getDataFolder(), "messages.yml");
        if (file.exists()) {
            yamlConfiguration = new YamlConfiguration();
            yamlConfiguration.addDefaults(defaults);
            try {
                yamlConfiguration.load(new InputStreamReader(new FileInputStream(file)));
                return;
            } catch (IOException e) {
                essentials.getLogger().severe("Unable to load external copy of messages.yml");
            } catch (InvalidConfigurationException e) {
                throw new IllegalStateException("Unable to properly load messages.yml from disk!");
            }
        }
        yamlConfiguration = defaults;
    }
}
