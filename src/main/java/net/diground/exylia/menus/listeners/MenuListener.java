package net.diground.exylia.menus.listeners;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.menus.model.InventoryPlayer;
import net.diground.exylia.menus.model.InventorySection;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

import static net.diground.exylia.ExyliaClans.refreshTasks;
import static net.diground.exylia.menus.managers.MainMenuManager.allowedBannerSlot;

public class MenuListener implements Listener {

    private final ExyliaClans plugin;

    public MenuListener(ExyliaClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        InventoryPlayer inventoryPlayer = plugin.getMenuManager().getInventoryPlayer(player);
        if (inventoryPlayer != null) {
            if (inventoryPlayer.getSection() == InventorySection.BANNER) {
                if (event.getCurrentItem() != null) {
                    if (event.getSlot() == allowedBannerSlot || event.getClickedInventory() == player.getOpenInventory().getBottomInventory()) {
                        if (!event.getCurrentItem().getType().name().contains("_BANNER")) {
                            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_banners")));
                            event.setCancelled(true);
                        } else {
                            plugin.getMenuManager().inventoryClick(inventoryPlayer, event.getSlot(), event.getClick());
                        }
                    } else {
                        event.setCancelled(true);
                        plugin.getMenuManager().inventoryClick(inventoryPlayer, event.getSlot(), event.getClick());
                    }
                }
            } else {
                event.setCancelled(true);
                if (event.getCurrentItem() != null && event.getClickedInventory() == player.getOpenInventory().getTopInventory()) {
                    plugin.getMenuManager().inventoryClick(inventoryPlayer, event.getSlot(), event.getClick());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();
        InventoryPlayer inventoryPlayer = plugin.getMenuManager().getInventoryPlayer(player);
        Inventory inventory = event.getInventory();
        ItemStack item = inventory.getItem(allowedBannerSlot);
        if (inventoryPlayer != null) {
            if (inventoryPlayer.getSection() == InventorySection.BANNER) {
                if (item != null && item.getType().name().contains("_BANNER")) {
                    Bukkit.getLogger().info("Uploaded banner for player " + player.getName());
                    ClanUtils.uploadClanBanner(plugin, inventoryPlayer.getClanId(), item);
                } else {
                    ClanUtils.removeClanBanner(plugin, inventoryPlayer.getClanId());
                }
            }
            if (refreshTasks.containsKey(playerId)) {
                refreshTasks.get(playerId).cancel();
                refreshTasks.remove(playerId);
            }
            plugin.getMenuManager().removePlayer(player);
        }
    }

}
