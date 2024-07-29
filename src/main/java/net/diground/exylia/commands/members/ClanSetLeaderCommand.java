package net.diground.exylia.commands.members;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.Rank;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClanSetLeaderCommand {
    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;
    private final Map<UUID, Long> confirmSetLeader = new HashMap<>();

    public ClanSetLeaderCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("usage_setleader")));
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();
        String clanName = ClanUtils.getPlayerClan(plugin, playerUUID);


        if (clanName == null) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        if (!ClanUtils.isPlayerClanLeader(plugin, playerUUID)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_leader")));
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("player_not_found")));
            return true;
        }

        String targetUUID = targetPlayer.getUniqueId().toString();
        UUID playerId = player.getUniqueId();

        if (!confirmSetLeader.containsKey(playerId) || (System.currentTimeMillis() - confirmSetLeader.get(playerId) > 10000)) {
            confirmSetLeader.put(playerId, System.currentTimeMillis());
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("confirm_setleader")));
            return true;
        }

        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT user_rank_id FROM players WHERE uuid = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, targetUUID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int currentRank = rs.getInt("user_rank_id");
                Rank rank = plugin.getRankManager().getRankById(currentRank);

                if (rank != null && rank.getRankOrder() > 0) {
                    String updateRankSQL = "UPDATE players SET user_rank_id = 0 WHERE uuid = ?";
                    PreparedStatement updateStmt = connection.prepareStatement(updateRankSQL);
                    updateStmt.setString(1, targetUUID);
                    updateStmt.executeUpdate();
                    updateStmt.close();

                    Rank newRank = plugin.getRankManager().getRankByOrder(rank.getRankOrder() - 1);

                    if (newRank != null) {
                        int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID);
                        String demoteOldLeaderSQL = "UPDATE players SET user_rank_id = ? WHERE uuid = ?";
                        PreparedStatement demoteStmt = connection.prepareStatement(demoteOldLeaderSQL);
                        demoteStmt.setInt(1, newRank.getId());
                        demoteStmt.setString(2, playerUUID);
                        demoteStmt.executeUpdate();
                        demoteStmt.close();

                        player.sendMessage(ChatUtils.translateColors(plugin.getMessage("setleader_success").replace("%player%", targetPlayer.getName())));
                        targetPlayer.sendMessage(ChatUtils.translateColors(plugin.getMessage("promoted_to_leader")));
                        notificationManager.notifyNewLeader(clanId, targetPlayer.getName());
                    }
                }
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not set leader: " + e.getMessage());
        }

        return true;
    }
}
