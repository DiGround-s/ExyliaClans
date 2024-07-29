package net.diground.exylia.utils;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.Rank;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.regex.Pattern;

public class ClanUtils {

    public static List<Integer> getAllClansIds(ExyliaClans plugin) {
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT id FROM clans";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            List<Integer> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
            rs.close();
            stmt.close();
            return ids;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not get all clans ids: " + e.getMessage());
            return null;
        }
    }

    public static String getPlayerActualChat(ExyliaClans plugin, UUID playerUUID) {
        String chatMode = plugin.getClanChatManager().getChatMode(playerUUID);
        return switch (chatMode) {
            case "CLAN" -> plugin.getMessage("chat_mode.clan");
            case "ALLY" -> plugin.getMessage("chat_mode.ally");
            case "GLOBAL" -> plugin.getMessage("chat_mode.global");
            default -> "NONE";
        };
    }
    public static boolean isPlayerInTheClan(ExyliaClans plugin, int clanId, UUID playerUUID) {
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT 1 FROM players WHERE clan_id = ? AND uuid = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            stmt.setString(2, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next();
            rs.close();
            stmt.close();
            return exists;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check if player is in a clan: " + e.getMessage());
            return false;
        }
    }


    public static boolean isValidClanName(ExyliaClans plugin, String clanName) {
        int minLength = plugin.getConfig().getInt("name.min_length");
        int maxLength = plugin.getConfig().getInt("name.max_length");
        String regex = plugin.getConfig().getString("name.regex");

        if (clanName.length() < minLength || clanName.length() > maxLength) {
            return false;
        }

        assert regex != null;
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(clanName).matches();
    }

    public static boolean isValidPrefix(ExyliaClans plugin, String prefix) {
        int minLength = plugin.getConfig().getInt("prefix.min_length");
        int maxLength = plugin.getConfig().getInt("prefix.max_length");

        // Remover los códigos de color usando una expresión regular
        String visiblePrefix = prefix.replaceAll("(&[0-9a-fk-orA-FK-OR])|(&#([A-Fa-f0-9]{6}))", "");

        if (visiblePrefix.length() < minLength || visiblePrefix.length() > maxLength) {
            return false;
        }

        // Opción para una validación adicional basada en regex, si es necesario
        String regex = plugin.getConfig().getString("prefix.regex");
        if (regex != null) {
            Pattern pattern = Pattern.compile(regex);
            return pattern.matcher(visiblePrefix).matches();
        }

        return true;
    }

    public static boolean isClanNameTaken(ExyliaClans plugin, String clanName) {
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT 1 FROM clans WHERE name = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, clanName);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next();
            rs.close();
            stmt.close();
            return exists;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check if clan name is taken: " + e.getMessage());
            return true;
        }
    }

    public static boolean isClanPrefixTaken(ExyliaClans plugin, String clanPrefix) {
        Connection connection = plugin.getConnection();
        try {
            // Eliminar los códigos de color del prefijo proporcionado
            String visiblePrefix = clanPrefix.replaceAll("(&[0-9a-fk-orA-FK-OR])|(&#([A-Fa-f0-9]{6}))", "");

            // Obtener todos los prefijos existentes en la base de datos
            String query = "SELECT prefix FROM clans";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Comparar el prefijo proporcionado con los existentes, sin tener en cuenta los códigos de color
            while (rs.next()) {
                String existingPrefix = rs.getString("prefix");
                String visibleExistingPrefix = existingPrefix.replaceAll("(&[0-9a-fk-orA-FK-OR])|(&#([A-Fa-f0-9]{6}))", "");

                if (visiblePrefix.equalsIgnoreCase(visibleExistingPrefix)) {
                    rs.close();
                    stmt.close();
                    return true;
                }
            }

            rs.close();
            stmt.close();
            return false;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check if clan prefix is taken: " + e.getMessage());
            return true;
        }
    }

    public static String getClanPrefix(ExyliaClans plugin, int clanId) {
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT prefix FROM clans WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            String clanPrefix = rs.getString("prefix");
            rs.close();
            stmt.close();
            return clanPrefix;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not get clan prefix: " + e.getMessage());
            return null;
        }
    }

    public static void updateClanPrefix(ExyliaClans plugin, int clanId, String clanPrefix) {
        Connection connection = plugin.getConnection();
        try {
            String updateClanPrefixSQL = "UPDATE clans SET prefix = ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(updateClanPrefixSQL);
            stmt.setString(1, clanPrefix);
            stmt.setInt(2, clanId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not update clan prefix: " + e.getMessage());
        }
    }

    public static void updateClanName(ExyliaClans plugin, int clanId, String clanName) {
        Connection connection = plugin.getConnection();
        try {
            String updateClanNameSQL = "UPDATE clans SET name = ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(updateClanNameSQL);
            stmt.setString(1, clanName);
            stmt.setInt(2, clanId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not update clan name: " + e.getMessage());
        }
    }

    public static boolean isPlayerInClan(ExyliaClans plugin, String playerUUID) {
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT 1 FROM players WHERE uuid = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, playerUUID);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next();
            rs.close();
            stmt.close();
            return exists;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check if player is in a clan: " + e.getMessage());
            return false;
        }
    }

    public static void addExpToClan(ExyliaClans plugin, int clanId, int exp) {
        Connection connection = plugin.getConnection();
        try {
            String updateClanExpSQL = "UPDATE clans SET exp = exp + ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(updateClanExpSQL);
            stmt.setInt(1, exp);
            stmt.setInt(2, clanId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not update clan exp: " + e.getMessage());
        }
    }

    public static void removeExpFromClan(ExyliaClans plugin, int clanId, int exp) {
        Connection connection = plugin.getConnection();
        try {
            String updateClanExpSQL = "UPDATE clans SET exp = exp - ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(updateClanExpSQL);
            stmt.setInt(1, exp);
            stmt.setInt(2, clanId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not update clan exp: " + e.getMessage());
        }
    }

    public static int getClanExp(ExyliaClans plugin, int clanId) {
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT exp FROM clans WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int clanExp = rs.getInt("exp");
            rs.close();
            stmt.close();
            return clanExp;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not get clan exp: " + e.getMessage());
            return 0;
        }
    }

    public static void addLevelToClan(ExyliaClans plugin, int clanId, int level) {
        Connection connection = plugin.getConnection();
        try {
            String updateClanLevelSQL = "UPDATE clans SET level = level + ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(updateClanLevelSQL);
            stmt.setInt(1, level);
            stmt.setInt(2, clanId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not update clan level: " + e.getMessage());
        }
    }

    public static void removeLevelFromClan(ExyliaClans plugin, int clanId, int level) {
        Connection connection = plugin.getConnection();
        try {
            String updateClanLevelSQL = "UPDATE clans SET level = level - ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(updateClanLevelSQL);
            stmt.setInt(1, level);
            stmt.setInt(2, clanId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not update clan level: " + e.getMessage());
        }
    }

    public static int getClanLevel(ExyliaClans plugin, int clanId) {
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT level FROM clans WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int clanLevel = rs.getInt("level");
            rs.close();
            stmt.close();
            return clanLevel;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not get clan level: " + e.getMessage());
            return 0;
        }
    }

    public static boolean arePlayersInSameClan(ExyliaClans plugin, String playerUUID1, String playerUUID2) {
        int clanId1 = getPlayerClanId(plugin, playerUUID1);
        int clanId2 = getPlayerClanId(plugin, playerUUID2);

        // Verificar si ambos jugadores están en algún clan y si los IDs de los clanes son iguales
        return clanId1 != -1 && clanId2 != -1 && clanId1 == clanId2;
    }

    public static boolean isPlayerClanLeader(ExyliaClans plugin, String playerUUID) {
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT user_rank_id FROM players WHERE uuid = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, playerUUID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt("user_rank_id") == Rank.LEADER_RANK) {
                rs.close();
                stmt.close();
                return true;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check if player is clan leader: " + e.getMessage());
        }
        return false;
    }

    public static OfflinePlayer getClanLeader(ExyliaClans plugin, int clanId) {
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT uuid FROM players WHERE clan_id = ? AND user_rank_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            stmt.setInt(2, Rank.LEADER_RANK);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String clanLeaderUUID = rs.getString("uuid");
                rs.close();
                stmt.close();
                return Bukkit.getOfflinePlayer(UUID.fromString(clanLeaderUUID));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not get clan leader: " + e.getMessage());
        }
        return null;
    }

    public static String getPlayerName(String playerUUID) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
        return player.getName() != null ? player.getName() : playerUUID;
    }

    public static List<String> getClanNames(ExyliaClans plugin) {
        List<String> clanNames = new ArrayList<>();
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT name FROM clans";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                clanNames.add(rs.getString("name"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan names: " + e.getMessage());
        }
        return clanNames;
    }

    public static String getClanName(ExyliaClans plugin, int clanId) {
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT name FROM clans WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String clanName = rs.getString("name");
                rs.close();
                stmt.close();
                return clanName;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan name: " + e.getMessage());
        }
        return null;
    }

    public static List<String> getClanMembers(ExyliaClans plugin, int clanId) {
        List<String> members = new ArrayList<>();
        Connection connection = plugin.getConnection();
        String query = "SELECT uuid FROM players WHERE clan_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, clanId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String uuid = rs.getString("uuid");
                    members.add(getPlayerName(uuid));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan members: " + e.getMessage());
        }

        return members;
    }

    public static List<UUID> getClanMemberUUIDs(ExyliaClans plugin, int clanId) {
        List<UUID> members = new ArrayList<>();
        Connection connection = plugin.getConnection();
        String query = "SELECT uuid FROM players WHERE clan_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, clanId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String uuid = rs.getString("uuid");
                    members.add(UUID.fromString(uuid));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan members: " + e.getMessage());
        }

        return members;
    }

    public static List<Player> getClanPlayerMembers(ExyliaClans plugin, int clanId) {
        List<String> members = getClanMembers(plugin, clanId);
        List<Player> clanMembers = new ArrayList<>();
        for (String name : members) {
            clanMembers.add(Bukkit.getPlayerExact(name));
        }
        return clanMembers;
    }
    public static List<OfflinePlayer> getClanOfflinePlayerMembers(ExyliaClans plugin, int clanId) {
        // Obtén los UUIDs de los miembros del clan
        List<UUID> memberUUIDs = getClanMemberUUIDs(plugin, clanId);
        List<OfflinePlayer> clanMembers = new ArrayList<>();
        for (UUID uuid : memberUUIDs) {
            clanMembers.add(Bukkit.getOfflinePlayer(uuid));
        }
        return clanMembers;
    }

    public static List<String> getClanOfflinePlayerMemberNames(ExyliaClans plugin, int clanId) {
        List<OfflinePlayer> clanMembers = getClanOfflinePlayerMembers(plugin, clanId);
        List<String> memberNames = new ArrayList<>();
        for (OfflinePlayer player : clanMembers) {
            memberNames.add(player.getName()); // getName() puede ser null si el jugador nunca ha estado en línea en el servidor
        }
        return memberNames;
    }


    public static List<String> getOnlineClanMembers(ExyliaClans plugin, int clanId) {
        List<String> onlineMembers = new ArrayList<>();
        List<String> clanMembers = getClanMembers(plugin, clanId);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (clanMembers.contains(player.getName())) {
                onlineMembers.add(player.getName());
            }
        }

        return onlineMembers;
    }

    public static int getOnlineClanMembersCount(ExyliaClans plugin, int clanId) {
        return getOnlineClanMembers(plugin, clanId).size();
    }

    public static boolean isPvpEnabled(ExyliaClans plugin, int clanId) {
        boolean pvpEnabled = false;

        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT pvp_enabled FROM clans WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                pvpEnabled = rs.getBoolean("pvp_enabled");
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve PvP status: " + e.getMessage());
        }

        return pvpEnabled;
    }
    public static String getPlayerClan(ExyliaClans plugin, String playerUUID) {
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT clans.name FROM players JOIN clans ON players.clan_id = clans.id WHERE players.uuid = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, playerUUID);
            ResultSet resultSet = statement.executeQuery();
            String clanName = resultSet.next() ? resultSet.getString("name") : null;
            resultSet.close();
            statement.close();
            return clanName;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve player's clan: " + e.getMessage());
            return null;
        }
    }

    public static List<Map<String, Object>> getClans(ExyliaClans plugin) {
        List<Map<String, Object>> clans = new ArrayList<>();

        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT id, name FROM clans";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int clanId = rs.getInt("id");
                String name = rs.getString("name");

                int totalMembers = getTotalMembers(plugin, clanId);
                long onlineMembers = getOnlineMembers(plugin, clanId);

                Map<String, Object> clan = new HashMap<>();
                clan.put("id", clanId);
                clan.put("name", name);
                clan.put("total_members", totalMembers);
                clan.put("online_members", onlineMembers);

                clans.add(clan);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clans: " + e.getMessage());
        }

        clans.sort((a, b) -> Long.compare((long) b.get("online_members"), (long) a.get("online_members")));

        return clans;
    }

    private static int getTotalMembers(ExyliaClans plugin, int clanId) {
        int totalMembers = 0;

        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT COUNT(*) as total FROM players WHERE clan_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                totalMembers = rs.getInt("total");
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve total members: " + e.getMessage());
        }

        return totalMembers;
    }

    private static long getOnlineMembers(ExyliaClans plugin, int clanId) {
        long onlineMembers = 0;

        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT uuid FROM players WHERE clan_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String uuid = rs.getString("uuid");
                if (Bukkit.getPlayer(UUID.fromString(uuid)) != null) {
                    onlineMembers++;
                }
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve online members: " + e.getMessage());
        }

        return onlineMembers;
    }

    public static int getClanIdByName(ExyliaClans plugin, String clanName) {
        int clanId = -1;

        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT id FROM clans WHERE name = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, clanName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                clanId = rs.getInt("id");
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan ID by name: " + e.getMessage());
        }

        return clanId;
    }

    public static Map<String, Object> getClanInfo(ExyliaClans plugin, int clanId) {
        Map<String, Object> clanInfo = new HashMap<>();

        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT name FROM clans WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                clanInfo.put("id", clanId);
                clanInfo.put("name", rs.getString("name"));
                clanInfo.put("total_members", getTotalMembers(plugin, clanId));
                clanInfo.put("online_members", getOnlineMembers(plugin, clanId));
            } else {
                clanInfo = null;
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan info: " + e.getMessage());
            clanInfo = null;
        }

        return clanInfo;
    }

    public static List<String> getClanMembersNames(ExyliaClans plugin, int clanId) {
        List<String> members = new ArrayList<>();

        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT uuid FROM players WHERE clan_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String uuid = rs.getString("uuid");
                Player player = Bukkit.getPlayer(UUID.fromString(uuid));
                if (player != null) {
                    members.add(player.getName());
                } else {
                    members.add(uuid); // Placeholder, you might want to resolve UUID to player names here
                }
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan members: " + e.getMessage());
        }

        return members;
    }

    public static Map<Integer, ItemStack> getClanChest(ExyliaClans plugin, int clanId) {
        Map<Integer, ItemStack> chestContents = new HashMap<>();

        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT chest FROM clans WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String chestData = rs.getString("chest");
                if (chestData != null && !chestData.isEmpty()) {
                    String[] items = chestData.split(";");
                    for (String itemData : items) {
                        String[] parts = itemData.split(":");
                        if (parts.length == 2) {
                            int slot = Integer.parseInt(parts[0]);
                            ItemStack item = deserializeItemStack(parts[1]);
                            chestContents.put(slot, item);
                        }
                    }
                }
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan chest: " + e.getMessage());
        }

        return chestContents;
    }

    public static void saveClanChest(ExyliaClans plugin, int clanId, Map<Integer, ItemStack> chestContents) {
        try {
            Connection connection = plugin.getConnection();
            StringBuilder serializedChest = new StringBuilder();

            for (Map.Entry<Integer, ItemStack> entry : chestContents.entrySet()) {
                int slot = entry.getKey();
                ItemStack item = entry.getValue();
                String itemData = serializeItemStack(item);
                serializedChest.append(slot).append(":").append(itemData).append(";");
            }

            // Remove trailing semicolon
            if (serializedChest.length() > 0) {
                serializedChest.setLength(serializedChest.length() - 1);
            }

            String updateQuery = "UPDATE clans SET chest = ? WHERE id = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
            updateStmt.setString(1, serializedChest.toString());
            updateStmt.setInt(2, clanId);
            updateStmt.executeUpdate();
            updateStmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save clan chest: " + e.getMessage());
        }
    }

    private static String serializeItemStack(ItemStack item) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(byteArrayOutputStream);
            bukkitObjectOutputStream.writeObject(item);
            bukkitObjectOutputStream.close();
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to serialize ItemStack", e);
        }
    }

    private static ItemStack deserializeItemStack(String data) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream);
            ItemStack item = (ItemStack) bukkitObjectInputStream.readObject();
            bukkitObjectInputStream.close();
            return item;
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Unable to deserialize ItemStack", e);
        }
    }


    public static void uploadClanBanner(ExyliaClans plugin, int clanId, ItemStack banner) {
        try {
            Connection connection = plugin.getConnection();
            String query = "UPDATE clans SET banner = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, serializeItemStack(banner));
            statement.setInt(2, clanId);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not upload clan banner: " + e.getMessage());
        }
    }

    public static void removeClanBanner(ExyliaClans plugin, int clanId) {
        try {
            Connection connection = plugin.getConnection();
            String query = "UPDATE clans SET banner = NULL WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clanId);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not remove clan banner: " + e.getMessage());
        }
    }

    public static ItemStack getClanBanner(ExyliaClans plugin, int clanId) {
        ItemStack banner = null;
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT banner FROM clans WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clanId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String bannerData = resultSet.getString("banner");
                if (bannerData != null && !bannerData.isEmpty()) {
                    banner = deserializeItemStack(bannerData);
                }
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan banner: " + e.getMessage());
        }
        return banner;
    }



    public static String getClanNameById(ExyliaClans plugin, int clanId) {
        String clanName = "";
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT name FROM clans WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clanId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                clanName = resultSet.getString("name");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan name: " + e.getMessage());
        }
        return clanName;
    }

    public static boolean areClansAllied(ExyliaClans plugin, int clanId1, int clanId2) {
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT COUNT(*) FROM alliances WHERE (clan1_id = ? AND clan2_id = ?) OR (clan1_id = ? AND clan2_id = ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clanId1);
            statement.setInt(2, clanId2);
            statement.setInt(3, clanId2);
            statement.setInt(4, clanId1);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            boolean allied = resultSet.getInt(1) > 0;
            resultSet.close();
            statement.close();
            return allied;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check if clans are allied: " + e.getMessage());
            return false;
        }
    }

    public static boolean hasPendingAllyRequest(ExyliaClans plugin, int clanId1, int clanId2) {
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT COUNT(*) FROM ally_requests WHERE requester_clan_id = ? AND target_clan_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clanId1);
            statement.setInt(2, clanId2);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            boolean pending = resultSet.getInt(1) > 0;
            resultSet.close();
            statement.close();
            return pending;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check if ally request is pending: " + e.getMessage());
            return false;
        }
    }

    public static void sendAllyRequest(ExyliaClans plugin, int requesterClanId, int targetClanId) {
        try {
            Connection connection = plugin.getConnection();
            String query = "INSERT INTO ally_requests (requester_clan_id, target_clan_id) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, requesterClanId);
            statement.setInt(2, targetClanId);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not send ally request: " + e.getMessage());
        }
    }

    public static void acceptAllyRequest(ExyliaClans plugin, int requesterClanId, int targetClanId) {
        try {
            Connection connection = plugin.getConnection();
            String query = "INSERT INTO alliances (clan1_id, clan2_id) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, requesterClanId);
            statement.setInt(2, targetClanId);
            statement.executeUpdate();

            query = "DELETE FROM ally_requests WHERE requester_clan_id = ? AND target_clan_id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, requesterClanId);
            statement.setInt(2, targetClanId);
            statement.executeUpdate();

            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not accept ally request: " + e.getMessage());
        }
    }

    public static void denyAllyRequest(ExyliaClans plugin, int requesterClanId, int targetClanId) {
        try {
            Connection connection = plugin.getConnection();
            String query = "DELETE FROM ally_requests WHERE requester_clan_id = ? AND target_clan_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, requesterClanId);
            statement.setInt(2, targetClanId);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not deny ally request: " + e.getMessage());
        }
    }

    public static void breakAlliance(ExyliaClans plugin, int clanId1, int clanId2) {
        try {
            Connection connection = plugin.getConnection();
            String query = "DELETE FROM alliances WHERE (clan1_id = ? AND clan2_id = ?) OR (clan1_id = ? AND clan2_id = ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clanId1);
            statement.setInt(2, clanId2);
            statement.setInt(3, clanId2);
            statement.setInt(4, clanId1);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not break alliance: " + e.getMessage());
        }
    }
    public static List<UUID> getClanMembersByName(ExyliaClans plugin, String clanName) {
        List<UUID> members = new ArrayList<>();
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT uuid FROM players WHERE clan_id = (SELECT id FROM clans WHERE name = ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, clanName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                members.add(UUID.fromString(resultSet.getString("uuid")));
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan members by name: " + e.getMessage());
        }
        return members;
    }

    public static List<UUID> getClanMembersById(ExyliaClans plugin, int clanId) {
        List<UUID> members = new ArrayList<>();
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT uuid FROM players WHERE clan_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clanId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                members.add(UUID.fromString(resultSet.getString("uuid")));
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan members by ID: " + e.getMessage());
        }
        return members;
    }

    public static boolean hasPermission(ExyliaClans plugin, String playerUUID, String permission) {
        String rankName = getPlayerRank(plugin, playerUUID);
        if (rankName == null) {
            return false;
        }

        Rank rank = plugin.getRankManager().getRank(rankName);
        if (rank == null) {
            return false;
        }

        return rank.getPermissions().contains("ALL") || rank.getPermissions().contains(permission);
    }

    public static String getPlayerRank(ExyliaClans plugin, String playerUUID) {
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT user_rank_id FROM players WHERE uuid = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, playerUUID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int rankId = rs.getInt("user_rank_id");
                rs.close();
                stmt.close();
                return plugin.getRankManager().getRankById(rankId).getName();
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve player rank: " + e.getMessage());
        }
        return null;
    }

    public static int getPlayerRankId(ExyliaClans plugin, String playerUUID) {
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT user_rank_id FROM players WHERE uuid = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, playerUUID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int rankId = rs.getInt("user_rank_id");
                rs.close();
                stmt.close();
                return rankId;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve player rank: " + e.getMessage());
        }
        return -1;
    }

    public static Date getPlayerTimeStampByName(ExyliaClans plugin, String playerName) {
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT join_date FROM players WHERE name = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("join_date");
                rs.close();
                stmt.close();
                return timestamp;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve player timestamp: " + e.getMessage());
        }
        return null;
    }


    public static int getPlayerRankByName(ExyliaClans plugin, String playerName) {
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT user_rank_id FROM players WHERE name = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int rankId = rs.getInt("user_rank_id");
                rs.close();
                stmt.close();
                return rankId;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve player rank: " + e.getMessage());
        }
        return -1;
    }

    public static String getRankNameById(ExyliaClans plugin, int rankId) {
        return plugin.getRankManager().getRankById(rankId).getName();
    }

    public static String getRankDisplayNameById(ExyliaClans plugin, int rankId) {
        return plugin.getRankManager().getRankById(rankId).getDisplayName();
    }

    public static boolean areClansEnemies(ExyliaClans plugin, int clanId1, int clanId2) {
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT COUNT(*) FROM enemies WHERE clan1_id = ? AND clan2_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clanId1);
            statement.setInt(2, clanId2);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            boolean isEnemy = resultSet.getInt(1) > 0;
            resultSet.close();
            statement.close();
            return isEnemy;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check if a clan is enemy of another: " + e.getMessage());
            return false;
        }
    }

    public static void addEnemy(ExyliaClans plugin, int clanId1, int clanId2) {
        if (plugin.getDatabaseType().equalsIgnoreCase("sqlite")) {
            try {
                Connection connection = plugin.getConnection();
                String query = "INSERT OR REPLACE INTO enemies (clan1_id, clan2_id) VALUES (?, ?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, clanId1);
                statement.setInt(2, clanId2);
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not add enemy: " + e.getMessage());
            }
        } else if (plugin.getDatabaseType().equalsIgnoreCase("mysql")) {
            try {
                Connection connection = plugin.getConnection();
                String query = "INSERT IGNORE INTO enemies (clan1_id, clan2_id) VALUES (?, ?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, clanId1);
                statement.setInt(2, clanId2);
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not add enemy: " + e.getMessage());
            }
        }
    }



    public static void removeEnemy(ExyliaClans plugin, int clanId1, int clanId2) {
        try {
            Connection connection = plugin.getConnection();
            String query = "DELETE FROM enemies WHERE clan1_id = ? AND clan2_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, clanId1);
            statement.setInt(2, clanId2);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not remove enemy: " + e.getMessage());
        }
    }



    public static boolean clanExists(ExyliaClans plugin, String clanName) {
        try {
            Connection connection = plugin.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT id FROM clans WHERE name = ?");
            statement.setString(1, clanName);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check if clan exists: " + e.getMessage());
        }
        return false;
    }

    public static int getEnemyCount(ExyliaClans plugin, int clanId) {
        return getClanEnemies(plugin, clanId).size();
    }

    public static int getAllianceCount(ExyliaClans plugin, int clanId) {
        return getClanAllies(plugin, clanId).size();
    }

    public static Player getPlayerByUUID(UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

    public static List<Player> getClanMembersPlayers(ExyliaClans plugin, int clanId) {
        List<Player> members = new ArrayList<>();
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT uuid FROM players WHERE clan_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                Player player = getPlayerByUUID(uuid);
                if (player != null) {
                    members.add(player);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan members: " + e.getMessage());
        }
        return members;
    }

    public static List<String> getClanAllies(ExyliaClans plugin, int clanId) {
        List<String> allies = new ArrayList<>();
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT clan2_id FROM alliances WHERE clan1_id = ? UNION SELECT clan1_id FROM alliances WHERE clan2_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            stmt.setInt(2, clanId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                allies.add(getClanNameById(plugin, rs.getInt(1)));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve allies: " + e.getMessage());
        }
        return allies;
    }

    public static List<Integer> getClanAlliesIds(ExyliaClans plugin, int clanId) {
        List<Integer> allies = new ArrayList<>();
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT clan2_id FROM alliances WHERE clan1_id = ? UNION SELECT clan1_id FROM alliances WHERE clan2_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            stmt.setInt(2, clanId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                allies.add(rs.getInt(1));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve allies: " + e.getMessage());
        }
        return allies;
    }

    // Método para obtener la lista de enemigos de un clanId
    public static List<String> getClanEnemies(ExyliaClans plugin, int clanId) {
        List<String> enemyNames = new ArrayList<>();
        Connection connection = plugin.getConnection();
        try {
            // Primero obtenemos los IDs de los enemigos
            String queryIds = "SELECT clan2_id FROM enemies WHERE clan1_id = ?";
            PreparedStatement stmtIds = connection.prepareStatement(queryIds);
            stmtIds.setInt(1, clanId);
            ResultSet rsIds = stmtIds.executeQuery();

            // Luego, por cada ID de enemigo, obtenemos el nombre del clan
            while (rsIds.next()) {
                int enemyId = rsIds.getInt("clan2_id");

                // Consulta para obtener el nombre del clan basado en el ID
                String queryName = "SELECT name FROM clans WHERE id = ?";
                PreparedStatement stmtName = connection.prepareStatement(queryName);
                stmtName.setInt(1, enemyId);
                ResultSet rsName = stmtName.executeQuery();

                if (rsName.next()) {
                    enemyNames.add(rsName.getString("name"));
                }
                rsName.close();
                stmtName.close();
            }
            rsIds.close();
            stmtIds.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan enemies: " + e.getMessage());
        }
        return enemyNames;
    }

    public static List<Integer> getClanEnemiesIds(ExyliaClans plugin, int clanId) {
        List<Integer> enemyIds = new ArrayList<>();
        Connection connection = plugin.getConnection();
        try {
            // Primero obtenemos los IDs de los enemigos
            String queryIds = "SELECT clan2_id FROM enemies WHERE clan1_id = ?";
            PreparedStatement stmtIds = connection.prepareStatement(queryIds);
            stmtIds.setInt(1, clanId);
            ResultSet rsIds = stmtIds.executeQuery();
            while (rsIds.next()) {
                enemyIds.add(rsIds.getInt("clan2_id"));
            }
            rsIds.close();
            stmtIds.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan enemies: " + e.getMessage());
        }
        return enemyIds;
    }



    // Método para obtener la cuenta de miembros de un clanId
    public static int getMemberCount(ExyliaClans plugin, int clanId) {
        int memberCount = 0;
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT COUNT(*) FROM players WHERE clan_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                memberCount = rs.getInt(1);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve member count: " + e.getMessage());
        }
        return memberCount;
    }

    private static final Map<UUID, Integer> playerClanCache = new HashMap<>();
    private static final Map<String, Boolean> relationCache = new HashMap<>();

    public static boolean isMember(ExyliaClans plugin, UUID player1, UUID player2) {
        return checkRelation(plugin, player1, player2, "members");
    }

    public static boolean isEnemy(ExyliaClans plugin, UUID player1, UUID player2) {
        return checkRelation(plugin, player1, player2, "enemies");
    }

    public static boolean isAlly(ExyliaClans plugin, UUID player1, UUID player2) {
        return checkRelation(plugin, player1, player2, "alliances");
    }

    private static boolean checkRelation(ExyliaClans plugin, UUID player1, UUID player2, String relationType) {
        int clan1Id = getPlayerClanId(plugin, String.valueOf(player1));
        int clan2Id = getPlayerClanId(plugin, String.valueOf(player2));

        if (clan1Id == -1 || clan2Id == -1) return false;

        String cacheKey = relationType + ":" + clan1Id + ":" + clan2Id;
        if (relationCache.containsKey(cacheKey)) {
            return relationCache.get(cacheKey);
        }

        boolean result = false;
        Connection connection = plugin.getConnection();
        try {
            String query;
            switch (relationType) {
                case "members":
                    query = "SELECT COUNT(*) FROM players WHERE clan_id = ? AND uuid = ?";
                    PreparedStatement stmtMember = connection.prepareStatement(query);
                    stmtMember.setInt(1, clan1Id);
                    stmtMember.setString(2, player2.toString());
                    ResultSet rsMember = stmtMember.executeQuery();
                    if (rsMember.next()) {
                        result = rsMember.getInt(1) > 0;
                    }
                    rsMember.close();
                    stmtMember.close();
                    break;
                case "enemies":
                    query = "SELECT COUNT(*) FROM enemies WHERE clan1_id = ? AND clan2_id = ?";
                    PreparedStatement stmtEnemy = connection.prepareStatement(query);
                    stmtEnemy.setInt(1, clan1Id);
                    stmtEnemy.setInt(2, clan2Id);
                    ResultSet rsEnemy = stmtEnemy.executeQuery();
                    if (rsEnemy.next()) {
                        result = rsEnemy.getInt(1) > 0;
                    }
                    rsEnemy.close();
                    stmtEnemy.close();
                    break;
                case "alliances":
                    query = "SELECT COUNT(*) FROM alliances WHERE clan1_id = ? AND clan2_id = ?";
                    PreparedStatement stmtAlly = connection.prepareStatement(query);
                    stmtAlly.setInt(1, clan1Id);
                    stmtAlly.setInt(2, clan2Id);
                    ResultSet rsAlly = stmtAlly.executeQuery();
                    if (rsAlly.next()) {
                        result = rsAlly.getInt(1) > 0;
                    }
                    rsAlly.close();
                    stmtAlly.close();
                    break;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not check if players have relation: " + e.getMessage());
        }

        relationCache.put(cacheKey, result);
        return result;
    }

    public static int getPlayerClanId(ExyliaClans plugin, String playerUUID) {
        if (playerClanCache.containsKey(UUID.fromString(playerUUID))) {
            return playerClanCache.get(UUID.fromString(playerUUID));
        }

        int clanId = -1;
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT clan_id FROM players WHERE uuid = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, playerUUID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                clanId = rs.getInt("clan_id");
                playerClanCache.put(UUID.fromString(playerUUID), clanId);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve player clan ID: " + e.getMessage());
        }
        return clanId;
    }

    public static boolean addClanKill(ExyliaClans plugin, int clanId) {
        try {
            Connection connection = plugin.getConnection();
            String query = "UPDATE clans SET kills = kills + 1 WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not add clan kill: " + e.getMessage());
            return false;
        }
    }

    public static boolean removeClanKill(ExyliaClans plugin, int clanId) {
        try {
            Connection connection = plugin.getConnection();
            String query = "UPDATE clans SET kills = GREATEST(kills - 1, 0) WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not remove clan kill: " + e.getMessage());
            return false;
        }
    }

    public static boolean addClanDeath(ExyliaClans plugin, int clanId) {
        try {
            Connection connection = plugin.getConnection();
            String query = "UPDATE clans SET deaths = deaths + 1 WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not add clan death: " + e.getMessage());
            return false;
        }
    }

    public static boolean removeClanDeath(ExyliaClans plugin, int clanId) {
        try {
            Connection connection = plugin.getConnection();
            String query = "UPDATE clans SET deaths = GREATEST(deaths - 1, 0) WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not remove clan death: " + e.getMessage());
            return false;
        }
    }

    public static Map<String, Integer> getClanStats(ExyliaClans plugin, int clanId) {
        Map<String, Integer> stats = new HashMap<>();
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT kills, deaths FROM clans WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("kills", rs.getInt("kills"));
                stats.put("deaths", rs.getInt("deaths"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan deaths: " + e.getMessage());
        }
        return stats;
    }



    public static int getClanKills(ExyliaClans plugin, int clanId) {
        int kills = 0;
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT kills FROM clans WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                kills = rs.getInt("kills");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan kills: " + e.getMessage());
        }
        return kills;
    }

    public static int getClanDeaths(ExyliaClans plugin, int clanId) {
        int deaths = 0;
        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT deaths FROM clans WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                deaths = rs.getInt("deaths");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not retrieve clan deaths: " + e.getMessage());
        }
        return deaths;
    }

    public static double getClanKDR(ExyliaClans plugin, int clanId) {
        double kdr = 0.0;
        Map<String, Integer> stats = getClanStats(plugin, clanId);
        int kills = stats.getOrDefault("kills", 0);
        int deaths = stats.getOrDefault("deaths", 0);

        if (deaths > 0) {
            kdr = (double) kills / deaths;
        }

        return Math.round(kdr * 10.0) / 10.0;
    }






}