package net.diground.exylia.commands.members;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.managers.Rank;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import net.diground.exylia.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class ClanDemoteCommand {
    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ClanDemoteCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("usage_demote")));
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();
        String clanName = ClanUtils.getPlayerClan(plugin, playerUUID);

        if (clanName == null) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        if (!ClanUtils.hasPermission(plugin, playerUUID, "USE_DEMOTE")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("player_not_found")));
            return true;
        }

        if (player.getUniqueId().equals(targetPlayer.getUniqueId())) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("cannot_demote_self")));
            return true;
        }

        try {
            Rank currentRank = PlayerUtils.getCurrentRank(plugin, targetPlayer.getUniqueId().toString());

            if (currentRank != null && currentRank.getRankOrder() < plugin.getRankManager().getLowestRankOrder()) {
                Rank newRank = plugin.getRankManager().getRankByOrder(currentRank.getRankOrder() + 1);

                if (newRank != null) {
                    if (PlayerUtils.updateRank(plugin, targetPlayer.getUniqueId().toString(), newRank.getId())) {
                        String targetName = targetPlayer.getName() != null ? targetPlayer.getName() : "Unknown";
                        player.sendMessage(ChatUtils.translateColors(plugin.getMessage("demote_success").replace("%player%", targetName).replace("%rank%", newRank.getDisplayName())));

                        if (targetPlayer.isOnline()) {
                            ((Player) targetPlayer).sendMessage(ChatUtils.translateColors(plugin.getMessage("demoted").replace("%rank%", newRank.getDisplayName())));
                        }

                        notificationManager.notifyMemberDemoted(ClanUtils.getPlayerClanId(plugin, playerUUID), targetName, newRank.getDisplayName());
                    }
                } else {
                    player.sendMessage(ChatUtils.translateColors(plugin.getMessage("already_lowest_rank")));
                }
            } else {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("already_lowest_rank")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not demote player: " + e.getMessage());
        }

        return true;
    }
}
