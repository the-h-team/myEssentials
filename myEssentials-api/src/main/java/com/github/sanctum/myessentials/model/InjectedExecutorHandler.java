package com.github.sanctum.myessentials.model;

import com.github.sanctum.myessentials.model.action.IExecutorCalculating;
import com.github.sanctum.myessentials.model.action.IExecutorCompleting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public class InjectedExecutorHandler {

	private final Map<CommandData, List<IExecutorCalculating<? extends CommandSender>>> EXECUTOR_CALCULATIONS;
	private final Map<CommandData, List<IExecutorCompleting<? extends CommandSender>>> EXECUTOR_COMPLETIONS;
	private final Plugin plugin;

	// TODO: finish this javadoc
	/**
	 * @param plugin a plugin
	 */
	public InjectedExecutorHandler(Plugin plugin) {
		this.plugin = plugin;
		this.EXECUTOR_CALCULATIONS = new HashMap<>();
		this.EXECUTOR_COMPLETIONS = new HashMap<>();
	}

	/**
	 * @return The plugin this executor handler is registered under.
	 */
	public @NotNull Plugin getPlugin() {
		return plugin;
	}

	// TODO: finish this javadoc
	/**
	 * @param data data
	 * @param commandData CommandData
	 * @return an InjectedExecutorHandler
	 */
	public @NotNull InjectedExecutorHandler addCalculatingExecutor(CommandData data, IExecutorCalculating<? extends CommandSender> commandData) {
		List<IExecutorCalculating<? extends CommandSender>> array;
		if (EXECUTOR_CALCULATIONS.containsKey(data)) {
			array = new ArrayList<>(EXECUTOR_CALCULATIONS.get(data));
		} else {
			array = new ArrayList<>();
		}
		array.add(commandData);
		EXECUTOR_CALCULATIONS.put(data, array);
		return this;
	}

	// TODO: finish this javadoc
	/**
	 * @param data data
	 * @param commandData CommandData
	 * @return an InjectedExecutorHandler
	 */
	public @NotNull InjectedExecutorHandler addCompletingExecutor(CommandData data, IExecutorCompleting<? extends CommandSender> commandData) {
		List<IExecutorCompleting<? extends CommandSender>> array;
		if (EXECUTOR_COMPLETIONS.containsKey(data)) {
			array = new ArrayList<>(EXECUTOR_COMPLETIONS.get(data));
		} else {
			array = new ArrayList<>();
		}
		array.add(commandData);
		EXECUTOR_COMPLETIONS.put(data, array);
		return this;
	}

	// TODO: finish this javadoc
	/**
	 * @param data CommandData
	 * @return an InjectedExecutorHandler
	 */
	public @NotNull InjectedExecutorHandler removePlayerCalculation(CommandData data) {
		List<IExecutorCalculating<? extends CommandSender>> array;
		if (EXECUTOR_CALCULATIONS.containsKey(data)) {
			array = new ArrayList<>(EXECUTOR_CALCULATIONS.get(data));
			for (IExecutorCalculating<? extends CommandSender> e : array) {
				if (e.getEntity() == ExecutorEntity.PLAYER) {
					array.remove(e);
					break;
				}
			}
			EXECUTOR_CALCULATIONS.put(data, array);
		}
		return this;
	}

	// TODO: finish this javadoc
	/**
	 * @param data data
	 * @return an InjectedExecutorHandler
	 */
	public @NotNull InjectedExecutorHandler removeConsoleCalculation(CommandData data) {
		List<IExecutorCalculating<? extends CommandSender>> array;
		if (EXECUTOR_CALCULATIONS.containsKey(data)) {
			array = new ArrayList<>(EXECUTOR_CALCULATIONS.get(data));
			for (IExecutorCalculating<? extends CommandSender> e : array) {
				if (e.getEntity() == ExecutorEntity.SERVER) {
					array.remove(e);
					break;
				}
			}
			EXECUTOR_CALCULATIONS.put(data, array);
		}
		return this;
	}

	// TODO: finish this javadoc
	/**
	 * @param data data
	 * @return an InjectedExecutorHandler
	 */
	public @NotNull InjectedExecutorHandler removeCompletions(CommandData data) {
		List<IExecutorCompleting<? extends CommandSender>> array;
		if (EXECUTOR_COMPLETIONS.containsKey(data)) {
			array = new ArrayList<>();
			EXECUTOR_COMPLETIONS.put(data, array);
		}
		return this;
	}

	// TODO: finish this javadoc
	/**
	 * @return a map
	 */
	public @NotNull Map<CommandData, List<IExecutorCompleting<? extends CommandSender>>> getExecutorCompletions() {
		return Collections.unmodifiableMap(EXECUTOR_COMPLETIONS);
	}

	// TODO: finish this javadoc
	/**
	 * @return a map
	 */
	public @NotNull Map<CommandData, List<IExecutorCalculating<? extends CommandSender>>> getExecutorCalculations() {
		return Collections.unmodifiableMap(EXECUTOR_CALCULATIONS);
	}

	// TODO: finish this javadoc
	/**
	 * @param label command label
	 * @return a list
	 */
	public @Nullable List<IExecutorCompleting<? extends CommandSender>> getCompletions(String label) {
		return EXECUTOR_COMPLETIONS.entrySet().stream().filter(e -> e.getKey().getLabel().equals(label)).map(Map.Entry::getValue).findFirst().orElse(null);
	}

	// TODO: finish this javadoc
	/**
	 * @param label command label
	 * @return a list
	 */
	public @Nullable List<IExecutorCalculating<? extends CommandSender>> getCalculations(String label) {
		return EXECUTOR_CALCULATIONS.entrySet().stream().filter(e -> e.getKey().getLabel().equals(label)).map(Map.Entry::getValue).findFirst().orElse(null);

	}

	@Override
	public String toString() {
		return "InjectedExecutorHandler{" +
				"executorBases=" + EXECUTOR_CALCULATIONS +
				", plugin=" + plugin +
				'}';
	}
}
