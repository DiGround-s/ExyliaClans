package net.diground.exylia.api;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.utils.ClanUtils;

import java.util.UUID;

public class ClanAPIImpl implements ClanAPI {

    private final ExyliaClans plugin;
    public ClanAPIImpl(ExyliaClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isAlly(UUID player1, UUID player2) {
        return ClanUtils.isAlly(plugin, player1, player2);
    }

    @Override
    public boolean isEnemy(UUID player1, UUID player2) {
        return ClanUtils.isEnemy(plugin, player1, player2);
    }

    @Override
    public boolean isMember(UUID player1, UUID player2) {
        return ClanUtils.isMember(plugin, player1, player2);
    }

    @Override
    public boolean isPlayerInClan(UUID player) {
        return ClanUtils.isPlayerInClan(plugin, player.toString());
    }

    @Override
    public int getPlayerClanId(UUID player) {
        return ClanUtils.getPlayerClanId(plugin, player.toString());
    }

    @Override
    public String getClanNameByPlayer(UUID player) {
        return ClanUtils.getPlayerClan(plugin, player.toString());
    }

    @Override
    public String getClanNameByClanId(int clanId) {
        return ClanUtils.getClanNameById(plugin, clanId);
    }

}
