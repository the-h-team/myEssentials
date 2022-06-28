package com.github.sanctum.myessentials.model.executor;

import com.github.sanctum.myessentials.model.CommandOutput;
import org.bukkit.command.CommandSender;

/**
 * A constructive interface for executing commands on console.
 */
@FunctionalInterface
public interface IExecutorConsolePointer extends IExecutorCommandBase<CommandSender> {

	void run(CommandOutput output, CommandSender sender, String commandLabel, String[] args);

	@Override
	default IExecutorEntity getEntity() {
		return IExecutorEntity.SERVER;
	}

}
