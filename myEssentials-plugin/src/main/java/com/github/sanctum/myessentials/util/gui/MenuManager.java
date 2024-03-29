/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.util.gui;

import com.github.sanctum.labyrinth.gui.InventoryRows;
import com.github.sanctum.labyrinth.gui.menuman.Menu;
import com.github.sanctum.labyrinth.gui.menuman.MenuBuilder;
import com.github.sanctum.labyrinth.gui.menuman.PaginatedBuilder;
import com.github.sanctum.labyrinth.gui.menuman.PaginatedClickAction;
import com.github.sanctum.labyrinth.gui.menuman.PaginatedCloseAction;
import com.github.sanctum.labyrinth.gui.shared.SharedMenu;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.api.AddonQuery;
import com.github.sanctum.myessentials.api.EssentialsAddon;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public final class MenuManager {

	private static MenuManager instance;
	private final NamespacedKey addonKey = new NamespacedKey(Essentials.getInstance(), "essentials_addon");

	// utility class
	private static MenuManager getInstance() {
		if (instance != null) return instance;
		return (instance = new MenuManager());
	}

	public static NamespacedKey getAddonKey() {
		return getInstance().addonKey;
	}

	protected static List<String> color(String... text) {
		ArrayList<String> convert = new ArrayList<>();
		for (String t : text) {
			convert.add(StringUtils.use(t).translate());
		}
		return convert;
	}

	@SuppressWarnings("ConstantConditions")
	private static ItemStack setItemName(ItemStack item, String display) {
		final ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(color(display));
		item.setItemMeta(meta);
		return item;
	}

	private static ItemStack getLeft() {
		return setItemName(new ItemStack(Material.DARK_OAK_BUTTON), "&cPrevious page");
	}

	private static ItemStack getRight() {
		return setItemName(new ItemStack(Material.DARK_OAK_BUTTON), "&aNext page");
	}

	private static ItemStack getBack() {
		return setItemName(new ItemStack(Material.BARRIER), "&3Go back.");
	}

	private static String color(String text) {
		return StringUtils.use(text).translate();
	}

	/**
	 * A multi-paged GUI screen.
	 */
	public enum Select {
		REGISTERED_ADDONS, ACTIVATED_ADDONS, DEACTIVATED_ADDONS, ADDON_REGISTRATION, DONATION_BIN;

		public @NotNull Menu get() {
			MenuBuilder builder = null;
			if (this == Select.ADDON_REGISTRATION) {
				builder = new MenuBuilder(InventoryRows.ONE, color("&2&oManage Essential Addons &0&l»"))
						.cancelLowerInventoryClicks(false)
						.addElement(new ItemStack(Material.WATER_BUCKET))
						.setLore(color("&2&oTurn off active addons."))
						.setText(color("&7[&3&lActive&7]"))
						.setAction(click -> {
							Player p = click.getPlayer();
							Select.ACTIVATED_ADDONS.supply().open(p);
						})
						.assignToSlots(3)
						.addElement(new ItemStack(Material.BUCKET))
						.setLore(color("&a&oTurn on inactive addons."))
						.setText(color("&7[&c&lIn-active&7]"))
						.setAction(click -> {
							Player p = click.getPlayer();
							Select.DEACTIVATED_ADDONS.supply().open(p);
						})
						.assignToSlots(5)
						.addElement(new ItemStack(Material.LAVA_BUCKET))
						.setLore(color("&b&oView a list of all currently loaded addons."))
						.setText(color("&7[&e&lLoaded&7]"))
						.setAction(click -> {
							Player p = click.getPlayer();
							Select.REGISTERED_ADDONS.supply().open(p);
						})
						.assignToSlots(4)
						.setFiller(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE))
						.setText(" ")
						.set();
			}
			return builder.create(Essentials.getInstance());
		}

		public @NotNull Menu.Paginated<EssentialsAddon> supply() {
			PaginatedBuilder<EssentialsAddon> builder;
			switch (this) {
				case ACTIVATED_ADDONS:
					builder = new PaginatedBuilder<>(AddonQuery.getEnabledAddons().stream().map(AddonQuery::find).collect(Collectors.toList()))
							.forPlugin(Essentials.getInstance())
							.setTitle(color("&3&oRegistered Addons &f(&2ACTIVE&f) &8&l»"))
							.setAlreadyFirst(color("&c&oYou are already on the first page of addons."))
							.setAlreadyLast(color("&c&oYou are already on the last page of addons."))
							.setNavigationLeft(getLeft(), 48, PaginatedClickAction::sync)
							.setNavigationRight(getRight(), 50, PaginatedClickAction::sync)
							.setNavigationBack(getBack(), 49, click -> Select.ADDON_REGISTRATION.get().open(click.getPlayer()))
							.setSize(InventoryRows.SIX)
							.setCloseAction(PaginatedCloseAction::clear)
							.setupProcess(e -> {
								e.setItem(() -> {
									EssentialsAddon addon = e.getContext();
									ItemStack i = new ItemStack(Material.CHEST);

									ItemMeta meta = i.getItemMeta();

									meta.setLore(color("&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oPersistent: &f" + addon.persist(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oDescription: &f" + addon.getAddonDescription()));

									meta.getPersistentDataContainer().set(getInstance().addonKey, PersistentDataType.STRING, e.getContext().getAddonName());

									meta.setDisplayName(StringUtils.use("&3&o " + e.getContext() + " &8&l»").translate());

									i.setItemMeta(meta);

									return i;
								}).setClick(click -> {
									Player p = click.getPlayer();
									String addon = click.getClickedItem().getItemMeta().getPersistentDataContainer().get(MenuManager.getAddonKey(), PersistentDataType.STRING);
									assert addon != null;
									// disable addon logic
									EssentialsAddon ad = AddonQuery.find(addon);
									AddonQuery.unregisterAll(ad);
									for (String d : AddonQuery.getDataLog()) {
										p.sendMessage(color("&b" + d.replace("[Essentials]", "[&2Essentials&r]&e")));
									}
									Select.ACTIVATED_ADDONS.supply().open(p);
								});
							})
							.setupBorder()
							.setBorderType(Material.GRAY_STAINED_GLASS_PANE)
							.setFillType(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
							.build()
							.limit(28);
					break;
				case DEACTIVATED_ADDONS:
					builder = new PaginatedBuilder<>(AddonQuery.getDisabledAddons().stream().map(AddonQuery::find).collect(Collectors.toList()))
							.forPlugin(Essentials.getInstance())
							.setTitle(color("&3&oRegistered Addons &f(&eINACTIVE&f) &8&l»"))
							.setAlreadyFirst(color("&c&oYou are already on the first page of addons."))
							.setAlreadyLast(color("&c&oYou are already on the last page of addons."))
							.setNavigationLeft(getLeft(), 48, PaginatedClickAction::sync)
							.setNavigationRight(getRight(), 50, PaginatedClickAction::sync)
							.setNavigationBack(getBack(), 49, click -> Select.ADDON_REGISTRATION.get().open(click.getPlayer()))
							.setSize(InventoryRows.SIX)
							.setCloseAction(PaginatedCloseAction::clear)
							.setupProcess(e -> {
								e.setItem(() -> {
									EssentialsAddon addon = e.getContext();
									ItemStack i = new ItemStack(Material.CHEST);

									ItemMeta meta = i.getItemMeta();

									meta.setLore(color("&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oPersistent: &f" + addon.persist(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oDescription: &f" + addon.getAddonDescription()));

									meta.getPersistentDataContainer().set(getInstance().addonKey, PersistentDataType.STRING, e.getContext().getAddonName());

									meta.setDisplayName(StringUtils.use("&3&o " + e.getContext() + " &8&l»").translate());

									i.setItemMeta(meta);

									return i;
								}).setClick(click -> {
									Player p = click.getPlayer();
									String addon = click.getClickedItem().getItemMeta().getPersistentDataContainer().get(MenuManager.getAddonKey(), PersistentDataType.STRING);
									assert addon != null;
									// disable addon logic
									EssentialsAddon ad = AddonQuery.find(addon);
									AddonQuery.registerAll(ad);
									for (String d : AddonQuery.getDataLog()) {
										p.sendMessage(color("&b" + d.replace("[Essentials]", "[&2Essentials&r]&e")));
									}
									Select.DEACTIVATED_ADDONS.supply().open(p);
								});
							})
							.setupBorder()
							.setBorderType(Material.GRAY_STAINED_GLASS_PANE)
							.setFillType(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
							.build()
							.limit(28);
					break;
				case REGISTERED_ADDONS:
					builder = new PaginatedBuilder<>(AddonQuery.getRegisteredAddons().stream().map(AddonQuery::find).collect(Collectors.toList()))
							.forPlugin(Essentials.getInstance())
							.setTitle(color("&3&oRegistered Addons &f(&6&lCACHE&f) &8&l»"))
							.setAlreadyFirst(color("&c&oYou are already on the first page of addons."))
							.setAlreadyLast(color("&c&oYou are already on the last page of addons."))
							.setNavigationLeft(getLeft(), 48, PaginatedClickAction::sync)
							.setNavigationRight(getRight(), 50, PaginatedClickAction::sync)
							.setNavigationBack(getBack(), 49, click -> Select.ADDON_REGISTRATION.get().open(click.getPlayer()))
							.setSize(InventoryRows.SIX)
							.setCloseAction(PaginatedCloseAction::clear)
							.setupProcess(e -> {
								e.setItem(() -> {
									EssentialsAddon addon = e.getContext();
									ItemStack i = new ItemStack(Material.CHEST);

									ItemMeta meta = i.getItemMeta();

									meta.setLore(color("&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oPersistent: &f" + addon.persist(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oDescription: &f" + addon.getAddonDescription(), "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "&2&oActive: &6&o" + AddonQuery.getEnabledAddons().contains(addon.getAddonName())));

									meta.getPersistentDataContainer().set(getInstance().addonKey, PersistentDataType.STRING, e.getContext().getAddonName());

									meta.setDisplayName(StringUtils.use("&3&o " + e.getContext() + " &8&l»").translate());

									i.setItemMeta(meta);

									return i;
								}).setClick(click -> {
									Player p = click.getPlayer();
								});
							})
							.setupBorder()
							.setBorderType(Material.GRAY_STAINED_GLASS_PANE)
							.setFillType(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
							.build()
							.limit(28);
					break;

				default:
					throw new IllegalStateException("Unexpected menu type: " + this);
			}
			return builder.build();
		}

		public SharedMenu share() {

			if (this == Select.DONATION_BIN) {
				return SharedMenu.get("My-Bin");
			}

			throw new IllegalStateException("Invalid menu type present.");
		}
	}

}
