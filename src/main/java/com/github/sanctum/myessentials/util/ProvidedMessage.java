/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides a message source template.
 */
public interface ProvidedMessage {
    /**
     * Retrieve the raw message.
     * <p>
     * May return null, for instance if configs are in an invalid state.
     *
     * @return raw message or null
     */
    @Nullable String get();

    /**
     * Processes the raw message provided by {@link #get()}.
     * <p>
     * This involves null-sanitation and may also include
     * color code processing.
     *
     * @return valid String object ready for players
     */
    @Override
    @NotNull
    String toString();

    /**
     * Replace placeholder portions of the message using varargs.
     * <p>
     * First vararg = {0}, second = {1} and so on.
     *
     * @param objects items to replace in order
     * @return message with provided replacements
     */
    default String replace(Object... objects) {
        String replacement = toString();
        int i = 0;
        for (Object obj : objects) {
            replacement = StringUtils.replace(replacement, "{" + i++ + "}", String.valueOf(obj));
        }
        return replacement;
    }

    /**
     * Process message into TextComponent.
     *
     * @return message as appended TextComponent
     */
    default TextComponent asComponent() {
        final TextComponent textComponent = new TextComponent();
        for (BaseComponent bc : TextComponent.fromLegacyText(toString())) {
            textComponent.addExtra(bc);
        }
        return textComponent;
    }
}
