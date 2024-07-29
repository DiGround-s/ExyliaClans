package net.diground.exylia.commands.base;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClanSetbaseCommand {

    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ClanSetbaseCommand(ExyliaClans plugin) {
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


        if (!ClanUtils.hasPermission(plugin, playerUUID, "SET_BASE")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID);
        Location loc = player.getLocation();

        try {
            Connection connection = plugin.getConnection();
            String updateBaseSQL = "UPDATE clans SET base_world = ?, base_x = ?, base_y = ?, base_z = ?, base_yaw = ?, base_pitch = ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(updateBaseSQL);
            stmt.setString(1, loc.getWorld().getName());
            stmt.setDouble(2, loc.getX());
            stmt.setDouble(3, loc.getY());
            stmt.setDouble(4, loc.getZ());
            stmt.setFloat(5, loc.getYaw());
            stmt.setFloat(6, loc.getPitch());
            stmt.setInt(7, clanId);
            stmt.executeUpdate();
            stmt.close();

            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("base_set_success")));
            notificationManager.notifyBaseCreated(clanId, player.getName());
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not set clan base: " + e.getMessage());
        }

        return true;
    }
}