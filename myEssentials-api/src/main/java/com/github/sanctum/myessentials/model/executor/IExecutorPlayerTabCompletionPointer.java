package com.github.sanctum.myessentials.model.executor;

import com.github.sanctum.myessentials.model.CommandInput;
import java.util.List;
import org.bukkit.entity.Player;

/**
 * A constructive interface for providing tab completions to a player.
 */
@FunctionalInterface
public interface IExecutorPlayerTabCompletionPointer extends IExecutorTabCompletionBase<Player> {

	List<String> run(CommandInput output, Player sender, String commandLabel, String[] args);

	@Override
	default IExecutorEntity getEntity() {
		return IExecutorEntity.PLAYER;
	}
}
