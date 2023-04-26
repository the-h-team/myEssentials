package com.github.sanctum.myessentials.model.executor;

import com.github.sanctum.myessentials.model.CommandInput;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface IExecutorTabCompletionBase<T extends CommandSender> extends IExecutorTabCompletion {

	@SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
	@Override
	default List<String> execute(CommandInput output, CommandSender sender, String commandLabel, String[] args) {
		Method runMethod = Arrays.stream(this.getClass().getDeclaredMethods()).filter(m -> m.getName().equals("run")).findFirst().get();
		Class<?> type = runMethod.getParameterTypes()[1];
		if (type.isInstance(sender)) {
			return this.run(output, (T) type.cast(sender), commandLabel, args);
		} else {
			throw new IllegalStateException("Unable to process resulting executor, type isn't representative of object CommandSender.");
		}
	}

	List<String> run(CommandInput builder, T sender, String commandLabel, String[] args);

}
