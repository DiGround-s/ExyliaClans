package net.diground.exylia.managers;

import net.diground.exylia.ExyliaClans;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ClanManager {

    private final ExyliaClans plugin;

    public ClanManager(ExyliaClans plugin) {
        this.plugin = plugin;
    }

    public String getClanNameByPlayer(UUID playerUUID) {
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT clans.name FROM players JOIN clans ON players.clan_id = clans.id WHERE players.uuid = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, playerUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("name");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan name: " + e.getMessage());
        }
        return null;
    }

    public boolean isPlayerInClan(UUID playerUUID, String clanName) {
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT COUNT(*) FROM players JOIN clans ON players.clan_id = clans.id WHERE players.uuid = ? AND clans.name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, playerUUID.toString());
            statement.setString(2, clanName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check if player is in clan: " + e.getMessage());
        }
        return false;
    }

    public void addClanBalance(Player player, double amount) {
        try {
            Connection connection = plugin.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE clans SET bank_balance = bank_balance + ? WHERE id = (SELECT clan_id FROM players WHERE uuid = ?)");
            statement.setDouble(1, amount);
            statement.setString(2, player.getUniqueId().toString());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not update clan balance: " + e.getMessage());
        }
    }

    public void subtractClanBalance(Player player, double amount) {
        try {
            Connection connection = plugin.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE clans SET bank_balance = bank_balance - ? WHERE id = (SELECT clan_id FROM players WHERE uuid = ?)");
            statement.setDouble(1, amount);
            statement.setString(2, player.getUniqueId().toString());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not update clan balance: " + e.getMessage());
        }
    }

    public double getClanBalance(Player player) {
        double balance = 0;
        try {
            Connection connection = plugin.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT bank_balance FROM clans WHERE id = (SELECT clan_id FROM players WHERE uuid = ?)");
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                balance = resultSet.getDouble("bank_balance");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan balance: " + e.getMessage());
        }
        return balance;
    }
    public boolean isInClan(Player player) {
        boolean inClan = false;
        try {
            Connection connection = plugin.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT clan_id FROM players WHERE uuid = ?");
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                inClan = resultSet.getInt("clan_id") != 0;
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check if player is in a clan: " + e.getMessage());
        }
        return inClan;
    }

}
