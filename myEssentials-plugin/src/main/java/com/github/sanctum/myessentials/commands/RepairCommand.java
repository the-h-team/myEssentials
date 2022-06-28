package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RepairCommand extends CommandOutput {
	public RepairCommand() {
		super(OptionLoader.TEST_COMMAND.from("repair", "/repair", "Repair the item you currently hold.", "mess.staff.repair"));
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {

			if (args.length == 0) {
				ItemStack item = player.getInventory().getItemInMainHand();

				if (item.getType() != Material.AIR) {
					ItemMeta theMeta = item.getItemMeta();
					Damageable damageable = (Damageable) theMeta;
					if (!damageable.hasDamage()) {
						sendMessage(player, "&cItem already fully repaired.");
						return true;
					}
					damageable.setDamage(0);
					item.setItemMeta(theMeta);
					sendMessage(player, "&aHere you go, all repaired!");
				} else {
					sendMessage(player, "&cCannot repair air...");
				}
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
