package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandInput;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.teleportation.TeleportRequest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TPAcceptCommand extends CommandInput {
    public TPAcceptCommand() {
        super(InternalCommandData.TP_ACCEPT_COMMAND);
    }

    @Override
    public List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }

    @Override
    public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(player)) return false;
        final Optional<TeleportRequest> any = api.getTeleportRunner().getActiveRequests().stream()
                .filter(r -> r.getPlayerTeleporting().getUniqueId().equals(player.getUniqueId()) || r.getPlayerRequested().map(p -> p.getUniqueId().equals(player.getUniqueId())).orElse(false))
                .findAny();
        if (any.isPresent()) {
            api.getTeleportRunner().acceptTeleport(any.get());
            sendMessage(player, "Teleport request accepted.");
        } else {
            sendMessage(player, "There are no teleport requests to accept at this time.");
        }
        return true;
    }

    @Override
    public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
        return false;
    }
}
