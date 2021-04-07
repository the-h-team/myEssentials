package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class TPRejectCommand extends CommandBuilder {
    public TPRejectCommand() {
        super(InternalCommandData.TP_REJECT_COMMAND);
    }

    @Override
    public List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }

    @Override
    public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(player)) return false;
        return true;
    }

    @Override
    public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
        return false;
    }
}
