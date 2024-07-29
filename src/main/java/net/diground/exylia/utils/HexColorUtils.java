package net.diground.exylia.utils;

import net.md_5.bungee.api.ChatColor;

public class HexColorUtils {

    public static String toChatColor(String hex) {
        return ChatColor.of(hex).toString();
    }
}
