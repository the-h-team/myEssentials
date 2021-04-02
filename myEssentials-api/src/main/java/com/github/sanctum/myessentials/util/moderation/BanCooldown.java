package com.github.sanctum.myessentials.util.moderation;

import com.github.sanctum.labyrinth.library.Cooldown;
import java.util.UUID;

public final class BanCooldown extends Cooldown {

	private final UUID user;
	private final long cooldown;

	protected BanCooldown(UUID user, long time) {
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
	public String fullTimeLeft() {
		return "&e" + getDaysLeft() + " &rDays &e" + getHoursLeft() + " &rHours &e" + getMinutesLeft() + " &rMinutes &e" + getSecondsLeft() + " &rSeconds";
	}

}
