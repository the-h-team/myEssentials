package com.github.sanctum.myessentials.model.executor;

import com.github.sanctum.labyrinth.library.Applicable;
import com.github.sanctum.myessentials.model.CommandData;
import com.github.sanctum.myessentials.model.CommandOutput;
import com.github.sanctum.myessentials.model.IExecutorHandler;
import java.util.List;
import java.util.Objects;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class IExecutorOutputChannel extends CommandOutput {
	private final IExecutorHandler handler;

	public IExecutorOutputChannel(IExecutorHandler handler, CommandData commandData) {
		super(commandData);
		this.handler = handler;
	}

	public IExecutorOutputChannel(IExecutorHandler handler, CommandData commandData, Applicable... pre) {
		super(commandData, pre);
		this.handler = handler;
	}

	@Override
	public @Nullable List<String> onPlayerTab(@NotNull Player player, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		for (IExecutorTabCompletionBase<? extends CommandSender> pr : Objects.requireNonNull(handler.getCompletions(this.commandData.getLabel()))) {
			if (pr.getEntity() == IExecutorEntity.PLAYER) {
				return pr.execute(this, player, alias, args);
			}
		}
		return null;
	}

	@Override
	public @Nullable List<String> onConsoleTab(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		for (IExecutorTabCompletionBase<? extends CommandSender> pr : Objects.requireNonNull(handler.getCompletions(this.commandData.getLabel()))) {
			if (pr.getEntity() == IExecutorEntity.SERVER) {
				return pr.execute(this, sender, alias, args);
			}
		}
		return null;
	}

	@Override
	public boolean onPlayer(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] args) {
		for (IExecutorCommandBase<? extends CommandSender> pr : Objects.requireNonNull(handler.getCalculations(this.commandData.getLabel()))) {
			if (pr.getEntity() == IExecutorEntity.PLAYER) {
				pr.execute(this, player, commandLabel, args);
				break;
			}
		}
		return true;
	}

	@Override
	public boolean onConsole(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		for (IExecutorCommandBase<? extends CommandSender> pr : Objects.requireNonNull(handler.getCalculations(this.commandData.getLabel()))) {
			if (pr.getEntity() == IExecutorEntity.SERVER) {
				pr.execute(this, sender, commandLabel, args);
				break;
			}
		}
		return true;
	}
}
