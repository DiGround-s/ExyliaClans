package net.diground.exylia.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.BaseComponent;

public class NotificationUtils {

    public static BaseComponent[] buildMessageWithButtons(String message) {
        ComponentBuilder builder = new ComponentBuilder();

        // Split the message by placeholders
        String[] parts = message.split("%accept%");

        for (int i = 0; i < parts.length; i++) {
            builder.append(parts[i]);

            // Add button after each placeholder occurrence
            if (i < parts.length - 1) {
                TextComponent acceptButton = new TextComponent("[ACEPTAR]");
                acceptButton.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan regroup accept"));
                builder.append(acceptButton);
            }
        }

        return builder.create();
    }
}
