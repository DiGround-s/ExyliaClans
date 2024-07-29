package net.diground.exylia.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.md_5.bungee.api.ChatColor;

public class ChatUtils {



    // Minimessage
    public static Component translateColors(String message) {
        Component parsed = MiniMessage.miniMessage().deserialize(message, Placeholder.unparsed("name", "name"));
        return parsed;
    }



    // Old
    public static String oldTranslateColors(String message) {
        return ChatColor.translateAlternateColorCodes('&', GradientUtils.applyGradientsAndHex(message));
    }
}
