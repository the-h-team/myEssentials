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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.EnumMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides configurable messages.
 */
public enum ConfiguredMessage implements ProvidedMessage {
    // Info
    @Section("Info") PREFIX(".prefix"),

    // Errors
    @Section("Errors") MUST_BE_PLAYER(".must-be-player"),
    @Section("Errors") TRY_IN_SURVIVAL(".try-in-survival"),
    @Section("Errors") NOT_VALID_PLAYER(".not-valid-player"),
    @Section("Errors") PLAYER_NOT_ONLINE(".player-not-online"),
    @Section("Errors") TARGET_NOT_FOUND(".target-not-found"), // Takes 1 replacement

    // Command messages (Default section)
    // /Fly
    FLIGHT_OFF(".fly.flight-off"),
    FLIGHT_ON(".fly.flight-on"),
    // /Back
    NO_PREVIOUS_LOCATION(".back.no-previous-location"),
    TELEPORTED_PREVIOUS(".back.teleported-previous"),
    // /Ban
    BANNED_TARGET(".ban.banned-target"),
    TARGET_ALREADY_BANNED(".ban.target-already-banned"),
    BANNED_REASON(".ban.banned-reason"), // Takes 1 replacement
    // -bin, -broadcast
    // /Day
    DAY_VALUES_DESC(".day.values"),
    SET_NOW_DAY(".day.set-now-day"),
    SET_MORNING(".day.set-morning"),
    SET_NOON(".day.set-noon"),
    SET_AFTERNOON(".day.set-afternoon"),
    // -feed
    // /Gamemode
    SET_GAMEMODE(".gamemode.set-gamemode"), // Takes 2 replacements
    // /Gm (toggle)
    NOT_IN_SURVIVAL_OR_CREATIVE(".gm-toggle.not-in-survival-creative"),
    PLAYER_GAMEMODE_SET(".gm-toggle.player-gamemode-set"), // Takes 1 replacement
    TARGET_NOT_SURVIVAL_CREATIVE(".gm-toggle.target-not-in-survival-creative"),
    TARGET_GAMEMODE_SET(".gm-toggle.target-gamemode-set"), // Takes 2 replacements
    // -god
    // /Heal
    HEAL_TARGET_MAXED(".heal.target-max"), // Takes 1 replacement
    HEAL_TARGET_NOT_ONLINE(".heal.target-not-online"), // Takes 1 replacement
    CONSOLE_HEAL_TARGET_MAXED(".heal.console.max"), // Takes 1 replacement
    CONSOLE_HEAL_TARGET_NOT_ONLINE(".heal.console.not-online"), // Takes 1 replacement
    CONSOLE_HEAL_TARGET_NOT_FOUND(".heal.console.not-found"), // Takes 1 replacement
    // -help
    // /Invsee
    INVSEE_DENY_SELF(".invsee.deny-own-inventory"),
    PLAYER_NOT_FOUND(".invsee.player-not-found"),
    // -item, -kickall, -message
    // /Night
    NIGHT_VALUES_DESC(".night.values"),
    NOW_DAY(".night.now-day"),
    NOW_NIGHT(".night.now-night"),
    NOW_MIDNIGHT(".night.now-midnight"),
    NOW_DUSK(".night.now-dusk"),
    // -onlinelist, -powertool, -reload, -reply, -socialspy, -spawnmob, -staff, -teleport
    // /Tempban
    REASON(".tempban.a-reason"),
    INVALID_TIME_FORMAT(".tempban.invalid-format"),
    TIME_EXAMPLE(".tempban.example"),
    BAN_KICK_1(".tempban.kick.line1"),
    BAN_KICK_2(".tempban.kick.line2"),
    BAN_KICK_TO_SENDER(".tempban.kick.to-sender"), // Takes 1 replacement
    UNBANNED_TIME(".tempban.will-be-unbanned"),
    BAN_KICK_3(".tempban.kick2.line3"),
    INVALID_TIME_CONSOLE(".tempban.console.invalid-time"),
    // -tpa
    // /Unban
    TARGET_UNBANNED(".unban.target-unbanned"),
    TARGET_NOT_BANNED(".unban.target-not-banned"),
    // -update, -whois
    // /World
    WORLD_SEARCH_INTERRUPTED(".world.search-interrupted"),
    TELEPORTED_SAFEST_LOCATION(".world.safest-location"), // Takes 1 replacement
    // /Kick
    YOU_WERE_KICKED(".kick.you-were-kicked"),
    DEFAULT_KICK_REASON(".kick.default-reason"),
    TARGET_KICKED(".kick.target-kicked"),
    TARGET_OFFLINE(".kick.target-offline"),
    CUSTOM_KICK_REASON(".kick.custom-reason"), // Takes 1 replacement
    TARGET_KICKED_WITH_REASON(".kick.target-kicked-reason") // Takes 1 replacement
    ;

    private static FileManager fileManager;
    private static final EnumMap<ConfiguredMessage, String> reflectSectionCache = new EnumMap<>(ConfiguredMessage.class);

    private final String key;

    ConfiguredMessage(String s) {
        this.key = s;
    }

    @Override
    @Nullable
    public String get() {
        final String section = reflectSectionCache.computeIfAbsent(this, node -> {
            try {
                final Field field = node.getDeclaringClass().getField(node.name());
                field.setAccessible(true);
                final Annotation[] declaredAnnotations = field.getDeclaredAnnotations();
                for (Annotation annotation : declaredAnnotations) {
                    if (annotation.annotationType().isInstance(Section.class)) {
                        return ((Section) annotation).value();
                    }
                }
                return "Commands";
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException("This is not good.", e);
            }
        });
        return fileManager.getConfig().getString("Messages.".concat(section).concat(key));
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

    public @interface Section {
        String value();
    }
}
