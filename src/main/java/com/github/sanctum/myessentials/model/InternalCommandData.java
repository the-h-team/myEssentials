package com.github.sanctum.myessentials.model;

import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.data.Config;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Objects;

public enum InternalCommandData implements CommandData {
    FLY_COMMAND("fly-command"),
    TPA_COMMAND("tpa-command"),
    BACK_COMMAND("back-command"),
    BAN_COMMAND("ban-command"),
    BROADCAST_COMMAND("broadcast-command"),
    CLAIM_COMMAND("claim-command"),
    DAY_COMMAND("day-command"),
    FEED_COMMAND("feed-command"),
    GAMEMODE_COMMAND("gamemode-command"),
    GIVE_COMMAND("give-command"),
    GMA_COMMAND("gma-command"),
    GMC_COMMAND("gmc-command"),
    GMS_COMMAND("gms-command"),
    GMSP_COMMAND("gmsp-command"),
    GOD_COMMAND("god-command"),
    HEAL_COMMAND("heal-command"),
    HELP_COMMAND("heal-command"),
    INVSEE_COMMAND("invsee-command"),
    ITEM_COMMAND("item-command"),
    KICKALL_COMMAND("kickall-command"),
    KICK_COMMAND("kick-command"),
    MESSAGE_COMMAND("message-command"),
    MUTECHAT_COMMAND("mutechat-command"),
    NIGHT_COMMAND("night-command"),
    ONLINELIST_COMMAND("onlinelist-command"),
    POWERTOOL_COMMAND("powertool-command"),
    RELOAD_COMMAND("reload-command"),
    REPLY_COMMAND("reply-command"),
    SOCIALSPY_COMMAND("socialspy-command"),
    SPAWNMOB_COMMAND("spawnmob-command"),
    STAFF_COMMAND("staff-command"),
    TELEPORT_COMMAND("teleport-command"),
    UNBAN_COMMAND("unban-command"),
    UPDATE_COMMAND("update-command"),
    WHOIS_COMMAND("whois-command"),
    WORLD_COMMAND("world-command");


    public String configNode;

    private static final Config commands = Config.get("commands", null);

    InternalCommandData(String configNode) {
        this.configNode = configNode;
    }

    @Override
    public @NotNull String getLabel() {
        return Objects.requireNonNull(commands.getConfig().getString(configNode + ".command"));
    }

    @Override
    public @NotNull String getDescription() {
        return Objects.requireNonNull(commands.getConfig().getString(configNode + ".description"));
    }

    @Override
    public @Nullable String getPermissionNode() {
        return commands.getConfig().getString(configNode + ".permission");
    }

    public boolean testNoPermission(CommandSender sender) {
        final String permissionNode = getPermissionNode();
        if (permissionNode == null) return false;
        return !sender.hasPermission(permissionNode);
    }

    public static void defaultOrReload(Essentials plugin) {
        final InputStream resource = plugin.getResource("commands.yml");

        if (!commands.exists()) {
            Config.copy(resource, commands.getFile());
        }
        if (commands.exists() && !commands.getConfig().getKeys(false).isEmpty()) {
            commands.reload();
        }
    }
}
