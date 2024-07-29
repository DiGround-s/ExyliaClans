package net.diground.exylia.managers;

import net.diground.exylia.ExyliaClans;

import java.sql.*;

public class MigrationManager {

    private final ExyliaClans plugin;
    private Connection oldConnection;
    private Connection newConnection;

    public MigrationManager(ExyliaClans plugin) {
        this.plugin = plugin;
        oldConnection = createOldConnection();
        newConnection = plugin.getConnection();
    }

    private Connection createOldConnection() {
        String migrationType = plugin.getConfig().getString("migration.type");
        if ("mysql".equalsIgnoreCase(migrationType)) {
            String host = plugin.getConfig().getString("migration.mysql.host");
            int port = plugin.getConfig().getInt("migration.mysql.port");
            String database = plugin.getConfig().getString("migration.mysql.database");
            String user = plugin.getConfig().getString("migration.mysql.user");
            String password = plugin.getConfig().getString("migration.mysql.password");

            try {
                return DriverManager.getConnection(
                        "jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not connect to old MySQL database: " + e.getMessage());
            }
        } else {
            // Configuración para SQLite
            String dbFile = plugin.getDataFolder() + "/advanced_clans.db";
            try {
                return DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not connect to old SQLite database: " + e.getMessage());
            }
        }
        return null;
    }

    public void migrateData(ExyliaClans plugin) {
        try {
            if (oldConnection == null || newConnection == null) {
                plugin.getLogger().severe("Migration connections are not established.");
                return;
            }

            migrateClans();
            migratePlayers();
        } catch (SQLException e) {
            plugin.getLogger().severe("Migration failed: " + e.getMessage());
        }
    }

    private void migrateClans() throws SQLException {
        Statement oldStatement = oldConnection.createStatement();
        Statement newStatement = newConnection.createStatement();

        ResultSet rs = oldStatement.executeQuery("SELECT * FROM clans");
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            int kills = rs.getInt("kills");
            int deaths = rs.getInt("deaths");
            int exp = rs.getInt("exp");
            int level = rs.getInt("level");

            // Verificar si el nombre del clan ya existe en la nueva base de datos
            ResultSet clanCheck = newStatement.executeQuery("SELECT id FROM clans WHERE name = '" + name + "'");
            if (!clanCheck.next()) {
                newStatement.executeUpdate("INSERT INTO clans (id, name, kills, deaths, exp, level, prefix, created_date) VALUES (" + id + ", '" + name + "', " + kills + ", " + deaths + ", " + exp + ", " + level + ", '" + name + "', CURRENT_TIMESTAMP)");
            } else {
                plugin.getLogger().warning("Clan name '" + name + "' already exists. Skipping clan.");
            }
            clanCheck.close();
        }

        ResultSet rsBases = oldStatement.executeQuery("SELECT * FROM clans_bases");
        while (rsBases.next()) {
            double x = rsBases.getDouble("x");
            double y = rsBases.getDouble("y");
            double z = rsBases.getDouble("z");
            double yaw = rsBases.getDouble("yaw");
            double pitch = rsBases.getDouble("pitch");
            String world = rsBases.getString("world");
            int clanId = rsBases.getInt("clan_id");

            newStatement.executeUpdate("UPDATE clans SET base_x = " + x + ", base_y = " + y + ", base_z = " + z + ", base_yaw = " + yaw + ", base_pitch = " + pitch + ", base_world = '" + world + "' WHERE id = " + clanId);
        }

        oldStatement.close();
        newStatement.close();
    }


    private void migratePlayers() throws SQLException {
        Statement oldStatement = oldConnection.createStatement();
        Statement newStatement = newConnection.createStatement();

        ResultSet rs = oldStatement.executeQuery("SELECT * FROM players");
        while (rs.next()) {
            String uuid = rs.getString("uuid");
            String name = rs.getString("name");
            int clanId = rs.getInt("clan_id");
            int kills = rs.getInt("kills");
            int deaths = rs.getInt("deaths");
            int ranks = rs.getInt("ranks");

            int userRankId;
            switch (ranks) {
                case 2:
                    userRankId = 0;
                    break;
                case 3:
                    userRankId = 1;
                    break;
                case 1:
                    userRankId = 2;
                    break;
                case 0:
                    userRankId = 3;
                    break;
                default:
                    userRankId = 3;
                    break;
            }

            // Verificar si el UUID ya existe en la nueva base de datos
            ResultSet playerCheck = newStatement.executeQuery("SELECT uuid FROM players WHERE uuid = '" + uuid + "'");
            if (!playerCheck.next()) {
                newStatement.executeUpdate("INSERT INTO players (uuid, name, clan_id, kills, deaths, user_rank_id, join_date) VALUES ('" + uuid + "', '" + name + "', " + clanId + ", " + kills + ", " + deaths + ", " + userRankId + ", CURRENT_TIMESTAMP)");
            } else {
                plugin.getLogger().warning("Player UUID '" + uuid + "' already exists. Skipping player.");
            }
            playerCheck.close();
        }

        oldStatement.close();
        newStatement.close();
    }
}
