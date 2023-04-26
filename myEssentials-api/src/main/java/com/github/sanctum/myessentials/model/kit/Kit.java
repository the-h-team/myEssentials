package com.github.sanctum.myessentials.model.kit;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.file.JsonAdapter;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.util.ParsedTimeFormat;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Node.Pointer(value = "com.github.sanctum.myessentials.Kit", type = DefaultKit.class)
public interface Kit extends Nameable, JsonAdapter<Kit> {

	@NotNull String getName();

	@Nullable ParsedTimeFormat getCooldown();

	@NotNull Kit setCooldown(@NotNull ParsedTimeFormat timeFormat);

	@Nullable ItemStack getHelmet();

	@Nullable ItemStack getChestplate();

	@Nullable ItemStack getLeggings();

	@Nullable ItemStack getBoots();

	@NotNull ItemStack[] getInventory();

	default boolean canUse(@NotNull String name) {
		Cooldown c = LabyrinthProvider.getService(Service.COOLDOWNS).getCooldown(getName() + "-" + name);
		if (c != null) {
			if (!c.isComplete()) return false;
			c.remove();
		}
		return true;
	}

	@Override
	default JsonElement write(Kit kit) {
		JsonObject object = new JsonObject();
		object.addProperty("name", kit.getName());
		if (kit.getCooldown() != null) {
			object.addProperty("cooldown", kit.getCooldown().getDays() + "d" + kit.getCooldown().getHours() + "hr" + kit.getCooldown().getMinutes() + "m" + kit.getCooldown().getSeconds() + "s");
		}
		JsonAdapter<ItemStack> adapter = Configurable.getAdapter(ItemStack.class);
		if (kit.getHelmet() != null) {
			object.add("helmet", adapter.write(kit.getHelmet()));
		}
		if (kit.getChestplate() != null) {
			object.add("chestplate", adapter.write(kit.getChestplate()));
		}
		if (kit.getLeggings() != null) {
			object.add("leggings", adapter.write(kit.getLeggings()));
		}
		if (kit.getBoots() != null) {
			object.add("boots", adapter.write(kit.getBoots()));
		}
		JsonArray array = new JsonArray();
		for (ItemStack i : kit.getInventory()) {
			array.add(adapter.write(i));
		}
		object.add("inventory", array);
		return object;
	}

	@Override
	default Kit read(Map<String, Object> map) {

		JsonAdapter<ItemStack> adapter = Configurable.getAdapter(ItemStack.class);

		String cooldown = null;
		String name = map.get("name").toString();

		ItemStack helmet = null;
		ItemStack chestplate = null;
		ItemStack leggings = null;
		ItemStack boots = null;

		if (map.containsKey("helmet")) {
			helmet = adapter.read((Map<String, Object>) map.get("helmet"));
		}

		if (map.containsKey("chestplate")) {
			chestplate = adapter.read((Map<String, Object>) map.get("chestplate"));
		}

		if (map.containsKey("leggings")) {
			leggings = adapter.read((Map<String, Object>) map.get("leggings"));
		}

		if (map.containsKey("boots")) {
			boots = adapter.read((Map<String, Object>) map.get("boots"));
		}

		if (map.containsKey("cooldown")) {
			cooldown = map.get("cooldown").toString();
		}

		List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("inventory");
		List<ItemStack> items = new ArrayList<>();

		list.forEach(m -> items.add(adapter.read(m)));

		String finalCooldown = cooldown;
		ItemStack finalHelmet = helmet;
		ItemStack finalChestplate = chestplate;
		ItemStack finalLeggings = leggings;
		ItemStack finalBoots = boots;
		return new DefaultKit(new Kit() {
			ParsedTimeFormat format;

			{
				try {
					if (finalCooldown != null) {
						this.format = KitTimeStamp.of(finalCooldown);
					}
				} catch (Exception ignored) {
				}
			}

			@Override
			public @NotNull String getName() {
				return name;
			}

			@Override
			public @Nullable ParsedTimeFormat getCooldown() {
				return format;
			}

			@Override
			public @NotNull Kit setCooldown(@NotNull ParsedTimeFormat timeFormat) {
				this.format = timeFormat;
				return this;
			}

			@Override
			public @Nullable ItemStack getHelmet() {
				return finalHelmet;
			}

			@Override
			public @Nullable ItemStack getChestplate() {
				return finalChestplate;
			}

			@Override
			public @Nullable ItemStack getLeggings() {
				return finalLeggings;
			}

			@Override
			public @Nullable ItemStack getBoots() {
				return finalBoots;
			}

			@Override
			public @NotNull ItemStack[] getInventory() {
				return items.toArray(new ItemStack[0]);
			}
		});
	}

	@Override
	default Class<? extends Kit> getSerializationSignature() {
		return Kit.class;
	}

	static @NotNull Kit newSnapshot(@NotNull Player player, @NotNull String name, @Nullable ParsedTimeFormat time) {
		return new DefaultKit(new Kit() {
			final ItemStack helmet = player.getInventory().getHelmet();
			final ItemStack chest = player.getInventory().getChestplate();
			final ItemStack boots = player.getInventory().getBoots();
			final ItemStack legg = player.getInventory().getLeggings();
			ParsedTimeFormat timeFormat;
			final ItemStack[] stack;

			{
				ItemStack[] actual = player.getInventory().getContents();
				int length = actual.length;
				stack = new ItemStack[length];
				for (int i = 0; i < length; i++) {
					ItemStack match = actual[i];
					if (match != null) {
						stack[i] = new ItemStack(match);
					} else {
						stack[i] = new ItemStack(Material.AIR);
					}
				}
				this.timeFormat = time;
			}

			@Override
			public @NotNull String getName() {
				return name;
			}

			@Override
			public @Nullable ParsedTimeFormat getCooldown() {
				return timeFormat;
			}

			@Override
			public @NotNull Kit setCooldown(@NotNull ParsedTimeFormat timeFormat) {
				this.timeFormat = timeFormat;
				return this;
			}

			@Override
			public @Nullable ItemStack getHelmet() {
				return helmet;
			}

			@Override
			public @Nullable ItemStack getChestplate() {
				return chest;
			}

			@Override
			public @Nullable ItemStack getLeggings() {
				return legg;
			}

			@Override
			public @Nullable ItemStack getBoots() {
				return boots;
			}

			@Override
			public @NotNull ItemStack[] getInventory() {
				return stack;
			}
		});
	}

	interface Holder extends Nameable {

		@Override
		@NotNull String getName();

		@NotNull UUID getId();

		@Nullable Kit getCurrent();

		boolean apply(@NotNull Kit kit);

	}
}
