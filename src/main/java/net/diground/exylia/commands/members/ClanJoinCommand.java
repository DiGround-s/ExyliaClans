package net.diground.exylia.commands.members;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClanJoinCommand {
    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ClanJoinCommand(ExyliaClans plugin) {
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

        if (args.length == 1) {
            return joinSingleClan(player, playerUUID);
        } else if (args.length == 2) {
            return joinSpecificClan(player, playerUUID, args[1]);
        } else {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("usage_join")));
            return true;
        }
    }

    private boolean joinSingleClan(Player player, String playerUUID) {
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT clans.id, clans.name FROM invites JOIN clans ON invites.clan_id = clans.id WHERE invites.player_uuid = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, playerUUID);
            ResultSet rs = stmt.executeQuery();

            List<String> invitedClans = new ArrayList<>();
            int clanId = -1;
            String clanName = null;

            while (rs.next()) {
                invitedClans.add(rs.getString("name"));
                if (clanId == -1) {
                    clanId = rs.getInt("id");
                    clanName = rs.getString("name");
                }
            }

            if (invitedClans.size() == 0) {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_invites")));
            } else if (invitedClans.size() == 1) {
                joinClan(player, playerUUID, clanId, clanName);
            } else {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("multiple_invites")));
                for (String name : invitedClans) {
                    player.sendMessage(ChatUtils.translateColors("&6- " + name));
                }
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("join_specific")));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan invites: " + e.getMessage());
        }

        return true;
    }

    private boolean joinSpecificClan(Player player, String playerUUID, String clanName) {
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT clans.id FROM invites JOIN clans ON invites.clan_id = clans.id WHERE invites.player_uuid = ? AND clans.name = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, playerUUID);
            stmt.setString(2, clanName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int clanId = rs.getInt("id");
                joinClan(player, playerUUID, clanId, clanName);
            } else {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("invite_not_found").replace("%clan%", clanName)));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan invite: " + e.getMessage());
        }

        return true;
    }

    private void joinClan(Player player, String playerUUID, int clanId, String clanName) {
        try {
            Connection connection = plugin.getConnection();
            String deleteInvitesSQL = "DELETE FROM invites WHERE player_uuid = ?";
            PreparedStatement deleteStmt = connection.prepareStatement(deleteInvitesSQL);
            deleteStmt.setString(1, playerUUID);
            deleteStmt.executeUpdate();
            deleteStmt.close();

            String insertPlayerSQL = "INSERT INTO players (uuid, name, clan_id, user_rank_id, join_date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement insertStmt = connection.prepareStatement(insertPlayerSQL);
            insertStmt.setString(1, playerUUID);
            insertStmt.setString(2, player.getName());
            insertStmt.setInt(3, clanId);
            insertStmt.setInt(4, plugin.getConfig().getInt("default.rank_id"));
            insertStmt.executeUpdate();
            insertStmt.close();

            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("join_success").replace("%clan%", clanName)));

            notificationManager.notifyNewMember(clanId, player.getName());
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not join clan: " + e.getMessage());
        }
    }
}
