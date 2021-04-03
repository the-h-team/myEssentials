package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.library.Cooldown;
import org.jetbrains.annotations.Nullable;

public interface CooldownFinder {

	default @Nullable Cooldown timer(String id) {
		return Cooldown.getById(id);
	}

	default Cooldown factory(Cooldown c) {
		return c.format("&r(D&r)&e{DAYS} &r(H&r)&e{HOURS} &r(M&r)&e{MINUTES} &r(S&r)&e{SECONDS}");
	}

}
