package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.teleportation.TeleportRequest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TpaCancelCommand extends CommandBuilder {
    public TpaCancelCommand() {
        super(InternalCommandData.TPA_CANCEL_COMMAND);
    }

    @Override
    public List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }

    @Override
    public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(player)) return false;
        final Optional<TeleportRequest> any = api.getTeleportRunner().getActiveRequests().stream()
                .filter(r -> r.getPlayerRequesting().getUniqueId().equals(player.getUniqueId()))
                .findAny();
        if (any.isPresent()) {
            api.getTeleportRunner().cancelRequest(any.get());
            sendMessage(player, "Request cancelled.");
        } else {
            sendMessage(player, "You have no active requests.");
        }
        return true;
    }

    @Override
    public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
        return false;
    }
}
