package com.github.ms5984.malthadus.mess.commands;

import com.github.ms5984.malthadus.mess.data.PlayerData;
import com.github.ms5984.malthadus.mess.model.CommandData;
import com.github.ms5984.malthadus.mess.model.CommandsBase;
import com.github.ms5984.malthadus.mess.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetHomeCommand extends CommandsBase {
    public SetHomeCommand() {
        super(CommandData.SETHOME_COMMAND);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Messaging.MUST_BE_PLAYER.toString());
            return true;
        }
        if (!testPermission(sender)) {
            return true;
        }
        final Player player = (Player) sender;
        final PlayerData playerData = PlayerData.getPlayerData(player);
        switch (args.length) {
            case 0:
                playerData.saveHome("home", player.getLocation());
                sendMessage(player, Messaging.HOME_SAVED);
                return true;
            case 1:
                final String homeName = args[0];
                playerData.saveHome(homeName, player.getLocation());
                player.sendMessage(Messaging.HOME_NAME_SAVED.replace(homeName));
                return true;
        }
        return false;
    }
}
