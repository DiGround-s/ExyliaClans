package net.diground.exylia.listeners;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    private final ExyliaClans plugin;

    public PlayerChatListener(ExyliaClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String playerUUID = event.getPlayer().getUniqueId().toString();
        String chatMode = plugin.getClanChatManager().getChatMode(event.getPlayer().getUniqueId());

        String message = event.getMessage();
        Component formattedMessage;

        switch (chatMode) {
            case "CLAN":
                event.setCancelled(true);
                if (!ClanUtils.isPlayerInClan(plugin, playerUUID)) {
                    event.getPlayer().sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
                    return;
                }
                String clanName = ClanUtils.getPlayerClan(plugin, playerUUID);
                String clanMessage = plugin.getMessage("chat_format.clan")
                        .replace("%player%", event.getPlayer().getName())
                        .replace("%message%", message)
                        .replace("%clan%", clanName);
                formattedMessage = MiniMessage.miniMessage().deserialize(clanMessage);
                plugin.getClanChatManager().sendClanMessage(event.getPlayer(), formattedMessage);
                break;

            case "ALLY":
                event.setCancelled(true);
                if (!ClanUtils.isPlayerInClan(plugin, playerUUID)) {
                    event.getPlayer().sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
                    return;
                }
                String playerClan = ClanUtils.getPlayerClan(plugin, playerUUID);
                String allyMessage = plugin.getMessage("chat_format.ally")
                        .replace("%player%", event.getPlayer().getName())
                        .replace("%message%", message)
                        .replace("%clan%", playerClan);
                formattedMessage = MiniMessage.miniMessage().deserialize(allyMessage);
                plugin.getClanChatManager().sendAllyMessage(event.getPlayer(), formattedMessage);
                break;

            case "GLOBAL":
                // Aquí podrías implementar lógica similar si decides mantener el chat global.
                break;
        }
    }


}
