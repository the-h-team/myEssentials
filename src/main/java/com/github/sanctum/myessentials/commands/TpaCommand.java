package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.teleportation.PendingTeleportToPlayerEvent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class TpaCommand extends CommandBuilder {
    private final PluginManager pm = Bukkit.getPluginManager();
    public TpaCommand() {
        super(InternalCommandData.TPA_COMMAND);
    }

    private final Map<UUID, Date> request = new HashMap<>();



    private Date getRequest(Player p) {
        if (!request.containsKey(p.getUniqueId())) {
            return request.put(p.getUniqueId(), new Date());
        } else
            return request.get(p.getUniqueId());
    }



    @Override
    public boolean consoleView(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
        sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
        return true;
    }

    @Override
    public boolean playerView(@NotNull Player p, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(p)) {
            return true;
        }
        final Player player = p;
        if (args.length != 1) return false;
        Optional.ofNullable(Bukkit.getPlayerExact(args[0])).ifPresent(p2 -> {
            player.sendMessage("TP UP");
            pm.callEvent(new PendingTeleportToPlayerEvent(player, p2));
            p2.sendMessage("INCOMING");
        });
        return true;
    }
}
