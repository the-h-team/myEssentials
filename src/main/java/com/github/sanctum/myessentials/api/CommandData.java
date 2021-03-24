package com.github.sanctum.myessentials.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Describes a command's label, description and base permission node.
 */
public interface CommandData {

    /**
     * The label for the command.
     *
     * @return label for command
     */
    @NotNull String getLabel();

    /**
     * How to use the command.
     *
     * @return The brief usage description.
     */
    @NotNull String getUsage();

    /**
     * A description for the command.
     *
     * @return description for command
     */
    @NotNull String getDescription();

    /**
     * The permission node for the command.
     *
     * @return permission node or null
     */
    @Nullable String getPermissionNode();
}
