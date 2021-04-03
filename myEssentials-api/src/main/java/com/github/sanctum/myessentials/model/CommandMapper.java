package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.library.Applicable;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.action.IExecutorCalculating;
import com.github.sanctum.myessentials.model.action.IExecutorCompleting;
import com.github.sanctum.myessentials.model.specifier.ConsoleResultingCommandExecutor;
import com.github.sanctum.myessentials.model.specifier.PlayerResultingCommandExecutor;
import com.github.sanctum.myessentials.model.specifier.PlayerResultingTabCompleter;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandMapper extends CommandBuilder {

	private final InjectedCommandExecutor KNOWN_COMMANDS;

	private List<Applicable> APPLICABLE_HANDLER;

	protected CommandMapper(CommandData data) {
		super(data);
		this.KNOWN_COMMANDS = MyEssentialsAPI.getInstance().getExecutor();
	}

	protected CommandMapper(CommandData data, Applicable... applicables) {
		super(data, applicables);
		this.KNOWN_COMMANDS = MyEssentialsAPI.getInstance().getExecutor();
	}

	public static CommandMapper from(CommandData data) {
		return new CommandMapper(data);
	}

	public static CommandMapper load(CommandData data, Applicable... applicable) {
		return new CommandMapper(data, applicable);
	}

	public CommandMapper apply(PlayerResultingCommandExecutor commandData) {
		this.KNOWN_COMMANDS.addResultingExecutor(this.commandData, commandData);
		return this;
	}

	public CommandMapper completion(PlayerResultingTabCompleter completer) {
		this.KNOWN_COMMANDS.addCompletingExecutor(this.commandData, completer);
		return this;
	}

	public CommandMapper read(ConsoleResultingCommandExecutor commandData) {
		this.KNOWN_COMMANDS.addResultingExecutor(this.commandData, commandData);
		return this;
	}

	@Override
	public @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		for (IExecutorCompleting<? extends CommandSender> pr : KNOWN_COMMANDS.getCompleters(this.commandData.getLabel())) {
			if (pr.getEntity() == ExecutorEntity.PLAYER) {
				return pr.execute(this, player, alias, args);
			}
		}
		return null;
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		for (IExecutorCalculating<? extends CommandSender> pr : KNOWN_COMMANDS.getExecutors(this.commandData.getLabel())) {
			if (pr.getEntity() == ExecutorEntity.PLAYER) {
				pr.execute(this, player, commandLabel, args);
				break;
			}
		}
		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		for (IExecutorCalculating<? extends CommandSender> pr : KNOWN_COMMANDS.getExecutors(this.commandData.getLabel())) {
			if (pr.getEntity() == ExecutorEntity.SERVER) {
				pr.execute(this, sender, commandLabel, args);
				break;
			}
		}
		return true;
	}
}
