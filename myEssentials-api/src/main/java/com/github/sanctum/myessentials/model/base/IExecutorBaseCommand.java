package com.github.sanctum.myessentials.model.base;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.ExecutorEntity;
import org.bukkit.command.CommandSender;

public interface IExecutorBaseCommand {

	void execute(CommandBuilder builder, CommandSender sender, String commandLabel, String[] args);

	default ExecutorEntity getEntity() {
		return ExecutorEntity.UNKNOWN;
	}

}
