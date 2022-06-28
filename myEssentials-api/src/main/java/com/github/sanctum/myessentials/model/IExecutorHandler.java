package com.github.sanctum.myessentials.model;

import com.github.sanctum.myessentials.model.executor.IExecutorCommandBase;
import com.github.sanctum.myessentials.model.executor.IExecutorEntity;
import com.github.sanctum.myessentials.model.executor.IExecutorTabCompletionBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
@SuppressWarnings("UnusedReturnValue")
public class IExecutorHandler {

	protected final Map<CommandData, List<IExecutorCommandBase<? extends CommandSender>>> executorCalculations = new HashMap<>();
	protected final Map<CommandData, List<IExecutorTabCompletionBase<? extends CommandSender>>> executorCompletions = new HashMap<>();

	/**
	 * @param data data
	 * @param commandData CommandData
	 * @return an InjectedExecutorHandler
	 */
	public @NotNull IExecutorHandler addExecutor(CommandData data, IExecutorCommandBase<? extends CommandSender> commandData) {
		List<IExecutorCommandBase<? extends CommandSender>> array;
		if (executorCalculations.containsKey(data)) {
			array = new ArrayList<>(executorCalculations.get(data));
		} else {
			array = new ArrayList<>();
		}
		array.add(commandData);
		executorCalculations.put(data, array);
		return this;
	}

	/**
	 * @param data        data
	 * @param commandData CommandData
	 * @return an InjectedExecutorHandler
	 */
	public @NotNull IExecutorHandler addExecutor(CommandData data, IExecutorTabCompletionBase<? extends CommandSender> commandData) {
		List<IExecutorTabCompletionBase<? extends CommandSender>> array;
		if (executorCompletions.containsKey(data)) {
			array = new ArrayList<>(executorCompletions.get(data));
		} else {
			array = new ArrayList<>();
		}
		array.add(commandData);
		executorCompletions.put(data, array);
		return this;
	}

	/**
	 * @param data CommandData
	 * @return an InjectedExecutorHandler
	 */
	public @NotNull IExecutorHandler removePlayerCalculation(CommandData data) {
		List<IExecutorCommandBase<? extends CommandSender>> array;
		if (executorCalculations.containsKey(data)) {
			array = new ArrayList<>(executorCalculations.get(data));
			for (IExecutorCommandBase<? extends CommandSender> e : array) {
				if (e.getEntity() == IExecutorEntity.PLAYER) {
					array.remove(e);
					break;
				}
			}
			executorCalculations.put(data, array);
		}
		return this;
	}

	/**
	 * @param data data
	 * @return an InjectedExecutorHandler
	 */
	public @NotNull IExecutorHandler removeConsoleCalculation(CommandData data) {
		List<IExecutorCommandBase<? extends CommandSender>> array;
		if (executorCalculations.containsKey(data)) {
			array = new ArrayList<>(executorCalculations.get(data));
			for (IExecutorCommandBase<? extends CommandSender> e : array) {
				if (e.getEntity() == IExecutorEntity.SERVER) {
					array.remove(e);
					break;
				}
			}
			executorCalculations.put(data, array);
		}
		return this;
	}

	/**
	 * @param data data
	 * @return an InjectedExecutorHandler
	 */
	public @NotNull IExecutorHandler removeCompletions(CommandData data) {
		List<IExecutorTabCompletionBase<? extends CommandSender>> array;
		if (executorCompletions.containsKey(data)) {
			array = new ArrayList<>();
			executorCompletions.put(data, array);
		}
		return this;
	}

	/**
	 * @return a map
	 */
	public @NotNull Map<CommandData, List<IExecutorTabCompletionBase<? extends CommandSender>>> getExecutorCompletions() {
		return Collections.unmodifiableMap(executorCompletions);
	}

	/**
	 * @return a map
	 */
	public @NotNull Map<CommandData, List<IExecutorCommandBase<? extends CommandSender>>> getExecutorCalculations() {
		return Collections.unmodifiableMap(executorCalculations);
	}

	/**
	 * @param label command label
	 * @return a list
	 */
	public @Nullable List<IExecutorTabCompletionBase<? extends CommandSender>> getCompletions(String label) {
		return executorCompletions.entrySet().stream().filter(e -> e.getKey().getLabel().equals(label)).map(Map.Entry::getValue).findFirst().orElse(null);
	}

	/**
	 * @param label command label
	 * @return a list
	 */
	public @Nullable List<IExecutorCommandBase<? extends CommandSender>> getCalculations(String label) {
		return executorCalculations.entrySet().stream().filter(e -> e.getKey().getLabel().equals(label)).map(Map.Entry::getValue).findFirst().orElse(null);

	}

	@Override
	public String toString() {
		return "InjectedExecutorHandler{" +
				"executorBases=" + executorCalculations + '}';
	}
}
