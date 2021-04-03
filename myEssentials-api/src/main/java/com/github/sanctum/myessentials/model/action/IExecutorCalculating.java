package com.github.sanctum.myessentials.model.action;

import com.github.sanctum.myessentials.model.CommandBuilder;
import com.github.sanctum.myessentials.model.base.IExecutorBaseCommand;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface IExecutorCalculating<T extends CommandSender> extends IExecutorBaseCommand {

	@SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
	@Override
	default void execute(CommandBuilder builder, CommandSender sender, String commandLabel, String[] args) {
		Method runMethod = Arrays.stream(this.getClass().getDeclaredMethods()).filter(m -> m.getName().equals("run")).findFirst().get();
		Class<?> type = runMethod.getParameterTypes()[1];
		if (type.isInstance(sender)) {
			this.run(builder, (T) type.cast(sender), commandLabel, args);
		} else {
			throw new IllegalStateException("Unable to process resulting executor, type " + type.getSimpleName() + " isn't representative of object CommandSender.");
		}
	}

	void run(CommandBuilder builder, T sender, String commandLabel, String[] args);

}
