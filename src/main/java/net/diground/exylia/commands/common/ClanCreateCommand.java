package net.diground.exylia.commands.common;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClanCreateCommand {

    private final ExyliaClans plugin;

    public ClanCreateCommand(ExyliaClans plugin) {
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("usage_create")));
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();
        String clanName = args[1];

        // Validar nombre del clan
        if (!ClanUtils.isValidClanName(plugin, clanName)) {
            String invalidClanMessage = plugin.getMessage("invalid_clan_name")
                    .replace("%min%", String.valueOf(plugin.getConfig().getInt("name.min_length")))
                    .replace("%max%", String.valueOf(plugin.getConfig().getInt("name.max_length")));

            player.sendMessage(ChatUtils.translateColors(invalidClanMessage));
            return true;
        }

        if (plugin.getClanManager().isInClan(player)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("in_clan")));
            return true;
        }

        if (isClanNameTaken(clanName)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("clan_name_taken")));
            return true;
        }

        try {
            if (plugin.getEconomy() != null) {
                Bukkit.getLogger().info("Using economy. Cost: " + plugin.getConfig().getInt("cost.clan_create"));
                int cost = plugin.getConfig().getInt("cost.clan_create");
                if (plugin.getEconomy().getBalance(player) < cost) {
                    player.sendMessage(ChatUtils.translateColors(plugin.getMessage("insufficient_player_funds_with_cost").replace("%cost%", String.valueOf(cost))));
                    return true;
                } else {
                    plugin.getEconomy().withdrawPlayer(player, cost);
                }
            }

            Connection connection = plugin.getConnection();
            String insertClanSQL = "INSERT INTO clans (name, created_date) VALUES (?, CURRENT_TIMESTAMP)";
            PreparedStatement stmt = connection.prepareStatement(insertClanSQL, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, clanName);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int clanId = generatedKeys.getInt(1);

                ClanUtils.updateClanPrefix(plugin, clanId, clanName);

                String insertPlayerSQL = "INSERT INTO players (uuid, name, clan_id, user_rank_id, join_date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
                PreparedStatement playerStmt = connection.prepareStatement(insertPlayerSQL);
                playerStmt.setString(1, playerUUID);
                playerStmt.setString(2, player.getName());
                playerStmt.setInt(3, clanId);
                playerStmt.setInt(4, 0);
                playerStmt.executeUpdate();
                playerStmt.close();

            }

            generatedKeys.close();
            stmt.close();

            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("clan_created").replace("%clan%", clanName)));
            if (plugin.getConfig().getBoolean("broadcast.clan_created")) {
                String rawBroadcastMessage = plugin.getMessage("clan_created_broadcast")
                        .replace("%clan%", clanName)
                        .replace("%player%", player.getName());

                Component broadcastMessage = MiniMessage.miniMessage().deserialize(rawBroadcastMessage);
                Bukkit.broadcast(broadcastMessage);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create clan: " + e.getMessage());
        }

        return true;
    }

    private boolean isClanNameTaken(String clanName) {
        try {
            Connection connection = plugin.getConnection();
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

}
