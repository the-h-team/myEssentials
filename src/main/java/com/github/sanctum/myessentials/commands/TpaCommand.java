package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandData;
import com.github.sanctum.myessentials.model.CommandsBase;
import com.github.sanctum.myessentials.util.Messaging;
import com.github.sanctum.myessentials.util.TeleportUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TpaCommand extends CommandsBase {
    private final PluginManager pm = Bukkit.getPluginManager();
    public TpaCommand() {
        super(CommandData.TPA_COMMAND);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, Messaging.MUST_BE_PLAYER);
            return true;
        }
        if (!testPermission(sender)) {
            return true;
        }
        final Player player = (Player) sender;
        if (args.length != 1) return false;
        Optional.ofNullable(Bukkit.getPlayerExact(args[0])).ifPresent(p2 -> {
            player.sendMessage("TP UP");
            pm.callEvent(new TeleportUtil.PendingTeleportToEntityEvent<>(player, p2));
            p2.sendMessage("INCOMING");
        });
        return true;
    }
}
