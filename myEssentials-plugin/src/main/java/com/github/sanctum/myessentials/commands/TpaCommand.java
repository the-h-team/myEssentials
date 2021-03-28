/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials, a derivative work inspired by the
 *  Essentials <http://ess3.net/> and EssentialsX <https://essentialsx.net/>
 *  projects, both licensed under the GPLv3.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.events.MEssPendingTeleportToPlayerEvent;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TpaCommand extends CommandBuilder {
    private final PluginManager pm = Bukkit.getPluginManager();

    public TpaCommand() {
        super(InternalCommandData.TPA_COMMAND);
    }

    private final Map<UUID, Date> request = new HashMap<>();

    @Override
    public @Nullable
    List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return null;
    }

    private Date getRequest(Player p) {
        if (!request.containsKey(p.getUniqueId())) {
            return request.put(p.getUniqueId(), new Date());
        } else
            return request.get(p.getUniqueId());
    }



    @Override
    public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
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
            pm.callEvent(new MEssPendingTeleportToPlayerEvent(player, p2));
            p2.sendMessage("INCOMING");
        });
        return true;
    }
}
