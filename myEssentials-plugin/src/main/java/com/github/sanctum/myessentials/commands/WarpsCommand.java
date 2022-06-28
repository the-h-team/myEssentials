package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.gui.unity.impl.BorderElement;
import com.github.sanctum.labyrinth.gui.unity.impl.FillerElement;
import com.github.sanctum.labyrinth.gui.unity.impl.ItemElement;
import com.github.sanctum.labyrinth.gui.unity.impl.ListElement;
import com.github.sanctum.labyrinth.gui.unity.impl.MenuType;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.warp.Warp;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.OptionLoader;
import com.github.sanctum.skulls.SkullType;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WarpsCommand extends CommandOutput {
	public WarpsCommand() {
		super(OptionLoader.TEST_COMMAND.from("warps", "/warps", "View all server warps.", "mess.warp"));
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		MenuType.PAGINATED.build()
				.setTitle("Warps")
				.setSize(Menu.Rows.THREE)
				.setProperty(Menu.Property.LIVE_META)
				.setHost(Essentials.getInstance())
				.setStock(i -> {
					ListElement<Warp> warps = new ListElement<>(() -> MyEssentialsAPI.getInstance().getWarps().stream().collect(Collectors.toList()));
					warps.setLimit(7);
					warps.setPopulate((value, element) -> {
						element.setElement(ed -> ed.setTitle("&e" + value.getName()).setLore("&fClick to teleport.").setType(Material.END_CRYSTAL).build());
						element.setTypeAndAddAction(ItemElement.ControlType.DISPLAY, click -> {
							click.setCancelled(true);
							click.setHotbarAllowed(false);
							click.getElement().performCommand("warp " + value.getName());
						});
					});
					BorderElement<?> element = new BorderElement<>(i);
					for (Menu.Panel p : Menu.Panel.values()) {
						if (p != Menu.Panel.MIDDLE) {
							element.add(p, itemElement -> itemElement.setElement(ed -> ed.setTitle(" ").setType(Material.GLASS_PANE).build()));
						}
					}
					FillerElement<?> element2 = new FillerElement<>(i);
					element2.add(it -> it.setElement(ed -> ed.setTitle(" ").setType(Material.STONE).build()));
					i.addItem(b -> b.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle("&5Next").build()).setType(ItemElement.ControlType.BUTTON_NEXT).setSlot(25))
							.addItem(b -> b.setElement(it -> it.setItem(SkullType.ARROW_BLUE_LEFT.get()).setTitle("&5Previous").build()).setType(ItemElement.ControlType.BUTTON_BACK).setSlot(19));
					i.addItem(element);
					i.addItem(element2);
					i.addItem(warps);
				})
				.join()
				.open(player);
		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		sendMessage(sender, ConfiguredMessage.MUST_BE_PLAYER);
		return true;
	}
}
