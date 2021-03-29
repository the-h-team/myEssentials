package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class GMToggleCommand extends CommandBuilder {
    public GMToggleCommand() {
        super(InternalCommandData.GM_COMMAND);
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        // if more than one arg is provided, or player does not have perms return no completions
        if (args.length > 1 || !command.testPermissionSilent(player)) return Collections.emptyList();
        // return default completion (online players)
        return null;
    }

    @Override
    public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length < 1) {
            // set for player
            final GameMode original = player.getGameMode();
            final GameMode newMode;
            if (original == GameMode.SURVIVAL) {
                newMode = GameMode.CREATIVE;
            } else if (original == GameMode.CREATIVE) {
                newMode = GameMode.SURVIVAL;
            } else {
                sendMessage(player, "You are not currently in survival or creative.");
                return true;
            }
            player.setGameMode(newMode);
            sendMessage(player, "&6Your gamemode has been set to " + newMode.name().toLowerCase() + ".");
            return true;
        } else if (args.length > 1) {
            sendUsage(player);
            return false;
        }
        return toggleForPlayer(player, args[0]);
    }

    @Override
    public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length < 1) {
            sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
        } else if (args.length == 1) {
            return toggleForPlayer(sender, args[0]);
        }
        sendUsage(sender);
        return false;
    }

    private boolean toggleForPlayer(CommandSender sender, String playerName) {
        // check if arg = playerName
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
            // get current gamemode
            final GameMode current = player.getGameMode();
            final GameMode newMode;
            if (current == GameMode.SURVIVAL) {
                newMode = GameMode.CREATIVE;
            } else if (current == GameMode.CREATIVE) {
                newMode = GameMode.SURVIVAL;
            } else {
                sendMessage(sender, "The player is not currently in survival or creative.");
                return true;
            }
            player.setGameMode(newMode);
            sendMessage(sender, "&6Set &e" + playerName + " &6to " + newMode.name().toLowerCase() + ".");
            return true;
        }
        return false;
    }
}
