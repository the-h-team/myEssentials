package com.github.sanctum.myessentials.model.specifier;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.ExecutorEntity;
import com.github.sanctum.myessentials.model.action.IExecutorCalculating;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface PlayerResultingCommandExecutor extends IExecutorCalculating<Player> {

	void run(CommandBuilder builder, Player sender, String commandLabel, String[] args);

	@Override
	default ExecutorEntity getEntity() {
		return ExecutorEntity.PLAYER;
	}
}
