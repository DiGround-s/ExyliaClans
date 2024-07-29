package net.diground.exylia.commands.members;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClanInviteCommand {
    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ClanInviteCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("usage_invite")));
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();
        String playerClanName = ClanUtils.getPlayerClan(plugin, playerUUID);

        if (playerClanName == null) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        // Verificar permisos del jugador
        if (!ClanUtils.hasPermission(plugin, playerUUID, "USE_INVITE")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("player_not_found")));
            return true;
        }

        String targetUUID = targetPlayer.getUniqueId().toString();

        if (ClanUtils.isPlayerInClan(plugin, targetUUID)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("player_already_in_clan")));
            return true;
        }

        try {
            Connection connection = plugin.getConnection();
            String query = "SELECT clans.id, clans.name FROM clans JOIN players ON clans.id = players.clan_id WHERE players.uuid = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, playerUUID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int clanId = rs.getInt("id");
                String clanName = rs.getString("name");

                String insertInviteSQL = "INSERT INTO invites (clan_id, player_uuid) VALUES (?, ?)";
                PreparedStatement inviteStmt = connection.prepareStatement(insertInviteSQL);
                inviteStmt.setInt(1, clanId);
                inviteStmt.setString(2, targetUUID);
                inviteStmt.executeUpdate();
                inviteStmt.close();

                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("invite_sent").replace("%player%", targetPlayer.getName())));
                notificationManager.notifyClanInvite(clanId, player.getName(), targetPlayer.getName());
                if (targetPlayer.isOnline()) {
                    targetPlayer.getPlayer().sendMessage(ChatUtils.translateColors(plugin.getMessage("invite_received").replace("%clan%", clanName)));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not send clan invite: " + e.getMessage());
        }

        return true;
    }
}
