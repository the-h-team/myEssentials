package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.data.PlayerData;
import com.github.sanctum.myessentials.model.CommandData;
import com.github.sanctum.myessentials.model.CommandsBase;
import com.github.sanctum.myessentials.util.Messaging;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HomeCommand extends CommandsBase {
    public HomeCommand() {
        super(CommandData.HOME_COMMAND);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Messaging.MUST_BE_PLAYER);
            return true;
        }
        if (!testPermission(sender)) { // automatically sends no-perms message
            return true;
        }
        final Player player = (Player) sender;
        final PlayerData playerData = PlayerData.getPlayerData(player.getPlayer());
        if (args.length > 1) {
            return false;
        }
        final Map<String, Location> homes = playerData.getHomes();
        if (args.length == 0) {
            final Location location = homes.get("home");
            if (location == null) {
                if (homes.isEmpty()) {
                    sendMessage(player, Messaging.NO_HOME_SET);
                } else {
                    final StringBuilder sb = new StringBuilder();
                    boolean first = true;
                    for (String home : homes.keySet()) {
                        if (first) {
                            sb.append(home);
                            first = false;
                            continue;
                        }
                        sb.append(", ").append(home);
                    }
                    player.sendMessage(Messaging.HOMES_LISTING.replace(sb));
                }
            } else {
                player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        } else {
            final String homeSelected = args[0];
            if (!homes.containsKey(homeSelected)) {
                sendMessage(player, Messaging.HOME_NOT_FOUND);
                return true;
            }
            final Location location = homes.get(homeSelected);
            player.teleport(location);
        }
        return true;
    }
}
