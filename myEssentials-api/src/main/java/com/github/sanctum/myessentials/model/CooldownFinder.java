package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.library.Cooldown;
import org.jetbrains.annotations.Nullable;

public interface CooldownFinder extends CooldownFormatter {

	default @Nullable Cooldown timer(String id) {
		return Cooldown.getById(id);
	}

}
