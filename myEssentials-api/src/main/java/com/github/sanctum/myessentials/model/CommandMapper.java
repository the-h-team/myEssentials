package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.library.Applicable;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.executor.IExecutorCommand;
import com.github.sanctum.myessentials.model.executor.IExecutorConsolePointer;
import com.github.sanctum.myessentials.model.executor.IExecutorConsoleTabCompletionPointer;
import com.github.sanctum.myessentials.model.executor.IExecutorOutputChannel;
import com.github.sanctum.myessentials.model.executor.IExecutorPlayerPointer;
import com.github.sanctum.myessentials.model.executor.IExecutorPlayerTabCompletionPointer;
import com.github.sanctum.myessentials.model.executor.IExecutorTabCompletion;
import com.github.sanctum.myessentials.model.executor.IExecutorTabCompletionBase;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

/**
 * Build &amp; complete command construction quicker than you ever have before.
 */
@SuppressWarnings("UnusedReturnValue")
public final class CommandMapper {

	private final IExecutorHandler handler;
	private final IExecutorOutputChannel output;

	public CommandMapper(@NotNull CommandData data, Applicable... applicables) {
		this(data, MyEssentialsAPI.getInstance().getExecutorHandler(), applicables);
	}

	public CommandMapper(@NotNull CommandData data, @NotNull IExecutorHandler handler, Applicable... applicables) {
		this.handler = MyEssentialsAPI.getInstance().getExecutorHandler();
		this.output = new IExecutorOutputChannel(handler, data, applicables);
	}

	/**
	 * Build player specific argument's for the {@link IExecutorCommand}
	 *
	 * @param commandData Build your command using the provided {@link CommandData}.
	 *                    An example best used would be {@code ((builder, sender, label, args) -> {}}
	 *                    Every native {@link org.bukkit.command.Command} class parameter and the registered data.
	 * @return The same Command Mapper.
	 */
	public @NotNull CommandMapper setPlayerExecutor(@NotNull IExecutorPlayerPointer commandData) {
		this.handler.addExecutor(this.output.getData(), commandData);
		return this;
	}

	/**
	 * Build console specific argument's for the {@link IExecutorCommand}
	 *
	 * @param commandData Build your command using the provided {@link CommandData}.
	 *                    An example best used would be {@code ((builder, sender, label, args) -> {}}
	 *                    Every native {@link org.bukkit.command.Command} class parameter and the registered data.
	 * @return The same Command Mapper.
	 */
	public @NotNull CommandMapper setConsoleExecutor(@NotNull IExecutorConsolePointer commandData) {
		this.handler.addExecutor(this.output.getData(), commandData);
		return this;
	}

	/**
	 * Build &amp; customize the tab completions for your registered command.
	 *
	 * @param completer Build your {@link IExecutorCommand} linked
	 *                  {@link IExecutorTabCompletionBase} {@link IExecutorTabCompletion}
	 * @return The same Command Mapper.
	 */
	public @NotNull CommandMapper setPlayerTabCompleter(@NotNull IExecutorPlayerTabCompletionPointer completer) {
		this.handler.addExecutor(this.output.getData(), completer);
		return this;
	}

	/**
	 * Build &amp; customize the tab completions for your registered command.
	 *
	 * @param completer Build your {@link IExecutorCommand} linked
	 *                  {@link IExecutorTabCompletionBase} {@link IExecutorTabCompletion}
	 * @return The same Command Mapper.
	 */
	public @NotNull CommandMapper setConsoleTabCompleter(@NotNull IExecutorConsoleTabCompletionPointer completer) {
		this.handler.addExecutor(this.output.getData(), completer);
		return this;
	}

	/**
	 * Assign the data from a {@link CommandData} inheritable into
	 * the server's executor map.
	 *
	 * @param data The command properties to register. This information is precisely grabbing
	 *             information such as {@link CommandData#getLabel()}, {@link CommandData#getDescription()},
	 *             {@link CommandData#getPermissionNode()} and more.
	 * @return An instantiated Command Mapper.
	 */
	public static @NotNull CommandMapper from(@NotNull CommandData data) {
		return new CommandMapper(data);
	}

	/**
	 * Assign the data from a {@link CommandData} inheritable into
	 * the server's executor map.
	 *
	 * @param data            The command properties to register. This information is precisely grabbing
	 *                        information such as {@link CommandData#getLabel()}, {@link CommandData#getDescription()},
	 *                        {@link CommandData#getPermissionNode()} and more.
	 * @param outputProcessor Similar to {@link CommandMapper#load(CommandData, Applicable...)} load data on command structure
	 *                        initialization with the underlying command data information.
	 * @return An instantiated Command Mapper.
	 */
	public static @NotNull CommandMapper from(@NotNull CommandData data, @NotNull Consumer<CommandOutput> outputProcessor) {
		CommandMapper mapper = new CommandMapper(data);
		outputProcessor.accept(mapper.output);
		return mapper;
	}

	/**
	 * Assign the data from an {@link CommandData} inheritable into
	 * the server's executor map.
	 *
	 * @param data       The command properties to register. This information is precisely grabbing
	 *                   information such as {@link CommandData#getLabel()}, {@link CommandData#getDescription()},
	 *                   {@link CommandData#getPermissionNode()} and more.
	 * @param applicable The optional pre-processed {@code} operations ahead of command registration.
	 *                   As described any information passed here will be processed just before the command
	 *                   register's.
	 * @return An instantiated Command Mapper.
	 */
	public static @NotNull CommandMapper load(@NotNull CommandData data, Applicable... applicable) {
		return new CommandMapper(data, applicable);
	}

}
