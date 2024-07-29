package net.diground.exylia.commands.members;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClanLeaveCommand {
    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ClanLeaveCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();

        if (!ClanUtils.isPlayerInClan(plugin, playerUUID)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        if (ClanUtils.isPlayerClanLeader(plugin, playerUUID)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("cannot_leave_as_leader")));
            return true;
        }

        try {
            Connection connection = plugin.getConnection();
            int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID);

            String deletePlayerSQL = "DELETE FROM players WHERE uuid = ?";
            PreparedStatement deleteStmt = connection.prepareStatement(deletePlayerSQL);
            deleteStmt.setString(1, playerUUID);
            deleteStmt.executeUpdate();
            deleteStmt.close();

            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("leave_success")));
            notificationManager.notifyMemberLeft(clanId, player.getName());

            String notifyLeaderSQL = "SELECT uuid FROM players WHERE clan_id = ? AND user_rank_id = 0";
            PreparedStatement notifyStmt = connection.prepareStatement(notifyLeaderSQL);
            notifyStmt.setInt(1, clanId);
            ResultSet rs = notifyStmt.executeQuery();

            if (rs.next()) {
                String leaderUUID = rs.getString("uuid");
                Player leader = plugin.getServer().getPlayer(leaderUUID);
                if (leader != null && leader.isOnline()) {
                    leader.sendMessage(ChatUtils.translateColors(plugin.getMessage("member_left").replace("%player%", player.getName())));
                }
            }

            rs.close();
            notifyStmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not leave clan: " + e.getMessage());
        }

        return true;
    }
}
