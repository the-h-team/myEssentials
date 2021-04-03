package com.github.sanctum.myessentials.model.base;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.ExecutorEntity;
import java.util.List;
import org.bukkit.command.CommandSender;

public interface IExecutorBaseCompletion {

	List<String> execute(CommandBuilder builder, CommandSender sender, String commandLabel, String[] args);

	default ExecutorEntity getEntity() {
		return ExecutorEntity.UNKNOWN;
	}

}
