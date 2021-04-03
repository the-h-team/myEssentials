package com.github.sanctum.myessentials.model.specifier;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.ExecutorEntity;
import com.github.sanctum.myessentials.model.action.IExecutorCalculating;
import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface ConsoleResultingCommandExecutor extends IExecutorCalculating<CommandSender> {

	void run(CommandBuilder builder, CommandSender sender, String commandLabel, String[] args);

	@Override
	default ExecutorEntity getEntity() {
		return ExecutorEntity.SERVER;
	}

}
