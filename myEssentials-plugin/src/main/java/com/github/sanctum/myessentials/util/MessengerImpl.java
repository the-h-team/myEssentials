package com.github.sanctum.myessentials.util;

import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.api.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public final class MessengerImpl implements Messenger {
    private final Essentials essentials;

    public MessengerImpl(Essentials essentials) {
        this.essentials = essentials;
    }

    @Override
    public void broadcastMessage(String message) {
        Bukkit.broadcastMessage(StringUtils.translate(message));
    }

    @Override
    public boolean broadcastMessage(CommandSender sender, String message) {
        if (!sender.hasPermission("mess.broadcast")) {
            return false;
        }
        broadcastMessage(message);
        return true;
    }

    @Override
    public boolean broadcastMessagePrefixed(CommandSender sender, String message) {
        if (!sender.hasPermission("mess.broadcast")) {
            return false;
        }
        broadcastMessage("&7[&2" + essentials.getName() + "&7]&r" + message);
        return true;
    }
}
