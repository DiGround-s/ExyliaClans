package net.diground.exylia.migrations;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.utils.ClanUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdvancedClansMigration {

    public static void migrate(ExyliaClans plugin) {
        try (Connection conn = plugin.getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "SELECT * FROM advancedclans_clans";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int kills = rs.getInt("kills");
                int deaths = rs.getInt("deaths");

                // Insertar en la nueva base de datos
                MigrationUtils.addClan(plugin, name);
                int newClanId = ClanUtils.getClanIdByName(plugin, name);
                MigrationUtils.setClanKills(plugin, newClanId, kills);
                MigrationUtils.setClanDeaths(plugin, newClanId, deaths);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
