package com.github.sanctum.myessentials.api;

import java.util.Set;

public interface MyEssentialsAPI {
    /**
     * Get data for all commands registered by MyEssentials.
     *
     * @return set of data for all registered commands
     */
    Set<CommandData> getRegisteredCommands();
}
