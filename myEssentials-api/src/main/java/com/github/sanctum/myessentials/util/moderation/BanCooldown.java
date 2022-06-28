package com.github.sanctum.myessentials.util.moderation;

import com.github.sanctum.labyrinth.library.Cooldown;
import java.util.UUID;

public final class BanCooldown extends Cooldown {

	private final UUID user;
	private final long cooldown;

	BanCooldown(UUID user, long time) {
		this.user = user;
		this.cooldown = abv((int) time);
	}

	@Override
	public String getId() {
		return "MyBan-id-" + user.toString();
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String toFormat() {
		return "&e" + getDays() + " &rDays &e" + getHours() + " &rHours &e" + getMinutes() + " &rMinutes &e" + getSeconds() + " &rSeconds";
	}

}
