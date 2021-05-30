package com.github.sanctum.myessentials.listeners;

import com.github.sanctum.labyrinth.data.EconomyProvision;
import java.math.BigDecimal;
import org.bukkit.OfflinePlayer;

public class PlayerWrapperObject {

	private final OfflinePlayer player;
	private final EconomyProvision provision;

	public PlayerWrapperObject(OfflinePlayer op) {
		this.player = op;
		this.provision = EconomyProvision.getInstance();
	}

	public boolean has(double amount) {
		return provision.has(BigDecimal.valueOf(amount), player).orElse(false);
	}

	public boolean deposit(double amount) {
		return provision.deposit(BigDecimal.valueOf(amount), player).orElse(false);
	}

	public boolean withdraw(double amount) {
		return provision.withdraw(BigDecimal.valueOf(amount), player).orElse(false);
	}


}
