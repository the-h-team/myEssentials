package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.library.Cooldown;

public interface CooldownFormatter {

	default Cooldown format(Cooldown c) {
		return c.format("&r(D&r)&e{DAYS} &r(H&r)&e{HOURS} &r(M&r)&e{MINUTES} &r(S&r)&e{SECONDS}");
	}

}
