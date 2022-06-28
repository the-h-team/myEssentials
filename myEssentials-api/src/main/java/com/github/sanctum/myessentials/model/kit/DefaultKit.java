package com.github.sanctum.myessentials.model.kit;

import com.github.sanctum.labyrinth.annotation.Note;
import com.github.sanctum.labyrinth.library.ParsedTimeFormat;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Note("This implementation is serialization safe via JsonAdapter.")
public class DefaultKit implements Kit {

	final Kit parent;

	@Deprecated
	public DefaultKit() {
		this.parent = null;
	}

	public DefaultKit(@NotNull Kit kit) {
		this.parent = kit;
	}

	@Override
	public @NotNull String getName() {
		return parent != null ? parent.getName() : "N?A";
	}

	@Override
	public @Nullable ParsedTimeFormat getCooldown() {
		return parent != null ? parent.getCooldown() : null;
	}

	@Override
	public @NotNull DefaultKit setCooldown(@NotNull ParsedTimeFormat timeFormat) {
		if (parent != null) parent.setCooldown(timeFormat);
		return this;
	}

	@Override
	public @Nullable ItemStack getHelmet() {
		return parent != null ? parent.getHelmet() : null;
	}

	@Override
	public @Nullable ItemStack getChestplate() {
		return parent != null ? parent.getChestplate() : null;
	}

	@Override
	public @Nullable ItemStack getLeggings() {
		return parent != null ? parent.getLeggings() : null;
	}

	@Override
	public @Nullable ItemStack getBoots() {
		return parent != null ? parent.getBoots() : null;
	}

	@Override
	public @NotNull ItemStack[] getInventory() {
		return parent != null ? parent.getInventory() : new ItemStack[27];
	}

}
