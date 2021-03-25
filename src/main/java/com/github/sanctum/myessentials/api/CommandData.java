/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
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
