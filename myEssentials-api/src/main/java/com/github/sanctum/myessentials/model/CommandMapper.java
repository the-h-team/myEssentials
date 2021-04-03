package com.github.sanctum.myessentials.model;

import com.github.sanctum.labyrinth.library.Applicable;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.specifier.ConsoleResultingCommandExecutor;
import com.github.sanctum.myessentials.model.specifier.PlayerResultingCommandExecutor;
import com.github.sanctum.myessentials.model.specifier.PlayerResultingTabCompleter;

public class CommandMapper {

	private final InjectedCommandExecutor KNOWN_COMMANDS;

	private final CommandBuilder builder;

	protected CommandMapper(CommandData data) {
		this.KNOWN_COMMANDS = MyEssentialsAPI.getInstance().getExecutor();
		this.builder = new BuilderImpl(KNOWN_COMMANDS, data);
	}

	protected CommandMapper(CommandData data, Applicable... applicables) {
		this.KNOWN_COMMANDS = MyEssentialsAPI.getInstance().getExecutor();
		this.builder = new BuilderImpl(KNOWN_COMMANDS, data, applicables);
	}

	public static CommandMapper from(CommandData data) {
		return new CommandMapper(data);
	}

	public static CommandMapper load(CommandData data, Applicable... applicable) {
		return new CommandMapper(data, applicable);
	}

	public CommandMapper apply(PlayerResultingCommandExecutor commandData) {
		this.KNOWN_COMMANDS.addResultingExecutor(this.builder.getData(), commandData);
		return this;
	}

	public CommandMapper completion(PlayerResultingTabCompleter completer) {
		this.KNOWN_COMMANDS.addCompletingExecutor(this.builder.getData(), completer);
		return this;
	}

	public CommandMapper read(ConsoleResultingCommandExecutor commandData) {
		this.KNOWN_COMMANDS.addResultingExecutor(this.builder.getData(), commandData);
		return this;
	}

}
