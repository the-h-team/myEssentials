package com.github.sanctum.myessentials.util;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.annotation.Note;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.gui.unity.impl.MenuType;
import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;

public interface SignFunction {

	Block getBlock();

	String getLine(SignEdit.Line line);

	boolean isFor(Type type);

	void initialize(Player creator);

	@Note("Requires no permission to build.")
	void build(Type type);

	void run(Player player);

	enum Type {
		BALANCE("[balance]"),
		BUY("[buy]"),
		DISPOSAL("[disposal]"),
		DURABILITY("[durability]"),
		FEED("[feed]"),
		FREE("[free]"),
		HEAL("[heal]"),
		KIT("[kit]"),
		REPAIR("[repair]"),
		SELL("[sell]"),
		WARP("[warp]");

		String tag;

		Type(String tag) {
			this.tag = tag;
		}

		public String toTag() {
			return tag;
		}

		public String getNode() {
			switch (this) {
				case BALANCE:
					return "mess.sign.balance";
				case BUY:
					return "mess.sign.buy";
				case DISPOSAL:
					return "mess.sign.disposal";
				case DURABILITY:
					return "mess.sign.durability";
				case FEED:
					return "mess.sign.feed";
				case FREE:
					return "mess.sign.free";
				case HEAL:
					return "mess.sign.heal";
				case KIT:
					return "mess.sign.kit";
				case REPAIR:
					return "mess.sign.repair";
				case SELL:
					return "mess.sign.sell";
				case WARP:
					return "mess.sign.warp";
				default:
					return null;
			}
		}

		public void build(@NotNull Sign sign) {
			ofDefault(sign.getBlock()).build(this);
		}

	}

	interface Adapter {

		void initialize(SignEdit edit, Player creator);

		void run(SignEdit edit, Player player);

	}

	static void newAdapter(@NotNull SignFunction.Adapter function) {
		SignFunctionLibrary.ADAPTERS.add(function);
	}

	static SignFunction ofLibrary(@NotNull Block block) {
		return new SignFunction() {

			final SignEdit edit = new SignEdit(block);

			@Override
			public Block getBlock() {
				return block;
			}

			@Override
			public String getLine(SignEdit.Line line) {
				return edit.getLine(line).orElse(null);
			}

			@Override
			public boolean isFor(Type type) {
				return StringUtils.use(ChatColor.stripColor(getLine(SignEdit.Line.ONE))).containsIgnoreCase(type.toTag());
			}

			@Override
			public void initialize(Player creator) {
				SignFunctionLibrary.ADAPTERS.forEach(a -> a.initialize(edit, creator));
			}

			@Override
			public void build(Type type) {

			}

			@Override
			public void run(Player player) {
				SignFunctionLibrary.ADAPTERS.forEach(a -> a.run(edit, player));
			}
		};
	}

	static SignFunction ofDefault(@NotNull Block block) {
		return new SignFunction() {

			final SignEdit edit = new SignEdit(block);

			Type type;


			Type getType() {
				if (type == null) {
					for (Type t : Type.values()) {
						if (isFor(t)) return (this.type = t);
					}
				}
				return type;
			}

			@Override
			public Block getBlock() {
				return block;
			}

			@Override
			public String getLine(SignEdit.Line line) {
				return edit.getLine(line).orElse(null);
			}

			@Override
			public boolean isFor(Type type) {
				return getLine(SignEdit.Line.ONE) != null && StringUtils.use(ChatColor.stripColor(getLine(SignEdit.Line.ONE))).containsIgnoreCase(type.toTag());
			}

			@Override
			public void initialize(Player creator) {
				Type type = getType();

				if (type != null && type.getNode() != null) {
					if (!creator.hasPermission(type.getNode() + ".create")) {
						Mailer.empty(creator).chat("&cYou don't have permission " + '"' + type.getNode() + ".create" + '"').queue();
						return;
					}
					switch (type) {
						case BALANCE:
							edit.setLine(SignEdit.Line.ONE, "&6[&2Balance&6]");
							break;
						case BUY:
							if (getLine(SignEdit.Line.TWO) != null && getLine(SignEdit.Line.THREE) != null) {
								if (StringUtils.use(getLine(SignEdit.Line.TWO)).isInt()) {
									edit.setLine(SignEdit.Line.ONE, "&6[&2Buy&6]");
								} else {
									edit.setLine(SignEdit.Line.ONE, "&e[&cBuy&e]");
								}
							} else {
								edit.setLine(SignEdit.Line.ONE, "&e[&cBuy&e]");
							}
							break;
						case DISPOSAL:
							edit.setLine(SignEdit.Line.ONE, "&6[&2Disposal&6]");
							break;
						case DURABILITY:
							edit.setLine(SignEdit.Line.ONE, "&6[&2Durability&6]");
							break;
						case FEED:
							edit.setLine(SignEdit.Line.ONE, "&6[&2Feed&6]");
							break;
						case FREE:
							edit.setLine(SignEdit.Line.ONE, "&6[&2Free&6]");
							break;
						case HEAL:
							edit.setLine(SignEdit.Line.ONE, "&6[&2Heal&6]");
							break;
						case KIT:
							edit.setLine(SignEdit.Line.ONE, "&6[&2Kit&6]");
							break;
						case REPAIR:
							edit.setLine(SignEdit.Line.ONE, "&6[&2Repair&6]");
							break;
						case SELL:
							if (getLine(SignEdit.Line.TWO) != null && getLine(SignEdit.Line.THREE) != null) {
								if (StringUtils.use(getLine(SignEdit.Line.TWO)).isInt()) {
									edit.setLine(SignEdit.Line.ONE, "&6[&2Sell&6]");
								} else {
									edit.setLine(SignEdit.Line.ONE, "&e[&cSell&e]");
								}
							} else {
								edit.setLine(SignEdit.Line.ONE, "&e[&cSell&e]");
							}
							break;
						case WARP:
							edit.setLine(SignEdit.Line.ONE, "&6[&2Warp&6]");
							break;
					}
				}
				if (creator.hasPermission("mess.sign.color")) {
					edit.colorAll();
				}
			}

			@Override
			public void build(Type type) {
				switch (type) {
					case REPAIR:
						edit.setLine(SignEdit.Line.ONE, "&6[&2Repair&6]");
						break;
					case FREE:
						edit.setLine(SignEdit.Line.ONE, "&6[&2Free&6]");
						break;
					case BALANCE:
						edit.setLine(SignEdit.Line.ONE, "&6[&2Balance&6]");
						break;
					case FEED:
						edit.setLine(SignEdit.Line.ONE, "&6[&2Feed&6]");
						break;
					case HEAL:
						edit.setLine(SignEdit.Line.ONE, "&6[&2Heal&6]");
						break;
					case WARP:
						edit.setLine(SignEdit.Line.ONE, "&6[&2Warp&6]");
						break;
					case KIT:
						edit.setLine(SignEdit.Line.ONE, "&6[&2Kit&6]");
						break;
					case DISPOSAL:
						edit.setLine(SignEdit.Line.ONE, "&6[&2Disposal&6]");
					case DURABILITY:
						edit.setLine(SignEdit.Line.ONE, "&6[&2Durability&6]");
						edit.setLine(SignEdit.Line.TWO, "&a10");
						break;
					case BUY:
						edit.setLine(SignEdit.Line.ONE, "&6[&2Buy&6]");
						edit.setLine(SignEdit.Line.TWO, "32");
						edit.setLine(SignEdit.Line.THREE, "dirt");
					case SELL:
						edit.setLine(SignEdit.Line.ONE, "&6[&2Sell&6]");
						edit.setLine(SignEdit.Line.TWO, "32");
						edit.setLine(SignEdit.Line.THREE, "dirt");
				}
			}

			@Override
			public void run(Player player) {

				Type type = getType();

				if (type != null && type.getNode() != null) {
					if (!player.hasPermission(type.getNode())) {
						Mailer.empty(player).chat("&cYou don't have permission " + '"' + type.getNode() + '"').queue();
						return;
					}
					String rawAmount = ChatColor.stripColor((getLine(SignEdit.Line.TWO) != null ? getLine(SignEdit.Line.TWO) : "0").replace("+", ""));
					switch (type) {

						case BALANCE:
							player.performCommand("balance");
							break;
						case BUY:
							if (StringUtils.use(rawAmount).isInt()) {
								int i = Integer.parseInt(rawAmount);
								player.performCommand("buy " + i + " " + getLine(SignEdit.Line.THREE));
							} else {
								edit.setLine(SignEdit.Line.TWO, "&4" + rawAmount);
							}
							break;
						case DISPOSAL:
							MenuType.SINGULAR.build()
									.setHost(MyEssentialsAPI.getInstance().getFileList().getPlugin())
									.setSize(Menu.Rows.SIX)
									.setProperty(Menu.Property.RECURSIVE)
									.setTitle("&6&lDisposal")
									.setStock(i -> {
										i.addItem(it -> {
											it.setElement(ed -> ed.setType(Material.LAVA_BUCKET).setTitle("&4Destroy").build());
											it.setClick(click -> {
												click.setCancelled(true);
												click.getParent().getParent().open(click.getElement());
											});
											it.setSlot(53);
										});
									})
									.join()
									.open(player);
							break;
						case DURABILITY:

							if (StringUtils.use(rawAmount).isInt()) {
								int i = Integer.parseInt(rawAmount);
								player.performCommand("durability " + (rawAmount.contains("-") ? Math.abs(i) : -i) + " -s");
							} else {
								edit.setLine(SignEdit.Line.TWO, "&4" + rawAmount);
							}
							break;
						case FEED:
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "feed " + player.getName());
							break;
						case FREE:
							MenuType.SINGULAR.build()
									.setHost(MyEssentialsAPI.getInstance().getFileList().getPlugin())
									.setSize(Menu.Rows.THREE)
									.setProperty(Menu.Property.CACHEABLE, Menu.Property.SHAREABLE)
									.setTitle("&b&lFree")
									.setKey("Mess;Freebie:" + block.getLocation())
									.setStock(i -> {
										i.addItem(it -> {
											it.setElement(ed -> ed.setType(Material.LAVA_BUCKET).setTitle("&eTake All").build());
											it.setClick(click -> {
												click.setCancelled(true);
												Inventory inv = click.getParent().getParent().getElement();
												ItemStack item = Items.edit(ed -> ed.setType(Material.CYAN_SHULKER_BOX).build());
												if (item.getItemMeta() instanceof BlockStateMeta) {
													BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
													if (im.getBlockState() instanceof ShulkerBox) {
														ShulkerBox shulker = (ShulkerBox) im.getBlockState();
														Inventory sh = shulker.getInventory();
														int amount = 0;
														for (ItemStack itemStack : inv) {
															if (itemStack != null && !itemStack.equals(click.getParent().getElement())) {
																sh.addItem(itemStack);
																amount++;
															}
														}
														im.setDisplayName(StringUtils.use("&eFreebie &fx" + amount).translate());
														im.setBlockState(shulker);
														item.setItemMeta(im);
													}
												}
												LabyrinthProvider.getInstance().getItemComposter().add(item, click.getElement());
												inv.clear();
												click.getParent().getParent().open(click.getElement());
											});
											it.setSlot(26);
										});
									})
									.orGet(m -> m.getKey().map(("Mess;Freebie:" + block.getLocation())::equals).orElse(false))
									.open(player);
							break;
						case HEAL:
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "heal " + player.getName());
							break;
						case KIT:
							player.performCommand("kit " + getLine(SignEdit.Line.TWO));
							break;
						case REPAIR:
							player.performCommand("repair");
							break;
						case SELL:
							if (StringUtils.use(rawAmount).isInt()) {
								int i = Integer.parseInt(rawAmount);
								player.performCommand("sell " + i + " " + getLine(SignEdit.Line.THREE));
							} else {
								edit.setLine(SignEdit.Line.TWO, "&4" + rawAmount);
							}
							break;
						case WARP:
							player.performCommand("warp " + getLine(SignEdit.Line.TWO));
							break;
					}
				}


			}
		};
	}

}
