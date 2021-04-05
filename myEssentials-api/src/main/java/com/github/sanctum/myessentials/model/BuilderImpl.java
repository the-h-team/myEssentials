package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.library.Applicable;
import com.github.sanctum.myessentials.model.action.IExecutorCalculating;
import com.github.sanctum.myessentials.model.action.IExecutorCompleting;
import java.util.List;
import java.util.Objects;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BuilderImpl extends CommandBuilder {
	private final InjectedExecutorHandler knownCommands;

	public BuilderImpl(InjectedExecutorHandler executor, CommandData commandData) {
		super(commandData);
		this.knownCommands = executor;
	}

	public BuilderImpl(InjectedExecutorHandler executor, CommandData commandData, Applicable... pre) {
		super(commandData, pre);
		this.knownCommands = executor;
	}

	@Override
	public @Nullable List<String> tabComplete(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		for (IExecutorCompleting<? extends CommandSender> pr : Objects.requireNonNull(knownCommands.getCompletions(this.commandData.getLabel()))) {
			if (pr.getEntity() == ExecutorEntity.PLAYER) {
				return pr.execute(this, player, alias, args);
			}
		}
		return null;
	}

	@Override
	public boolean playerView(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		for (IExecutorCalculating<? extends CommandSender> pr : Objects.requireNonNull(knownCommands.getCalculations(this.commandData.getLabel()))) {
			if (pr.getEntity() == ExecutorEntity.PLAYER) {
				pr.execute(this, player, commandLabel, args);
				break;
			}
		}
		return true;
	}

	@Override
	public boolean consoleView(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		for (IExecutorCalculating<? extends CommandSender> pr : Objects.requireNonNull(knownCommands.getCalculations(this.commandData.getLabel()))) {
			if (pr.getEntity() == ExecutorEntity.SERVER) {
				pr.execute(this, sender, commandLabel, args);
				break;
			}
		}
		return true;
	}
}
