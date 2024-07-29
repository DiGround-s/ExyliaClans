package net.diground.exylia.cache;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardCache {
    private final ExyliaClans plugin;
    private Map<String, List<Integer>> cachedClanIds;
    private long lastUpdateTime;

    public LeaderboardCache(ExyliaClans plugin) {
        this.plugin = plugin;
        this.cachedClanIds = new HashMap<>();
        this.lastUpdateTime = 0;
        startCacheUpdateTask();
    }

    private void startCacheUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateCache();
            }
        }.runTaskTimer(plugin, 0, 20L * 60 * plugin.getConfig().getInt("leaderboard.refresh-time"));
    }

    private void updateCache() {
        long start = System.currentTimeMillis();
        List<Integer> clanIds = ClanUtils.getAllClansIds(plugin);
        cachedClanIds.clear();
        cachedClanIds.put("all", clanIds);
        lastUpdateTime = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        Bukkit.getLogger().info("Caché de leaderboard actualizado en " + (end - start) + "ms");
    }

    public List<Integer> getCachedClanIds(String filter) {
        if (System.currentTimeMillis() - lastUpdateTime > 20 * 60 * 5 * 1000) {
            updateCache();
        }
        return cachedClanIds.getOrDefault(filter, cachedClanIds.get("all"));
    }
}