/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.Node;
import com.github.sanctum.myessentials.Essentials;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum InternalCommandData implements CommandData {
	FLY_COMMAND("fly"),
	TPA_COMMAND("tpa"),
	TPA_HERE_COMMAND("tpahere"),
    TPA_CANCEL_COMMAND("tpacancel"),
    TP_ACCEPT_COMMAND("tpaccept"),
	TP_REJECT_COMMAND("tpreject"),
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
    NIGHT_COMMAND("night"),
    ONLINELIST_COMMAND("online"),
	POWERTOOL_COMMAND("powertool"),
	RELOAD_COMMAND("reload"),
	SPAWNMOB_COMMAND("spawnmob"),
	STAFF_COMMAND("staff"),
	TELEPORT_COMMAND("teleport"),
	UNBAN_COMMAND("unban"),
	UPDATE_COMMAND("update"),
	WHOIS_COMMAND("whois"),
	WORLD_COMMAND("world"),
	TRANSITION_COMMAND("transition");


	private static FileManager fileManager;
	public String configNode;

	InternalCommandData(String configNode) {
		this.configNode = configNode;
	}

	@Override
    public @NotNull String getLabel() {
		return Objects.requireNonNull(fileManager.getRoot().getString(configNode + ".label"));
    }

    @Override
    public @NotNull List<String> getAliases() {
	    final Node node = fileManager.getRoot().getNode(configNode);
	    if (node.getNode("aliases").toPrimitive().isStringList()) {
		    return node.getNode("aliases").toPrimitive().getStringList();
	    }
	    return CommandData.super.getAliases();
    }

    @Override
    public @NotNull String getUsage() {
	    return Objects.requireNonNull(fileManager.getRoot().getString(configNode + ".usage"));
    }

    @Override
    public @NotNull String getDescription() {
	    return Objects.requireNonNull(fileManager.getRoot().getString(configNode + ".description"));
    }

    @Override
    public @Nullable String getPermissionNode() {
	    return fileManager.getRoot().getString(configNode + ".permission");
    }

    public static void defaultOrReload(Essentials plugin) {
	    if (fileManager == null) fileManager = plugin.getFileList().get("commands", "Configuration");
	    if (!fileManager.getRoot().exists()) {
		    final InputStream resource = plugin.getResource("commands.yml");
		    if (resource == null) {
			    throw new IllegalStateException("Unable to load internal command data from the jar! something is very wrong");
		    }
		    FileList.copy(resource, fileManager.getRoot().getParent());
	    }
	    if (fileManager.getRoot().exists() && !fileManager.getRoot().getKeys(false).isEmpty()) {
		    fileManager.getRoot().reload();
	    }
    }
}
