package com.github.sanctum.myessentials.model.executor;

import com.github.sanctum.myessentials.model.CommandInput;
import org.bukkit.entity.Player;

/**
 * A constructive interface for forming command executions on players.
 */
@FunctionalInterface
public interface IExecutorPlayerPointer extends IExecutorCommandBase<Player> {

	void run(CommandInput output, Player sender, String commandLabel, String[] args);

	@Override
	default IExecutorEntity getEntity() {
		return IExecutorEntity.PLAYER;
	}
}
