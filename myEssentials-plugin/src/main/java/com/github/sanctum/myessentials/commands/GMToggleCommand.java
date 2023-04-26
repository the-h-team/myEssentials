package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandInput;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GMToggleCommand extends CommandInput {
	public GMToggleCommand() {
		super(InternalCommandData.GM_COMMAND);
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		// if more than one arg is provided, or player does not have perms return no completions
		if (args.length > 1 || !command.testPermissionSilent(player)) return Collections.emptyList();
		// return default completion (online players)
		return null;
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {

		if (!testPermission(player))
			return true;

		if (args.length < 1) {
			// set for player
			final GameMode original = player.getGameMode();
			final GameMode newMode;
			if (original == GameMode.SURVIVAL) {
				newMode = GameMode.CREATIVE;
            } else if (original == GameMode.CREATIVE) {
                newMode = GameMode.SURVIVAL;
            } else {
                sendMessage(player, ConfiguredMessage.NOT_IN_SURVIVAL_OR_CREATIVE);
                return true;
            }
            player.setGameMode(newMode);
            sendMessage(player, ConfiguredMessage.PLAYER_GAMEMODE_SET.replace(newMode.name().toLowerCase()));
            return true;
        } else if (args.length > 1) {
            sendUsage(player);
            return false;
        }
        return toggleForPlayer(player, args[0]);
    }

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
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
            sendMessage(sender, ConfiguredMessage.NOT_VALID_PLAYER);
            sendUsage(sender);
        } else if (!player.isOnline()) {
            // Player must be online
            sendMessage(sender, ConfiguredMessage.PLAYER_MUST_BE_ONLINE);
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
                sendMessage(sender, ConfiguredMessage.TARGET_NOT_SURVIVAL_CREATIVE);
                return true;
            }
            player.setGameMode(newMode);
            sendMessage(sender, ConfiguredMessage.TARGET_GAMEMODE_SET.replace(playerName, newMode.name().toLowerCase()));
            return true;
        }
        return false;
    }
}
