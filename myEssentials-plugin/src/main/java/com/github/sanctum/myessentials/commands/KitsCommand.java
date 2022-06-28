package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.formatting.pagination.EasyPagination;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.PermissionCheck;
import com.github.sanctum.myessentials.model.kit.Kit;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitsCommand extends CommandOutput {
	public KitsCommand() {
		super(OptionLoader.TEST_COMMAND.from("kits", "/kits", "View server kits.", "mess.kits"));
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {
			Kit[] kits = MyEssentialsAPI.getInstance().getKits().stream().filter(k -> PermissionCheck.of("mess.kit").has(player, k.getName())).toArray(Kit[]::new);
			if (kits.length == 0) {
				sendMessage(player, "&cThere are currently no kits available.");
				return true;
			}
			EasyPagination<Kit> pagination = new EasyPagination<>(player, kits);
			pagination.limit(5);
			pagination.setHeader((p, message) -> {
				message.then("Select a kit.");
				message.then("\n");
				message.then("&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			});
			pagination.setFormat((kit, integer, message) -> message.then((kit.canUse(player.getName()) ? "&a" : "&c&m") + kit.getName()).hover(kit.canUse(player.getName()) ? "&eClick to equip" : "&cYou can't use me right now.").action(() -> player.performCommand("kit " + kit.getName())));
			pagination.setFooter((p, message) -> message.then("&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
			pagination.send(1);
		}


		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
		return true;
	}
}
