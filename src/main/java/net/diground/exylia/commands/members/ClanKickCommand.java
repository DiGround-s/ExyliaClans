package net.diground.exylia.commands.members;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import net.diground.exylia.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClanKickCommand {
    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ClanKickCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("usage_kick")));
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();
        String clanName;

        try {
            clanName = ClanUtils.getPlayerClan(plugin, playerUUID);
            if (clanName == null) {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
                return true;
            }

            if (!ClanUtils.hasPermission(plugin, playerUUID, "USE_KICK")) {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
                return true;
            }

            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
            if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("player_not_found")));
                return true;
            }

            if (player.getUniqueId().equals(targetPlayer.getUniqueId())) {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("cannot_kick_self")));
                return true;
            }

            int playerClanId = ClanUtils.getPlayerClanId(plugin, playerUUID);
            int targetClanId = ClanUtils.getPlayerClanId(plugin, targetPlayer.getUniqueId().toString());

            if (playerClanId != targetClanId) {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_same_clan")));
                return true;
            }

            if (ClanUtils.isPlayerClanLeader(plugin, targetPlayer.getUniqueId().toString())) {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("cannot_kick_leader")));
                return true;
            }

            if (PlayerUtils.kickPlayer(plugin, targetPlayer.getUniqueId().toString())) {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("kick_success").replace("%player%", targetPlayer.getName())));
                if (targetPlayer.isOnline()) {
                    ((Player) targetPlayer).sendMessage(ChatUtils.translateColors(plugin.getMessage("kicked").replace("%clan%", ClanUtils.getClanName(plugin, playerClanId))));
                }
                notificationManager.notifyMemberKicked(playerClanId, targetPlayer.getName());

            } else {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("kick_failed")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not kick player from clan: " + e.getMessage());
        }

        return true;
    }
}