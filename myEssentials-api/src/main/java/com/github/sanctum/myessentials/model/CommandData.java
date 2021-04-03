/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.model;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Describes a command's label, description and base permission node.
 */
public interface CommandData {

    /**
     * The label for this command.
     *
     * @return label for this command
     */
    @NotNull String getLabel();

    /**
     * Get a list of aliases for this command.
     * <p>
     * Empty by default.
     *
     * @return string list of aliases for this command
     */
    default @NotNull List<String> getAliases() {
        return Collections.emptyList();
    }

    /**
     * A message that explains how to use the command.
     *
     * @return a brief usage message
     */
    @NotNull String getUsage();

    /**
     * A description for the command.
     *
     * @return description for the command
     */
    @NotNull String getDescription();

    /**
     * The permission node for the command.
     *
     * @return permission node or null
     */
    @Nullable String getPermissionNode();
}
