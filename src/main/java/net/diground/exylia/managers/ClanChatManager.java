package net.diground.exylia.managers;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.utils.ClanUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClanChatManager {
    private final ExyliaClans plugin;
    private final Set<UUID> clanChatPlayers;
    private final Map<UUID, String> playerChatModes;

    public ClanChatManager(ExyliaClans plugin) {
        this.plugin = plugin;
        this.clanChatPlayers = new HashSet<>();
        this.playerChatModes = new HashMap<>();
    }

    public String toggleChatMode(UUID playerUUID) {
        String currentMode = playerChatModes.getOrDefault(playerUUID, "GLOBAL");
        String newMode;
        switch (currentMode) {
            case "GLOBAL":
                newMode = "CLAN";
                break;
            case "CLAN":
                newMode = "ALLY";
                break;
            case "ALLY":
                newMode = "GLOBAL";
                break;
            default:
                newMode = "GLOBAL";
                break;
        }
        playerChatModes.put(playerUUID, newMode);
        return newMode;
    }

    public String getChatMode(UUID playerUUID) {
        return playerChatModes.getOrDefault(playerUUID, "GLOBAL");
    }

    public void sendClanMessage(Player sender, Component message) {
        String clanName = plugin.getClanManager().getClanNameByPlayer(sender.getUniqueId());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getClanManager().isPlayerInClan(player.getUniqueId(), clanName)) {
                player.sendMessage(message);
            }
        }
    }


    public void sendAllyMessage(Player sender, Component message) {
        String playerClan = plugin.getClanManager().getClanNameByPlayer(sender.getUniqueId());
        for (Player player : Bukkit.getOnlinePlayers()) {
            String clanName = plugin.getClanManager().getClanNameByPlayer(player.getUniqueId());
            int playerClanId = ClanUtils.getClanIdByName(plugin, playerClan);
            int targetClanId = ClanUtils.getClanIdByName(plugin, clanName);
            if (playerClan.equals(clanName) || ClanUtils.areClansAllied(plugin, playerClanId, targetClanId)) {
                player.sendMessage(message);
            }
        }
    }


    public void sendGlobalMessage(Player sender, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }
}
