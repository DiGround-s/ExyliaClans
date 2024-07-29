package net.diground.exylia.managers;

import net.diground.exylia.ExyliaClans;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankManager {

    private final ExyliaClans plugin;
    private final Map<String, Rank> ranks = new HashMap<>();

    public RankManager(ExyliaClans plugin) {
        this.plugin = plugin;
        loadRanks();
    }

    public void loadRanks() {
        File ranksFile = new File(plugin.getDataFolder(), "ranks.yml");
        if (!ranksFile.exists()) {
            plugin.saveResource("ranks.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(ranksFile);
        for (String key : config.getConfigurationSection("ranks").getKeys(false)) {
            String name = config.getString("ranks." + key + ".name");
            String displayName = config.getString("ranks." + key + ".display-name");
            String prefix = config.getString("ranks." + key + ".prefix");
            int rankOrder = config.getInt("ranks." + key + ".rankOrder");
            int id = config.getInt("ranks." + key + ".id");
            List<String> permissions = config.getStringList("ranks." + key + ".permissions");

            Rank rank = new Rank(name, displayName, prefix, rankOrder, id, permissions);
            ranks.put(name.toLowerCase(), rank);
        }
    }

    public static String getPrefix(ExyliaClans plugin, int id) {
        return plugin.getRankManager().getRankById(id).getPrefix();
    }
    public static String getName(ExyliaClans plugin, int id) {
        return plugin.getRankManager().getRankById(id).getName();
    }

    public Rank getRank(String name) {
        return ranks.get(name.toLowerCase());
    }


    public Rank getRankById(int id) {
        for (Rank rank : ranks.values()) {
            if (rank.getId() == id) {
                return rank;
            }
        }
        return null;
    }

    public Rank getRankByOrder(int order) {
        for (Rank rank : ranks.values()) {
            if (rank.getRankOrder() == order) {
                return rank;
            }
        }
        return null;
    }

    public Map<String, Rank> getRanks() {
        return ranks;
    }

    public void saveRanks() {
        File ranksFile = new File(plugin.getDataFolder(), "ranks.yml");
        FileConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, Rank> entry : ranks.entrySet()) {
            String key = entry.getKey();
            Rank rank = entry.getValue();

            config.set("ranks." + key + ".name", rank.getName());
            config.set("ranks." + key + ".display-name", rank.getDisplayName());
            config.set("ranks." + key + ".prefix", rank.getPrefix());
            config.set("ranks." + key + ".rankOrder", rank.getRankOrder());
            config.set("ranks." + key + ".permissions", rank.getPermissions());
        }

        try {
            config.save(ranksFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save ranks.yml: " + e.getMessage());
        }
    }

    public int getLowestRankOrder() {
        return ranks.values().stream().mapToInt(Rank::getRankOrder).max().orElse(0);
    }
}
