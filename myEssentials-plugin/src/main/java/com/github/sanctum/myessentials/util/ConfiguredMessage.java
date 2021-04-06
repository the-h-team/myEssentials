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

import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.Essentials;
import java.io.InputStream;
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

    private static FileManager fileManager;

    private final String key;

    ConfiguredMessage(String s) {
        this.key = s;
    }

    @Override
    @Nullable
    public String get() {
        return fileManager.getConfig().getString("Messages".concat(key));
    }

    @Override
    public @NotNull String toString() {
        final String s = get();
        return (s != null) ? StringUtils.translate(s) : "null";
    }

    public static void loadProperties(Essentials essentials) {
        if (fileManager == null) fileManager = essentials.getFileList().find("messages", "Configuration");
        final InputStream resource = essentials.getResource("messages.yml");
        if (!fileManager.exists()) {
            assert resource != null;
            FileManager.copy(resource, fileManager.getFile());
        }
    }
}
