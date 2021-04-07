package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TpaHereCommand extends CommandBuilder {
    private final TextLib textLib = TextLib.getInstance();

    public TpaHereCommand() {
        super(InternalCommandData.TPA_HERE_COMMAND);
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length > 1 || !testPermission(player)) return Collections.emptyList();
        return null;
    }

    @Override
    public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
        return false;
    }

    @Override
    public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(player)) {
            return false;
        }
        if (args.length != 1) {
            sendUsage(player);
            return false;
        }
        Optional.ofNullable(Bukkit.getPlayerExact(args[0])).ifPresent(target -> {
            api.getTeleportRunner().requestTeleport(player, target, player);
            sendMessage(player, "&aRequest sent to &e{0}".replaceAll("\\{0}", target.getDisplayName()));
            player.spigot().sendMessage(textLib.textRunnable(
                    "To cancel this request, click &7[",
                    "&lhere",
                    "&7]&r or type &7/" + InternalCommandData.TPA_CANCEL_COMMAND.getLabel(),
                    "Click to cancel",
                    InternalCommandData.TPA_CANCEL_COMMAND.getLabel()));
            sendMessage(target, "&c{0} &6has requested that you teleport.".replaceAll("\\{0}", player.getDisplayName()));
            target.spigot().sendMessage(textLib.textRunnable(
                    "To accept this request, click &7[",
                    "&lhere",
                    "&7]&r or type &7/" + InternalCommandData.TP_ACCEPT_COMMAND.getLabel(),
                    "Accept",
                    InternalCommandData.TP_ACCEPT_COMMAND.getLabel()));
            target.spigot().sendMessage(textLib.textRunnable(
                    "To reject, click &7[",
                    "&lhere",
                    "&7]&r or type &7/" + InternalCommandData.TP_REJECT_COMMAND.getLabel(),
                    "Reject",
                    InternalCommandData.TP_REJECT_COMMAND.getLabel()));
        });
        return true;
    }
}
