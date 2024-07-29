package net.diground.exylia.commands.common;

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

public class ClanPvpCommand {
    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ClanPvpCommand(ExyliaClans plugin) {
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

        if (!ClanUtils.isPlayerInClan(plugin, playerUUID)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        if (!ClanUtils.hasPermission(plugin, playerUUID, "USE_PVP")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID);
        boolean pvpEnabled = isPvpEnabled(clanId);

        try {
            Connection connection = plugin.getConnection();
            String updatePvpSQL = "UPDATE clans SET pvp_enabled = ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(updatePvpSQL);
            stmt.setBoolean(1, !pvpEnabled);
            stmt.setInt(2, clanId);
            stmt.executeUpdate();
            stmt.close();

            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("pvp_toggled")
                    .replace("%status%", pvpEnabled ? "disabled" : "enabled")));
            if (!pvpEnabled) {
                notificationManager.notifyPvpEnabled(clanId);
            } else {
                notificationManager.notifyPvpDisabled(clanId);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not toggle PvP: " + e.getMessage());
        }

        return true;
    }

    private boolean isPvpEnabled(int clanId) {
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
}