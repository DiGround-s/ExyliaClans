package net.diground.exylia.utils;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.Rank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerUtils {


    public static Rank getCurrentRank(ExyliaClans plugin, String targetUUID) throws SQLException {
        Connection connection = plugin.getConnection();
        String query = "SELECT user_rank_id FROM players WHERE uuid = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, targetUUID);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int currentRank = rs.getInt("user_rank_id");
            rs.close();
            stmt.close();
            return plugin.getRankManager().getRankById(currentRank);
        }

        rs.close();
        stmt.close();
        return null;
    }

    public static boolean updateRank(ExyliaClans plugin, String targetUUID, int newRankId) throws SQLException {
        Connection connection = plugin.getConnection();
        String updateRankSQL = "UPDATE players SET user_rank_id = ? WHERE uuid = ?";
        PreparedStatement updateStmt = connection.prepareStatement(updateRankSQL);
        updateStmt.setInt(1, newRankId);
        updateStmt.setString(2, targetUUID);
        int rowsUpdated = updateStmt.executeUpdate();
        updateStmt.close();
        return rowsUpdated > 0;
    }

    public static boolean kickPlayer(ExyliaClans plugin, String targetUUID) throws SQLException {
        Connection connection = plugin.getConnection();
        String deletePlayerSQL = "DELETE FROM players WHERE uuid = ?";
        PreparedStatement deleteStmt = connection.prepareStatement(deletePlayerSQL);
        deleteStmt.setString(1, targetUUID);
        int rowsDeleted = deleteStmt.executeUpdate();
        deleteStmt.close();
        return rowsDeleted > 0;
    }




    public static void addPlayerKill(ExyliaClans plugin, int clanId, String playerUUID) {
        try {
            Connection connection = plugin.getConnection();
            String query = "UPDATE players SET kills = kills + 1 WHERE clan_id = ? AND uuid = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clanId);
            statement.setString(2, playerUUID);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not add player kill: " + e.getMessage());
        }
    }

    public static void removePlayerKill(ExyliaClans plugin, int clanId, String playerUUID) {
        try {
            Connection connection = plugin.getConnection();
            String query = "UPDATE players SET kills = GREATEST(kills - 1, 0) WHERE clan_id = ? AND uuid = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clanId);
            statement.setString(2, playerUUID);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not remove player kill: " + e.getMessage());
        }
    }

    public static void addPlayerDeath(ExyliaClans plugin, int clanId, String playerUUID) {
        try {
            Connection connection = plugin.getConnection();
            String query = "UPDATE players SET deaths = deaths + 1 WHERE clan_id = ? AND uuid = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clanId);
            statement.setString(2, playerUUID);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not add player death: " + e.getMessage());
        }
    }

    public static void removePlayerDeath(ExyliaClans plugin, int clanId, String playerUUID) {
        try {
            Connection connection = plugin.getConnection();
            String query = "UPDATE players SET deaths = GREATEST(deaths - 1, 0) WHERE clan_id = ? AND uuid = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clanId);
            statement.setString(2, playerUUID);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not remove player death: " + e.getMessage());
        }
    }

    public static int getPlayerKills(ExyliaClans plugin, String playerUUID) {
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT kills FROM players WHERE uuid = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, playerUUID);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int kills = rs.getInt("kills");
                rs.close();
                statement.close();
                return kills;
            }
            rs.close();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not get player kills: " + e.getMessage());
        }
        return 0;
    }

    public static int getPlayerDeaths(ExyliaClans plugin, String playerUUID) {
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT deaths FROM players WHERE uuid = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, playerUUID);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int deaths = rs.getInt("deaths");
                rs.close();
                statement.close();
                return deaths;
            }
            rs.close();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not get player deaths: " + e.getMessage());
        }
        return 0;
    }

    public static double getPlayerKDR(ExyliaClans plugin, String playerUUID) {
        int kills = getPlayerKills(plugin, playerUUID);
        int deaths = getPlayerDeaths(plugin, playerUUID);
        double kdr = deaths == 0 ? kills : (double) kills / deaths;
        kdr = Math.round(kdr * 10.0) / 10.0;
        return kdr;
    }

}
