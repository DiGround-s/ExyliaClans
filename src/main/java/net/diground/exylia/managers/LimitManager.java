package net.diground.exylia.managers;

import net.diground.exylia.ExyliaClans;
import org.bukkit.configuration.file.FileConfiguration;

public class LimitManager {
    private final ExyliaClans plugin;
    private int maxMembers;
    private int maxAlliances;
    private int maxEnemies;

    public LimitManager(ExyliaClans plugin) {
        this.plugin = plugin;
        loadLimits();
    }

    private void loadLimits() {
        FileConfiguration config = plugin.getConfig();
        maxMembers = config.getInt("limits.max_members", 20);
        maxAlliances = config.getInt("limits.max_alliances", 5);
        maxEnemies = config.getInt("limits.max_enemies", 5);
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public int getMaxAlliances() {
        return maxAlliances;
    }

    public int getMaxEnemies() {
        return maxEnemies;
    }
}
