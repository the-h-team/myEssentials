package com.github.sanctum.myessentials.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public interface TextLibrary {
    static TextLibrary text1_16() {
        return new TextLibrary() {
            @Override
            public TextComponent textRunnable(String normalText, String hoverText, String hoverMessage, String command) {
                final TextComponent textComponent = new TextComponent();
                for (BaseComponent bc : TextComponent.fromLegacyText(normalText)) {
                    textComponent.addExtra(bc);
                }
                final TextComponent hover = new TextComponent();
                for (BaseComponent bc : TextComponent.fromLegacyText(hoverText)) {
                    hover.addExtra(bc);
                }
                final TextComponent message = new TextComponent();
                for (BaseComponent bc : TextComponent.fromLegacyText(hoverMessage)) {
                    message.addExtra(bc);
                }
                hover.setHoverEvent(message.getHoverEvent());
                return textComponent;
            }
        };
    }
    static TextLibrary legacy() {
        return new TextLibrary() {
            @Override
            public TextComponent textRunnable(String normalText, String hoverText, String hoverMessage, String command) {
                return null;
            }
        };
    }

    TextComponent textRunnable(String normalText, String hoverText, String hoverMessage, String command);
}
