package com.github.sanctum.myessentials.commands;

import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.gui.unity.construct.PaginatedMenu;
import com.github.sanctum.labyrinth.gui.unity.construct.PrintableMenu;
import com.github.sanctum.labyrinth.gui.unity.impl.ItemElement;
import com.github.sanctum.labyrinth.gui.unity.impl.ListElement;
import com.github.sanctum.labyrinth.gui.unity.impl.MenuType;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.util.OptionLoader;
import com.github.sanctum.skulls.CustomHead;
import com.github.sanctum.skulls.SkullType;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HeadsCommand extends CommandBuilder {
	public HeadsCommand() {
		super(OptionLoader.TEST_COMMAND.from("heads", "/heads", "Retrieve the entire list of cached skull items", "mess.staff.heads"));
	}

	private static PaginatedMenu menu(Player p) {
		return MenuType.PAGINATED.build()
				.setTitle("&0&l[&2Head Database&0&l] {0}/{1}")
				.setSize(Menu.Rows.ONE)
				.setHost(Essentials.getInstance()).setKey("HEAD-" + p.getUniqueId())
				.setProperty(Menu.Property.CACHEABLE, Menu.Property.RECURSIVE)
				.setStock(i -> {

					Material mat = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
					i.addItem(it -> it.setElement(el -> el.setType(mat).setTitle(" ").build()).setClick(c -> c.setCancelled(true)).setSlot(1));
					i.addItem(it -> it.setElement(el -> el.setType(mat).setTitle(" ").build()).setClick(c -> c.setCancelled(true)).setSlot(6));
					i.addItem(it -> it.setElement(el -> el.setItem(SkullType.COMMAND_BLOCK.get()).setTitle("&2Select page.").build()).setClick(c -> {
						c.setCancelled(true);
						Player pl = c.getElement();
						PrintableMenu m = MenuType.PRINTABLE.build()
								.setTitle(StringUtils.use("&5Enter a page #").translate())
								.setSize(Menu.Rows.ONE)
								.setHost(Essentials.getInstance()).setKey("HEAD-SEARCH-" + pl.getUniqueId())
								.setStock(inv -> inv.addItem(b -> b.setElement(item -> item.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle(i.getViewer(p).getPage().toNumber() + "").build()).setSlot(0).setClick(click -> {
									click.setCancelled(true);
									click.setHotbarAllowed(false);
								}))).join()
								.addAction(click -> {
									click.setCancelled(true);
									click.setHotbarAllowed(false);
									if (click.getSlot() == 2) {
										try {
											PaginatedMenu menu = menu(p);
											menu.getInventory().getViewer(p).setPage(Integer.parseInt(click.getParent().getName()));
											menu.open(p);
										} catch (NumberFormatException ignored) {
										}
									}
								});

						m.open(p);
					}).setSlot(7));

					i.addItem(new ListElement<>(CustomHead.Manager.getHeads()).setLimit(4).setPopulate((value, element) -> {
								element.setElement(value.get());
								element.setElement(edit -> edit.setTitle(value.name()).build());
								element.setClick(c -> {
									c.setCancelled(true);
									c.setHotbarAllowed(false);
								});
							})).addItem(b -> b.setElement(it -> it.setItem(SkullType.ARROW_BLUE_RIGHT.get()).setTitle("&5Next").build()).setType(ItemElement.ControlType.BUTTON_NEXT).setSlot(8))
							.addItem(b -> b.setElement(it -> it.setItem(SkullType.ARROW_BLUE_LEFT.get()).setTitle("&5Previous").build()).setType(ItemElement.ControlType.BUTTON_BACK).setSlot(0));

				})
				.orGet(me -> me instanceof PaginatedMenu && me.getKey().isPresent() && me.getKey().get().equals("HEAD-" + p.getUniqueId()));
	}

	@Override
	public @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return defaultCompletion(player, alias, args);
	}

	@Override
	public boolean playerView(@NotNull Player p, @NotNull String commandLabel, @NotNull String[] args) {
		if (testPermission(p)) {
			menu(p).open(p);
		}
		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		return false;
	}
}
