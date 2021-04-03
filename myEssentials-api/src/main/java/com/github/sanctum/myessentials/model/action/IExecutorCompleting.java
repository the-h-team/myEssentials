package com.github.sanctum.myessentials.model.action;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.base.IExecutorBaseCompletion;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface IExecutorCompleting<T extends CommandSender> extends IExecutorBaseCompletion {

	@SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
	@Override
	default List<String> execute(CommandBuilder builder, CommandSender sender, String commandLabel, String[] args) {
		Method runMethod = Arrays.stream(this.getClass().getDeclaredMethods()).filter(m -> m.getName().equals("run")).findFirst().get();
		Class<?> type = runMethod.getParameterTypes()[1];
		if (type.isInstance(sender)) {
			return this.run(builder, (T) type.cast(sender), commandLabel, args);
		} else {
			throw new IllegalStateException("Unable to process resulting executor, type isn't representative of object CommandSender.");
		}
	}

	List<String> run(CommandBuilder builder, T sender, String commandLabel, String[] args);

}
