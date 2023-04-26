package com.github.sanctum.myessentials.model.executor;

import com.github.sanctum.myessentials.model.CommandInput;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 * A constructive interface for providing tab completions to a player.
 */
@FunctionalInterface
public interface IExecutorConsoleTabCompletionPointer extends IExecutorTabCompletionBase<CommandSender> {

	List<String> run(CommandInput output, CommandSender sender, String commandLabel, String[] args);

	@Override
	default IExecutorEntity getEntity() {
		return IExecutorEntity.PLAYER;
	}
}
