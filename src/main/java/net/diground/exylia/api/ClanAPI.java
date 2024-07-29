package net.diground.exylia.api;

import java.util.UUID;

public interface ClanAPI {
    boolean isAlly(UUID player1, UUID player2);

    boolean isEnemy(UUID player1, UUID player2);

    boolean isMember(UUID player1, UUID player2);

    boolean isPlayerInClan(UUID player);

    int getPlayerClanId(UUID player);

    String getClanNameByPlayer(UUID player);

    String getClanNameByClanId(int clanId);
}
