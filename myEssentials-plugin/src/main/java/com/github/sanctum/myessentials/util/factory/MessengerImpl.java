package com.github.sanctum.myessentials.util.factory;

import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.model.Messenger;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public final class MessengerImpl implements Messenger {
    private final Essentials essentials;

    public MessengerImpl(Essentials essentials) {
        this.essentials = essentials;
    }

    @Override
    public void broadcastMessage(String message) {
        Bukkit.broadcastMessage(StringUtils.use(message).translate());
    }

    @Override
    public boolean broadcastMessage(CommandSender sender, String message) {
        if (!sender.hasPermission(Objects.requireNonNull(InternalCommandData.BROADCAST_COMMAND.getPermissionNode()))) {
            return false;
        }
        broadcastMessage(message);
        return true;
    }

    @Override
    public boolean broadcastMessagePrefixed(CommandSender sender, String message) {
        if (!sender.hasPermission(Objects.requireNonNull(InternalCommandData.BROADCAST_COMMAND.getPermissionNode()))) {
            return false;
        }
        broadcastMessage("&7[&2" + essentials.getName() + "&7]&r" + message);
        return true;
    }
}
