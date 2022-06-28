package com.github.sanctum.myessentials.model.executor;

import com.github.sanctum.myessentials.model.CommandOutput;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface IExecutorCommandBase<T extends CommandSender> extends IExecutorCommand {

	@SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
	@Override
	default void execute(CommandOutput output, CommandSender sender, String commandLabel, String[] args) {
		Method runMethod = Arrays.stream(this.getClass().getDeclaredMethods()).filter(m -> m.getName().equals("run")).findFirst().get();
		Class<?> type = runMethod.getParameterTypes()[1];
		if (type.isInstance(sender)) {
			this.run(output, (T) type.cast(sender), commandLabel, args);
		} else {
			throw new IllegalStateException("Unable to process resulting executor, type " + type.getSimpleName() + " isn't representative of object CommandSender.");
		}
	}

	void run(CommandOutput builder, T sender, String commandLabel, String[] args);

}
