package com.github.sanctum.myessentials.model.executor;

import com.github.sanctum.myessentials.model.CommandInput;
import org.bukkit.command.CommandSender;

public interface IExecutorCommand {

	void execute(CommandInput output, CommandSender sender, String commandLabel, String[] args);

	default IExecutorEntity getEntity() {
		return IExecutorEntity.UNKNOWN;
	}

}
