package com.github.sanctum.myessentials.model.specifier;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.ExecutorEntity;
import com.github.sanctum.myessentials.model.action.IExecutorCompleting;
import java.util.List;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface PlayerResultingTabCompleter extends IExecutorCompleting<Player> {

	List<String> run(CommandBuilder builder, Player sender, String commandLabel, String[] args);

	@Override
	default ExecutorEntity getEntity() {
		return ExecutorEntity.PLAYER;
	}
}
