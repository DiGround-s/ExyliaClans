package net.diground.exylia.commands.common;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClanDisbandCommand {

    private final ExyliaClans plugin;
    private final Map<UUID, Long> disbandConfirmations = new HashMap<>();

    public ClanDisbandCommand(ExyliaClans plugin) {
        this.plugin = plugin;
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

        if (!ClanUtils.isPlayerClanLeader(plugin, playerUUID)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_clan_leader")));
            return true;
        }

        if (disbandConfirmations.containsKey(player.getUniqueId())) {
            long timeSinceFirstRequest = System.currentTimeMillis() - disbandConfirmations.get(player.getUniqueId());
            if (timeSinceFirstRequest > 10000) {
                disbandConfirmations.remove(player.getUniqueId());
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("disband_timeout")));
            } else {
                disbandClan(playerUUID);
                disbandConfirmations.remove(player.getUniqueId());
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("clan_disbanded")));
            }
        } else {
            disbandConfirmations.put(player.getUniqueId(), System.currentTimeMillis());
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("disband_confirmation")));
        }

        return true;
    }


    private void disbandClan(String playerUUID) {
        Connection connection = plugin.getConnection();
        try {
            String query = "SELECT clan_id FROM players WHERE uuid = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, playerUUID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int clanId = rs.getInt("clan_id");

                // Delete the clan
                String deleteClanSQL = "DELETE FROM clans WHERE id = ?";
                PreparedStatement deleteClanStmt = connection.prepareStatement(deleteClanSQL);
                deleteClanStmt.setInt(1, clanId);
                deleteClanStmt.executeUpdate();
                deleteClanStmt.close();

                // Delete all players in the clan
                String deletePlayersSQL = "DELETE FROM players WHERE clan_id = ?";
                PreparedStatement deletePlayersStmt = connection.prepareStatement(deletePlayersSQL);
                deletePlayersStmt.setInt(1, clanId);
                deletePlayersStmt.executeUpdate();
                deletePlayersStmt.close();
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not disband clan: " + e.getMessage());
        }
    }
}
