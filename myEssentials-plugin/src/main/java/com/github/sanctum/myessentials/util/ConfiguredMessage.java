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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.EnumMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides configurable messages.
 */
public enum ConfiguredMessage implements ProvidedMessage {
    // === Info ===
    @Section("Info") PREFIX(".prefix"),

    // === Errors ===
    @Section("Errors") MUST_BE_PLAYER(".must-be-player"),
    @Section("Errors") TRY_IN_SURVIVAL(".try-in-survival"),
    @Section("Errors") NOT_VALID_PLAYER(".not-valid-player"),
    @Section("Errors") PLAYER_MUST_BE_ONLINE(".player-not-online"),
    @Section("Errors") TARGET_NOT_FOUND(".target-not-found"), // Takes 1 replacement

    // === Command messages (Default section) ===
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
    SET_DAY(".day.set-day"),
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
    SET_NIGHT(".night.set-night"),
    SET_MIDNIGHT(".night.set-midnight"),
    SET_DUSK(".night.set-dusk"),
    // -onlinelist, -powertool, -reload, -reply, -socialspy, -spawnmob, -staff, -teleport
    // /Tempban
    REASON(".tempban.a-reason"),
    INVALID_TIME_FORMAT(".tempban.invalid-format"),
    TIME_EXAMPLE(".tempban.example"),
    YOU_HAVE_BEEN_BANNED(".tempban.kick.line1"),
    BAN_EXPIRATION(".tempban.kick.line2"), // Takes 1 replacement
    UNBAN_TIME_TO_SENDER(".tempban.kick.to-sender"), // Takes 1 replacement
    UNBANNED_TIME(".tempban.will-be-unbanned"),
    BAN_KICK_REASON(".tempban.kick2.line3"),
    INVALID_TIME_CONSOLE(".tempban.console.invalid-time"),
    // -tpa
    // /Unban
    TARGET_UNBANNED(".unban.target-unbanned"),
    TARGET_NOT_BANNED(".unban.target-not-banned"),
    // -update, -whois
    // /World
    STOPPING_SEARCH(".world.stopping-search"),
    SEARCH_INTERRUPTED(".world.search-interrupted"),
    TELEPORTED_SAFEST_LOCATION(".world.safest-location"), // Takes 1 replacement
    // /Kick
    YOU_WERE_KICKED(".kick.you-were-kicked"),
    DEFAULT_KICK_REASON(".kick.default-reason"),
    TARGET_KICKED(".kick.target-kicked"),
    TARGET_OFFLINE(".kick.target-offline"),
    CUSTOM_KICK_REASON(".kick.custom-reason"), // Takes 1 replacement
    TARGET_KICKED_WITH_REASON(".kick.target-kicked-reason"), // Takes 1 replacement
    // /Transition
    TRANSITION_IN_PROGRESS(".transition.transition-in-progress"),
    ALREADY_DAY(".transition.already-day"),
    ALREADY_NIGHT(".transition.already-night"),
    TRANSITION_TOO_FAST(".transition.too-fast"),
    // /Tpa
    TPA_SENT(".tpa.request-sent"), // Takes 1 replacement
    STAND_STILL(".tpa.stand-still"),
    TP_CANCELLED(".tpa.tp-cancelled"),
    TP_SUCCESS(".tpa.tp-success"),
    TPA_TO_CANCEL_TEXT(".tpa.to-cancel.text"),
    TPA_TO_CANCEL_BUTTON(".tpa.to-cancel.button"),
    TPA_TO_CANCEL_TEXT2(".tpa.to-cancel.text2"), // Takes 1 replacement
    TPA_TO_CANCEL_HOVER(".tpa.to-cancel.hover"),
    TPA_REQUEST_TO_YOU(".tpa.request-to-you"),
    TPA_TO_ACCEPT_TEXT(".tpa.to-accept.text"),
    TPA_TO_ACCEPT_BUTTON(".tpa.to-accept.button"),
    TPA_TO_ACCEPT_TEXT2(".tpa.to-accept.text2"), // Takes 1 replacement
    TPA_TO_ACCEPT_HOVER(".tpa.to-accept.hover"),
    TPA_TO_REJECT_TEXT(".tpa.to-reject.text"),
    TPA_TO_REJECT_BUTTON(".tpa.to-reject.button"),
    TPA_TO_REJECT_TEXT2(".tpa.to-reject.text2"), // Takes 1 replacement
    TPA_TO_REJECT_HOVER(".tpa.to-reject.hover"),
    // /TpaHere
    TPA_HERE_REQUESTED(".tpa-here.requested"), // Takes 1 replacement
    // /god
    GOD_ENABLED(".god.enabled"),
    GOD_DISABLED(".god.disabled"),
    GOD_ENABLED_OTHER(".god.other.enabled"),
    GOD_DISABLED_OTHER(".god.other.disabled"),

    // === Event messages ===
    // PlayerHealEvent
    @Section("Events") PLAYER_HEALED_YOU(".PlayerHealEvent.player-healed-you"), // Takes 2 replacements
    @Section("Events") CONSOLE_HEALED_YOU(".PlayerHealEvent.console-healed-you"), // Takes 1 replacement
    @Section("Events") HEALED(".PlayerHealEvent.general"),
    @Section("Events") PLAYER_FED_YOU(".PlayerFeedEvent.player-fed-you"), // Takes 2 replacements
    @Section("Events") CONSOLE_FED_YOU(".PlayerFeedEvent.console-fed-you"), // Takes 1 replacement
    @Section("Events") FED(".PlayerFeedEvent.general"),
    // PlayerLoginEvent
    @Section("Events") LOGIN_TEMP_BANNED(".PlayerLoginEvent.temp-banned"),
    @Section("Events") LOGIN_BANNED_REASON(".PlayerLoginEvent.reason"), // Takes 1 replacement
    @Section("Events") LOGIN_BAN_EXPIRES(".PlayerLoginEvent.expires"), // Takes 1 replacement
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
                    if (annotation.annotationType() == Section.class) {
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
        return (s != null) ? StringUtils.use(s).translate() : "null";
    }

    public static void loadProperties(Essentials essentials) {
        if (fileManager == null) fileManager = essentials.getFileList().find("messages", "Configuration");
        final InputStream resource = essentials.getResource("messages.yml");
        if (!fileManager.exists()) {
            assert resource != null;
            FileManager.copy(resource, fileManager.getFile());
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Section {
        String value();
    }
}
