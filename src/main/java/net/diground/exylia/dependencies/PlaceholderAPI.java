package net.diground.exylia.dependencies;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import net.diground.exylia.ExyliaClans;
import net.diground.exylia.utils.ClanUtils;
import net.diground.exylia.utils.PlayerUtils;
import org.bukkit.entity.Player;

import java.util.*;

public class PlaceholderAPI extends PlaceholderExpansion implements Relational {

    private final ExyliaClans plugin;
    private final String allyColor;
    private final String enemyColor;
    private final String memberColor;
    private final String noneColor;


    public PlaceholderAPI(ExyliaClans plugin) {
        this.plugin = plugin;
        this.allyColor = plugin.getConfig().getString("relational.colors.ally");
        this.enemyColor = plugin.getConfig().getString("relational.colors.enemy");
        this.memberColor = plugin.getConfig().getString("relational.colors.member");
        this.noneColor = plugin.getConfig().getString("relational.colors.none");
    }

    @Override
    public String getIdentifier() {
        return "clan";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {

        if (player == null) {
            return "";
        }

        int clanId = ClanUtils.getPlayerClanId(plugin, player.getUniqueId().toString());
        String clanName = plugin.getClanManager().getClanNameByPlayer(player.getUniqueId());







        /////////////////// CLAN INFO /////////////////
        if (identifier.equals("name")) { //good
            if (ClanUtils.isPlayerInClan(plugin, player.getUniqueId().toString())) {
                return clanName;
            } else {
                return plugin.getCustomPlaceholderMessage("no_clan_name");
            }
        }

        if (identifier.equals("prefix")) {
            if (ClanUtils.isPlayerInClan(plugin, player.getUniqueId().toString())) {
                return ClanUtils.getClanPrefix(plugin, clanId);
            } else {
                return plugin.getCustomPlaceholderMessage("no_clan_name_colored");
            }
        }

        if (identifier.equals("clan_id")) { //good
            return clanId + "";
        }
        ////////////////// END CLAN INFO /////////////////








        ///////////////// CLAN MEMBERS /////////////////
        if (identifier.equals("list_members")) { //good
            return ClanUtils.getClanOfflinePlayerMemberNames(plugin, clanId).toString();
        }
        if (identifier.equals("list_online_members")) { //good
            return ClanUtils.getOnlineClanMembers(plugin, clanId) + "";
        }
        if (identifier.equals("count_members")) { //good
            return ClanUtils.getMemberCount(plugin, clanId) + "";
        }
        if (identifier.equals("count_online_members")) { //good
            return ClanUtils.getOnlineClanMembersCount(plugin, clanId) + "";
        }
        if (identifier.equals("max_members")) { //good
            return plugin.getLimitManager().getMaxMembers() + "";
        }
        //////////////// END CLAN MEMBERS /////////////////





        /////////////// CLAN ENEMIES /////////////////
        if (identifier.equals("enemies")) { //good
            List<String> enemies = ClanUtils.getClanEnemies(plugin, clanId);
            if (enemies.isEmpty()) {
                return plugin.getCustomPlaceholderMessage("no_enemies");
            }
            return enemies.toString();
        }
        if (identifier.equals("count_enemies")) { //good
            return ClanUtils.getEnemyCount(plugin, clanId) + "";
        }
        if (identifier.equals("max_enemies")) {  //good
            return plugin.getLimitManager().getMaxEnemies() + "";
        }
        //////////////// END CLAN ENEMIES /////////////////








        /////////////// CLAN ALLIES /////////////////
        if (identifier.equals("allies")) {  //good
            List<String> allies = ClanUtils.getClanAllies(plugin, clanId);
            if (allies.isEmpty()) {
                return plugin.getCustomPlaceholderMessage("no_allies");
            }
            return allies.toString();
        }
        if (identifier.equals("count_allies")) { //good
            return ClanUtils.getAllianceCount(plugin, clanId) + "";
        }

        if (identifier.equals("max_allies")) { //good
            return plugin.getLimitManager().getMaxAlliances() + "";
        }
        //////////////// END CLAN ENEMIES /////////////////






        /////////////// CLAN STATS /////////////////
        if (identifier.equals("balance")) {
            return plugin.getClanManager().getClanBalance(player) + "";
        }
        Map<String, Integer> stats = ClanUtils.getClanStats(plugin, clanId);

        if (identifier.equals("kills")) {
            return stats.getOrDefault("kills", 0) + "";
        }
        if (identifier.equals("deaths")) {
            return stats.getOrDefault("deaths", 0) + "";
        }
        if (identifier.equals("kdr")) {
            return ClanUtils.getClanKDR(plugin, clanId) + "";
        }
        //////////////// END STATS /////////////////




        /////////////// PLAYER INFO /////////////////

        if (identifier.equals("is_leader")) {
            return ClanUtils.isPlayerClanLeader(plugin, player.getUniqueId().toString()) ? "true" : "false";
        }
        if (identifier.equals("is_in_clan")) {
            return plugin.getClanManager().isInClan(player) ? "true" : "false";
        }
        if (identifier.equals("player_rank")) {
            String playerRank = ClanUtils.getPlayerRank(plugin, player.getUniqueId().toString());
            return Objects.requireNonNullElse(playerRank, "null");
        }

        if (identifier.equals("player_actual_chat")) {
            String playerRank = ClanUtils.getPlayerActualChat(plugin, player.getUniqueId());
            return Objects.requireNonNullElse(playerRank, "null");
        }

        if (identifier.equals("player_kills")) {
            return PlayerUtils.getPlayerKills(plugin, player.getUniqueId().toString()) + "";
        }
        if (identifier.equals("player_deaths")) {
            return PlayerUtils.getPlayerDeaths(plugin, player.getUniqueId().toString()) + "";
        }
        if (identifier.equals("player_kdr")) {
            return PlayerUtils.getPlayerKDR(plugin, player.getUniqueId().toString()) + "";
        }

        ////////////// END PLAYER INFO /////////////////

        return "";
    }


    @Override
    public String onPlaceholderRequest(Player one, Player two, String identifier) {
        if (one != null && two != null) {
            if (identifier.equals("color")) {
                UUID oneUUID = one.getUniqueId();
                UUID twoUUID = two.getUniqueId();

                // Utilizar caché para mejorar el rendimiento
                if (ClanUtils.isAlly(plugin, oneUUID, twoUUID)) {
                    return allyColor;
                } else if (ClanUtils.isEnemy(plugin, oneUUID, twoUUID)) {
                    return enemyColor;
                } else if (ClanUtils.isMember(plugin, oneUUID, twoUUID)) {
                    return memberColor;
                } else {
                    return noneColor;
                }
            }
            return "";
        }
        return "";
    }
}
