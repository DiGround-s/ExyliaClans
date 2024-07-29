package net.diground.exylia.menus.model;

import org.bukkit.entity.Player;

public class InventoryPlayer {
    private int clanId;
    private int page;
    private String filter;
    private Player player;
    private InventorySection section;

    public InventoryPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public InventorySection getSection() {
        return section;
    }

    public void setSection(InventorySection section) {
        this.section = section;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }


    public int getClanId() {
        return clanId;
    }

    public void setClanId(int clanId) {
        this.clanId = clanId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
