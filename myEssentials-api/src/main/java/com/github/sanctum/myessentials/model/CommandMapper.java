package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.library.Applicable;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.specifier.ConsoleResultingCommandExecutor;
import com.github.sanctum.myessentials.model.specifier.PlayerResultingCommandExecutor;
import com.github.sanctum.myessentials.model.specifier.PlayerResultingTabCompleter;
import org.jetbrains.annotations.NotNull;

/**
 * Build &amp; complete command construction quicker than you ever have before.
 */
public final class CommandMapper {

	private final @NotNull InjectedExecutorHandler REGISTRY;

	private final CommandBuilder builder;

	protected CommandMapper(@NotNull CommandData data) {
		this.REGISTRY = MyEssentialsAPI.getInstance().getExecutorHandler();
		this.builder = new BuilderImpl(REGISTRY, data);
	}

	protected CommandMapper(@NotNull CommandData data, Applicable... applicables) {
		this.REGISTRY = MyEssentialsAPI.getInstance().getExecutorHandler();
		this.builder = new BuilderImpl(REGISTRY, data, applicables);
	}

	/**
	 * Assign the data from an {@link CommandData} inheritable into
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

	/**
	 * Build player specific argument's for the {@link com.github.sanctum.myessentials.model.base.IExecutorBaseCommand}
	 *
	 * @param commandData Build your command using the provided {@link CommandData}.
	 *                    An example best used would be {@code ((builder, sender, label, args) -> {}}
	 *                    Every native {@link org.bukkit.command.Command} class parameter and the registered data.
	 * @return The same Command Mapper.
	 */
	public @NotNull CommandMapper apply(@NotNull PlayerResultingCommandExecutor commandData) {
		this.REGISTRY.addCalculatingExecutor(this.builder.getData(), commandData);
		return this;
	}

	/**
	 * Build console specific argument's for the {@link com.github.sanctum.myessentials.model.base.IExecutorBaseCommand}
	 *
	 * @param commandData Build your command using the provided {@link CommandData}.
	 *                    An example best used would be {@code ((builder, sender, label, args) -> {}}
	 *                    Every native {@link org.bukkit.command.Command} class parameter and the registered data.
	 * @return The same Command Mapper.
	 */
	public @NotNull CommandMapper next(@NotNull ConsoleResultingCommandExecutor commandData) {
		this.REGISTRY.addCalculatingExecutor(this.builder.getData(), commandData);
		return this;
	}

	/**
	 * Build &amp; customize the tab completions for your registered command.
	 *
	 * @param completer Build your {@link com.github.sanctum.myessentials.model.base.IExecutorBaseCommand} linked
	 *                  {@link com.github.sanctum.myessentials.model.action.IExecutorCompleting} {@link com.github.sanctum.myessentials.model.base.IExecutorBaseCompletion}
	 * @return The same Command Mapper.
	 */
	public @NotNull CommandMapper read(@NotNull PlayerResultingTabCompleter completer) {
		this.REGISTRY.addCompletingExecutor(this.builder.getData(), completer);
		return this;
	}

}
