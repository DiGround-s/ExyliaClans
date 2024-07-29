package net.diground.exylia.commands.base;

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

public class ClanDelbaseCommand {
    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ClanDelbaseCommand(ExyliaClans plugin) {
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
        String clanName = ClanUtils.getPlayerClan(plugin, playerUUID);

        if (clanName == null) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        if (!ClanUtils.hasPermission(plugin, playerUUID, "DELETE_BASE")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID);

        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT base_world FROM clans WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, clanId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String worldName = rs.getString("base_world");
                if (worldName == null) {
                    player.sendMessage(ChatUtils.translateColors(plugin.getMessage("base_not_set")));
                    return true;
                }

                String deleteBaseSQL = "UPDATE clans SET base_world = NULL, base_x = NULL, base_y = NULL, base_z = NULL, base_yaw = NULL, base_pitch = NULL WHERE id = ?";
                PreparedStatement deleteStmt = connection.prepareStatement(deleteBaseSQL);
                deleteStmt.setInt(1, clanId);
                deleteStmt.executeUpdate();
                deleteStmt.close();

                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("base_deleted")));
                notificationManager.notifyBaseDeleted(clanId, player.getName());
            } else {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("base_not_set")));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not delete clan base: " + e.getMessage());
        }

        return true;
    }
}