package com.github.sanctum.myessentials.model.executor;

import com.github.sanctum.myessentials.model.CommandInput;
import java.util.List;
import org.bukkit.command.CommandSender;

public interface IExecutorTabCompletion {

	List<String> execute(CommandInput output, CommandSender sender, String commandLabel, String[] args);

	default IExecutorEntity getEntity() {
		return IExecutorEntity.UNKNOWN;
	}

}
