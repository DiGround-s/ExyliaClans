package net.diground.exylia.listeners;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ConnectionListener(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerUUID = event.getPlayer().getUniqueId().toString();
        if (ClanUtils.isPlayerInClan(plugin, playerUUID)) {
            int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID);
            notificationManager.notifyMemberConnected(clanId, event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String playerUUID = event.getPlayer().getUniqueId().toString();
        if (ClanUtils.isPlayerInClan(plugin, playerUUID)) {
            int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID);
            notificationManager.notifyMemberDisconnected(clanId, event.getPlayer().getName());
        }
    }
}
