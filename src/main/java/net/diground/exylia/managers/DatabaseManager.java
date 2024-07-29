package net.diground.exylia.managers;

import net.diground.exylia.ExyliaClans;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;

public class DatabaseManager {

    private final ExyliaClans plugin;
    private Connection connection;

    public DatabaseManager(ExyliaClans plugin) {
        this.plugin = plugin;
    }

    public void setupDatabase() {
        FileConfiguration config = plugin.getConfig();
        String databaseType = config.getString("database.type");

        try {
            if ("mysql".equalsIgnoreCase(databaseType)) {
                String host = config.getString("database.mysql.host");
                int port = config.getInt("database.mysql.port");
                String database = config.getString("database.mysql.database");
                String user = config.getString("database.mysql.user");
                String password = config.getString("database.mysql.password");

                connection = DriverManager.getConnection(
                        "jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
                plugin.getLogger().info("Connected to MySQL database.");
            } else {
                String dbFile = plugin.getDataFolder() + "/clans.db";
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
                plugin.getLogger().info("Connected to SQLite database.");
            }
            initializeDatabase(databaseType);
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not connect to the database: " + e.getMessage());
            connection = null;
            plugin.getLogger().severe("-!-------------------------------------------------------!-");
            plugin.getLogger().severe("CHECK YOUR DATABASE CONFIGURATION, THE DATABASE CAN NOT BE CONNECTED.");
            plugin.getLogger().severe("-!-------------------------------------------------------!-");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public void initializeDatabase(String databaseType) throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection is not established.");
        }

        Statement statement = connection.createStatement();
        if ("mysql".equalsIgnoreCase(databaseType)) {
            // MySQL table creation
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS clans (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                    "name VARCHAR(255) UNIQUE," +
                    "prefix VARCHAR(255)," +
                    "kills INTEGER DEFAULT 0," +
                    "deaths INTEGER DEFAULT 0," +
                    "level INTEGER DEFAULT 0," +
                    "exp INTEGER DEFAULT 0," +
                    "banner TEXT," +
                    "base_world VARCHAR(255)," +
                    "base_x DOUBLE," +
                    "base_y DOUBLE," +
                    "base_z DOUBLE," +
                    "base_yaw FLOAT," +
                    "base_pitch FLOAT," +
                    "pvp_enabled BOOLEAN DEFAULT FALSE," +
                    "bank_balance DOUBLE DEFAULT 0," +
                    "chest TEXT," +
                    "created_date TIMESTAMP" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS players (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "name VARCHAR(20)," +
                    "clan_id INTEGER," +
                    "user_rank_id INTEGER," +
                    "kills INTEGER DEFAULT 0," +
                    "deaths INTEGER DEFAULT 0," +
                    "join_date TIMESTAMP," +
                    "FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS invites (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                    "clan_id INTEGER," +
                    "player_uuid VARCHAR(36)," +
                    "FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS alliances (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                    "clan1_id INTEGER," +
                    "clan2_id INTEGER," +
                    "FOREIGN KEY (clan1_id) REFERENCES clans(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (clan2_id) REFERENCES clans(id) ON DELETE CASCADE" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ally_requests (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                    "requester_clan_id INTEGER," +
                    "target_clan_id INTEGER," +
                    "FOREIGN KEY (requester_clan_id) REFERENCES clans(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (target_clan_id) REFERENCES clans(id) ON DELETE CASCADE" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS enemies (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                    "clan1_id INTEGER," +
                    "clan2_id INTEGER," +
                    "FOREIGN KEY (clan1_id) REFERENCES clans(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (clan2_id) REFERENCES clans(id) ON DELETE CASCADE" +
                    ")");
        } else {
            // SQLite table creation
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS clans (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name VARCHAR(255) UNIQUE," +
                    "prefix VARCHAR(255)," +
                    "kills INTEGER DEFAULT 0," +
                    "deaths INTEGER DEFAULT 0," +
                    "rank_id INTEGER DEFAULT 0," +
                    "level INTEGER DEFAULT 0," +
                    "exp INTEGER DEFAULT 0," +
                    "banner TEXT," +
                    "base_world VARCHAR(255)," +
                    "base_x DOUBLE," +
                    "base_y DOUBLE," +
                    "base_z DOUBLE," +
                    "base_yaw FLOAT," +
                    "base_pitch FLOAT," +
                    "pvp_enabled BOOLEAN DEFAULT FALSE," +
                    "bank_balance DOUBLE DEFAULT 0," +
                    "chest TEXT," +
                    "created_date TIMESTAMP" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS players (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "name VARCHAR(20)," +
                    "clan_id INTEGER," +
                    "user_rank_id INTEGER," +
                    "kills INTEGER DEFAULT 0," +
                    "deaths INTEGER DEFAULT 0," +
                    "join_date TIMESTAMP," +
                    "FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS invites (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "clan_id INTEGER," +
                    "player_uuid VARCHAR(36)," +
                    "FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS alliances (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "clan1_id INTEGER," +
                    "clan2_id INTEGER," +
                    "FOREIGN KEY (clan1_id) REFERENCES clans(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (clan2_id) REFERENCES clans(id) ON DELETE CASCADE" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ally_requests (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "requester_clan_id INTEGER," +
                    "target_clan_id INTEGER," +
                    "FOREIGN KEY (requester_clan_id) REFERENCES clans(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (target_clan_id) REFERENCES clans(id) ON DELETE CASCADE" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS enemies (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "clan1_id INTEGER," +
                    "clan2_id INTEGER," +
                    "FOREIGN KEY (clan1_id) REFERENCES clans(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (clan2_id) REFERENCES clans(id) ON DELETE CASCADE" +
                    ")");
        }
        statement.close();
    }


    public void updateDatabase() {
        try {
            if (connection == null) {
                plugin.getLogger().severe("Database connection is not established.");
                return;
            }

            Statement statement = connection.createStatement();


            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not update the database: " + e.getMessage());
        }
    }

    public void closeDatabaseConnection() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Database connection closed.");
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not close the database connection: " + e.getMessage());
            }
        }
    }

    public Connection getConnection() {
        if (connection == null) {
            plugin.getLogger().severe("Database connection is not established.");
            plugin.getServer()
                    .getPluginManager()
                    .disablePlugin(plugin);
            return null;
        }
        return connection;
    }
}
