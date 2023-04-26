package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.gui.unity.construct.SingularMenu;
import com.github.sanctum.labyrinth.gui.unity.impl.MenuType;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.model.CommandInput;
import com.github.sanctum.myessentials.model.InternalCommandData;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InvseeCommand extends CommandInput {
	public InvseeCommand() {
		super(InternalCommandData.INVSEE_COMMAND);
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (args.length == 0) {
			sendUsage(player);
			return true;
		}

		if (args.length == 1) {

			if (Bukkit.getPlayer(args[0]) != null) {
				Player target = Bukkit.getPlayer(args[0]);

				if (player == target) {
					sendMessage(player, ConfiguredMessage.INVSEE_DENY_SELF);
					return true;
				}

				assert target != null;

				Menu m = MenuType.SINGULAR.build()
						.setHost(Essentials.getInstance())
						.setKey("MyInv-" + target.getName())
						.setStock(i -> i.setElement(target.getInventory()))
						.setProperty(Menu.Property.SHAREABLE, Menu.Property.CACHEABLE)
						.setTitle(target.getName() + "'s inventory.")
						.setSize(Menu.Rows.FIVE)
						.orGet(me -> me instanceof SingularMenu && me.getKey().map(("MyInv-" + target.getName())::equals).orElse(false));

				m.open(player);

			} else {
				sendMessage(player, ConfiguredMessage.PLAYER_NOT_FOUND);
			}
		}
		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
		return true;
	}
}
