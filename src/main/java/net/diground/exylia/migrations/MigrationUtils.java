package net.diground.exylia.migrations;

import net.diground.exylia.ExyliaClans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class MigrationUtils {

    public static void addClan(ExyliaClans plugin, String name) {
        try (Connection conn = plugin.getConnection();
             Statement stmt = conn.createStatement()) {
            String query = "INSERT INTO clans (name) VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void setClanKills(ExyliaClans plugin, int clanId, int kills) {
        try (Connection conn = plugin.getConnection();
             Statement stmt = conn.createStatement()) {
            String query = "UPDATE clans SET kills = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, kills);
            pstmt.setInt(2, clanId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void setClanDeaths(ExyliaClans plugin, int clanId, int deaths) {
        try (Connection conn = plugin.getConnection();
             Statement stmt = conn.createStatement()) {
            String query = "UPDATE clans SET deaths = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, deaths);
            pstmt.setInt(2, clanId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
