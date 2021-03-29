package com.github.sanctum.myessentials.model;

import com.github.sanctum.myessentials.api.CommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Base class for gamemode switch commands.
 */
public abstract class ModeCommandBase extends CommandBuilder {
    protected final GameMode gameMode;
    protected final String gamemodeName;

    public ModeCommandBase(CommandData commandData, GameMode gameMode, String gamemodeName) {
        super(commandData);
        this.gameMode = gameMode;
        this.gamemodeName = gamemodeName;
    }

    @Override
    public @Nullable
    List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        // if more than one arg is provided, or player does not have perms return no completions
        if (args.length > 1 || !command.testPermissionSilent(player)) return Collections.emptyList();
        // return default completion (online players)
        return null;
    }

    @Override
    public final boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
        final boolean b = testPermission(player);
        if (!b) return false;
        if (args.length == 0) {
            // player has permission
            player.setGameMode(gameMode);
            sendMessage(player, "&6Your gamemode has been set to " + gamemodeName + ".");
            return true;
        }
        return setGameMode(player, args[0]);
    }

    @Override
    public final boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length != 1) {
            sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
            return false;
        }
        return setGameMode(sender, args[0]);
    }

    private boolean setGameMode(CommandSender sender, String playerName) {
        final Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            // Name isn't player
            sendMessage(sender, "&cThat is not a valid player.");
            sendUsage(sender);
        } else if (!player.isOnline()) {
            // Player must be online
            sendMessage(sender, "&cThe player must be online.");
        } else {
            // valid player
            player.setGameMode(gameMode);
            sendMessage(sender, "&6Set &e" + playerName + " &6to " + gamemodeName + ".");
            return true;
        }
        return false;
    }
}
