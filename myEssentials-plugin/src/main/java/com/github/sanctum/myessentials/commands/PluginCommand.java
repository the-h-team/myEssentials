package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.library.ListUtils;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.util.OptionLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PluginCommand extends CommandOutput {
	public PluginCommand() {
		super(OptionLoader.TEST_COMMAND.from("pl", "/pl", "View plugin information.", "mess.plugins", "?"));
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(player)) {
			LinkedList<BaseComponent> list = new LinkedList<>();
			Mailer msg = Mailer.empty(player);
			TextLib.consume(it -> {
				for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
					list.add(it.execute(() -> {
						msg.chat("&a" + p.getName() + " &fInformation:").deploy();
						msg.chat("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").deploy();
						msg.chat("Authors: &b" + p.getDescription().getAuthors()).deploy();
						msg.chat("Website: &b" + (p.getDescription().getWebsite() != null ? p.getDescription().getWebsite() : "None")).deploy();
						msg.chat("Description: &3" + Optional.ofNullable(p.getDescription().getDescription()).orElse("None")).deploy();
						msg.chat("Dependencies: &b" + p.getDescription().getDepend()).deploy();
						msg.chat("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").deploy();

					}, it.textHoverable("", (p.isEnabled() ? "&a" + p.getName() : "&c" + p.getName()), "&bClick to view plugin info for &f" + p.getName())));
				}
			});
			LinkedList<BaseComponent> newList = new LinkedList<>(ListUtils.use(list).append(component -> {
				component.addExtra(StringUtils.use("&r, ").translate());
			}));

			newList.addFirst(TextLib.getInstance().textHoverable("Plugins &7(&a" + list.size() + "&7)&f: ", "", ""));

			msg.chat(newList.toArray(new BaseComponent[0])).deploy();

		}
		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}
}
