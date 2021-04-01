/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.api.CommandData;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum InternalCommandData implements CommandData {
	FLY_COMMAND("fly"),
	TPA_COMMAND("tpa"),
	BACK_COMMAND("back"),
	BAN_COMMAND("ban"),
	TEMPBAN_COMMAND("tempban"),
	BIN_COMMAND("bin"),
	BROADCAST_COMMAND("broadcast"),
	DAY_COMMAND("day"),
	FEED_COMMAND("feed"),
	GAMEMODE_COMMAND("gamemode"),
	GIVE_COMMAND("give"),
	GM_COMMAND("gm-toggle"),
	GMA_COMMAND("gma"),
	GMC_COMMAND("gmc"),
	GMS_COMMAND("gms"),
    GMSP_COMMAND("gmsp"),
    GOD_COMMAND("god"),
    HEAL_COMMAND("heal"),
    HELP_COMMAND("help"),
    INVSEE_COMMAND("invsee"),
    ITEM_COMMAND("item"),
    KICKALL_COMMAND("kickall"),
    KICK_COMMAND("kick"),
    MESSAGE_COMMAND("message"),
    NIGHT_COMMAND("night"),
    ONLINELIST_COMMAND("online"),
    POWERTOOL_COMMAND("powertool"),
    RELOAD_COMMAND("reload"),
    REPLY_COMMAND("reply"),
    SOCIALSPY_COMMAND("socialspy"),
    SPAWNMOB_COMMAND("spawnmob"),
    STAFF_COMMAND("staff"),
    TELEPORT_COMMAND("teleport"),
    UNBAN_COMMAND("unban"),
    UPDATE_COMMAND("update"),
    WHOIS_COMMAND("whois"),
    WORLD_COMMAND("world");


    private static FileManager CONFIG;
    public String configNode;

    InternalCommandData(String configNode) {
        this.configNode = configNode;
    }

    @Override
    public @NotNull String getLabel() {
        return Objects.requireNonNull(CONFIG.getConfig().getString(configNode + ".label"));
    }

    @Override
    public @NotNull List<String> getAliases() {
        final ConfigurationSection configurationSection = CONFIG.getConfig().getConfigurationSection(configNode);
        if (configurationSection != null && configurationSection.contains("aliases")) {
            return configurationSection.getStringList("aliases");
        }
        return CommandData.super.getAliases();
    }

    @Override
    public @NotNull String getUsage() {
        return Objects.requireNonNull(CONFIG.getConfig().getString(configNode + ".usage"));
    }

    @Override
    public @NotNull String getDescription() {
        return Objects.requireNonNull(CONFIG.getConfig().getString(configNode + ".description"));
    }

    @Override
    public @Nullable String getPermissionNode() {
        return CONFIG.getConfig().getString(configNode + ".permission");
    }

    public boolean testNoPermission(CommandSender sender) {
        final String permissionNode = getPermissionNode();
        if (permissionNode == null) return false;
        return !sender.hasPermission(permissionNode);
    }

    public static void defaultOrReload(Essentials plugin) {
        if (CONFIG == null) CONFIG = plugin.getFileList().find("commands", null);
        if (!CONFIG.exists()) {
            final InputStream resource = plugin.getResource("commands.yml");
            if (resource == null) {
                throw new IllegalStateException("Unable to load internal command data from the jar! something is very wrong");
            }
            FileManager.copy(resource, CONFIG.getFile());
        }
        if (CONFIG.exists() && !CONFIG.getConfig().getKeys(false).isEmpty()) {
            CONFIG.reload();
        }
    }
}
