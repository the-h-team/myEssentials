package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.IllegalTimeFormatException;
import com.github.sanctum.labyrinth.library.ParsedTimeFormat;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.kit.Kit;
import com.github.sanctum.myessentials.model.kit.KitTimeStamp;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitCommand extends CommandOutput {
	public KitCommand() {
		super(OptionLoader.TEST_COMMAND.from("kit", "/kit", "Give yourself a kit.", "mess.kit"));
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {
			sendMessage(player, "&cUsage:");
			sendMessage(player, "/kit &7<kitName>");
		}

		if (args.length == 1) {

			Kit kit = MyEssentialsAPI.getInstance().getKit(args[0]);
			if (kit != null) {
				if (!getPermCheck().has(player, kit.getName())) {
					sendMessage(player, "&cYou don't have permission to " + getData().getPermissionNode() + "." + kit.getName());
					return true;
				}
				ParsedTimeFormat time = kit.getCooldown();
				Kit.Holder holder = MyEssentialsAPI.getInstance().getKitHolder(player);
				if (time != null) {
					Cooldown c = LabyrinthProvider.getService(Service.COOLDOWNS).getCooldown(kit.getName() + "-" + player.getName());
					if (c != null) {
						if (c.isComplete()) {
							c.remove();
							if (holder.apply(kit)) {
								sendMessage(player, "&aYou've been given kit " + kit.getName());
							}
						} else {
							sendMessage(player, "&cYou can't do this for another &r" + c.toFormat().replace("-", ""));
						}
						return true;
					}
				}
				if (holder.apply(kit)) {
					sendMessage(player, "&aYou've been given kit " + kit.getName());
					if (time != null) {
						Cooldown c = LabyrinthProvider.getService(Service.COOLDOWNS).getCooldown(kit.getName() + "-" + player.getName());
						assert c != null;
						sendMessage(player, "&3You can use this kit again in " + c.toFormat().replace("-", ""));
					}
				}
			} else {
				sendMessage(player, "&Kit not found.");
			}
		}

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("create")) {
				if (!getPermCheck().has(player, "create")) {
					sendMessage(player, "&cYou don't have permission to create kits!");
					return true;
				}
				Kit now = Kit.newSnapshot(player, args[1], null);
				MyEssentialsAPI.getInstance().loadKit(now);
				sendMessage(player, "&aKit " + args[1] + " saved.");
			}
			return true;
		}

		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("create")) {
				if (!getPermCheck().has(player, "create")) {
					sendMessage(player, "&cYou don't have permission to create kits!");
					return true;
				}
				try {
					Kit now = Kit.newSnapshot(player, args[1], KitTimeStamp.of(args[2]));
					MyEssentialsAPI.getInstance().loadKit(now);
					sendMessage(player, "&aKit " + args[1] + " saved with " + args[2] + " cooldown.");
				} catch (IllegalTimeFormatException e) {
					sendMessage(player, "&cUnable to parse time format.");
				}
			}
			return true;
		}


		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
		return true;
	}
}
