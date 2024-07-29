package net.diground.exylia.managers;

import java.util.List;

public class Rank {
    public static final int LEADER_RANK = 0;
    private final String name;
    private final String displayName;
    private final String prefix;
    private final int rankOrder;
    private final int id;
    private final List<String> permissions;

    public Rank(String name, String displayName, String prefix, int rankOrder, int rankId, List<String> permissions) {
        this.name = name;
        this.displayName = displayName;
        this.prefix = prefix;
        this.rankOrder = rankOrder;
        this.id = rankId;
        this.permissions = permissions;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getRankOrder() {
        return rankOrder;
    }

    public int getId() {
        return id;
    }

    public List<String> getPermissions() {
        return permissions;
    }

}
