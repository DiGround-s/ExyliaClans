package net.diground.exylia.listeners;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.commands.base.ClanBaseCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class TeleportListener implements Listener {

    private final ExyliaClans plugin;
    private final Player player;
    private final Location startLocation;
    private final ClanBaseCommand clanBaseCommand;
    private boolean isUnregistered = false;

    public TeleportListener(ExyliaClans plugin, Player player, Location startLocation, ClanBaseCommand clanBaseCommand) {
        this.plugin = plugin;
        this.player = player;
        this.startLocation = startLocation;
        this.clanBaseCommand = clanBaseCommand;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getPlayer().equals(player) && !isUnregistered) {
            if (hasMoved(startLocation, event.getTo())) {
                cancelTeleport();
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity().equals(player) && !isUnregistered) {
            cancelTeleport();
        }
    }

    private boolean hasMoved(Location from, Location to) {
        return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ();
    }

    private void cancelTeleport() {
        clanBaseCommand.cancelTeleport(player.getUniqueId());
        unregister();
    }

    public void unregister() {
        if (!isUnregistered) {
            HandlerList.unregisterAll(this);
            isUnregistered = true;
        }
    }
}