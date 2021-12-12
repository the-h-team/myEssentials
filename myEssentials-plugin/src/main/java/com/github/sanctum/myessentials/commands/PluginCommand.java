package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.library.ListUtils;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.util.OptionLoader;
import java.util.LinkedList;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PluginCommand extends CommandBuilder {
	public PluginCommand() {
		super(OptionLoader.TEST_COMMAND.from("pl", "/pl", "View plugin information.", "mess.plugins", "?"));
	}

	@Override
	public @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {
			LinkedList<BaseComponent> list = new LinkedList<>();
			Message msg = Message.form(player);
			TextLib.consume(it -> {
				for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
					list.add(it.execute(() -> {


						sendMessage(player, "&a" + p.getName() + " &fInformation:");
						msg.send("Authors: &b" + p.getDescription().getAuthors());
						msg.send("Website: &b" + (p.getDescription().getWebsite() != null ? p.getDescription().getWebsite() : "None"));
						msg.send("Description: &3" + p.getDescription().getDescription());
						msg.send("Dependencies: &b" + p.getDescription().getDepend());

					}, it.textHoverable("", (p.isEnabled() ? "&a" + p.getName() : "&c" + p.getName()), "&bClick to view plugin info for &f" + p.getName())));
				}
			});
			LinkedList<BaseComponent> newList = new LinkedList<>(ListUtils.use(list).append(component -> {
				component.addExtra(StringUtils.use("&r, ").translate());
			}));

			newList.addFirst(TextLib.getInstance().textHoverable("Plugins &7(&a" + list.size() + "&7)&f: ", "", ""));

			msg.build(newList.toArray(new BaseComponent[0]));

		}
		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}
}
