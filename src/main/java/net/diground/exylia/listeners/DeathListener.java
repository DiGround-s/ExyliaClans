package net.diground.exylia.listeners;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ClanUtils;
import net.diground.exylia.utils.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public DeathListener(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deceased = event.getEntity();
        Player killer = deceased.getKiller();

        String deceasedUUID = deceased.getUniqueId().toString();
        int deceasedClanId = ClanUtils.getPlayerClanId(plugin, deceasedUUID);

        if (killer != null) {
            String killerUUID = killer.getUniqueId().toString();
            int killerClanId = ClanUtils.getPlayerClanId(plugin, killerUUID);

            ClanUtils.addClanKill(plugin, killerClanId);
            ClanUtils.addClanDeath(plugin, deceasedClanId);
            PlayerUtils.addPlayerKill(plugin, killerClanId, killerUUID);
            PlayerUtils.addPlayerDeath(plugin, deceasedClanId, deceasedUUID);
        } else {
            ClanUtils.addClanDeath(plugin, deceasedClanId);
            PlayerUtils.addPlayerDeath(plugin, deceasedClanId, deceasedUUID);
        }

        if (ClanUtils.isPlayerInClan(plugin, deceasedUUID)) {
            notificationManager.notifyMemberDied(deceasedClanId, deceased.getName());
        }
    }
}
