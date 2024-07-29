package net.diground.exylia.managers;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClanChestManager {
    private final ExyliaClans plugin;
    private final Map<Integer, Inventory> clanChests = new HashMap<>();
    private final Map<UUID, Integer> playerChestMap = new HashMap<>();

    public ClanChestManager(ExyliaClans plugin) {
        this.plugin = plugin;
    }

    public void openClanChest(Player player, int clanId) {
        Inventory chest = clanChests.computeIfAbsent(clanId, id -> {
            Inventory newChest = Bukkit.createInventory(null, 54, "Clan Chest");
            Map<Integer, ItemStack> chestContents = ClanUtils.getClanChest(plugin, clanId);
            for (Map.Entry<Integer, ItemStack> entry : chestContents.entrySet()) {
                newChest.setItem(entry.getKey(), entry.getValue());
            }
            return newChest;
        });

        playerChestMap.put(player.getUniqueId(), clanId);
        player.openInventory(chest);
    }

    public void closeClanChest(Player player) {
        UUID playerUUID = player.getUniqueId();
        Integer clanId = playerChestMap.remove(playerUUID); // Use remove to get and delete
        if (clanId != null) {
            Inventory chest = clanChests.get(clanId);
            if (chest != null) {
                Map<Integer, ItemStack> chestContents = new HashMap<>();
                for (int i = 0; i < chest.getSize(); i++) {
                    ItemStack item = chest.getItem(i);
                    if (item != null) {
                        chestContents.put(i, item);
                    }
                }
                ClanUtils.saveClanChest(plugin, clanId, chestContents);
            } else {
            }
        } else {
        }
    }
}
