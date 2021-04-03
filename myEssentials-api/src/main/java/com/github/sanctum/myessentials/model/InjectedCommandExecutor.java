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

public class InjectedCommandExecutor {

	private final Map<CommandData, List<IExecutorCalculating<? extends CommandSender>>> CALCULATION_MAP;
	private final Map<CommandData, List<IExecutorCompleting<? extends CommandSender>>> EXECUTOR_COMPLETIONS;
	private final Plugin plugin;

	public InjectedCommandExecutor(Plugin plugin) {
		this.plugin = plugin;
		this.CALCULATION_MAP = new HashMap<>();
		this.EXECUTOR_COMPLETIONS = new HashMap<>();
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public InjectedCommandExecutor addResultingExecutor(CommandData data, IExecutorCalculating<? extends CommandSender> commandData) {
		List<IExecutorCalculating<? extends CommandSender>> array;
		if (CALCULATION_MAP.containsKey(data)) {
			array = new ArrayList<>(CALCULATION_MAP.get(data));
		} else {
			array = new ArrayList<>();
		}
		array.add(commandData);
		CALCULATION_MAP.put(data, array);
		return this;
	}

	public InjectedCommandExecutor addCompletingExecutor(CommandData data, IExecutorCompleting<? extends CommandSender> commandData) {
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

	public Map<CommandData, List<IExecutorCompleting<? extends CommandSender>>> getExecutorCompletions() {
		return Collections.unmodifiableMap(EXECUTOR_COMPLETIONS);
	}

	public Map<CommandData, List<IExecutorCalculating<? extends CommandSender>>> getExecutorCalculationMap() {
		return Collections.unmodifiableMap(CALCULATION_MAP);
	}

	public List<IExecutorCompleting<? extends CommandSender>> getCompleters(String label) {
		return EXECUTOR_COMPLETIONS.entrySet().stream().filter(e -> e.getKey().getLabel().equals(label)).map(Map.Entry::getValue).findFirst().orElse(null);
	}

	public List<IExecutorCalculating<? extends CommandSender>> getExecutors(String label) {
		return CALCULATION_MAP.entrySet().stream().filter(e -> e.getKey().getLabel().equals(label)).map(Map.Entry::getValue).findFirst().orElse(null);

	}

	@Override
	public String toString() {
		return "InjectedCommandExecutor{" +
				"executorBases=" + CALCULATION_MAP +
				", plugin=" + plugin +
				'}';
	}
}
