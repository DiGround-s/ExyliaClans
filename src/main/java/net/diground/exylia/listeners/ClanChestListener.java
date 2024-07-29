package net.diground.exylia.listeners;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.ClanChestManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class ClanChestListener implements Listener {
    private final ClanChestManager chestManager;

    public ClanChestListener(ExyliaClans plugin) {
        this.chestManager = new ClanChestManager(plugin);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Clan Chest")) {
            Player player = (Player) event.getPlayer();
            chestManager.closeClanChest(player);
            Bukkit.getLogger().info("Closed clan chest for player " + player.getName());
        }
    }
}
