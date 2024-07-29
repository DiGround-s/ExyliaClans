package net.diground.exylia.commands.vip;

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

public class ClanRenameCommand {
    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ClanRenameCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("usage_rename")));
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();
        String newClanName = args[1];
        String clanName = ClanUtils.getPlayerClan(plugin, playerUUID);


        if (clanName == null) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        if (!ClanUtils.hasPermission(plugin, playerUUID, "USE_RENAME")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        if (!ClanUtils.isValidClanName(plugin, newClanName)) {
            String invalidClanMessage = plugin.getMessage("invalid_clan_name")
                    .replace("%min%", String.valueOf(plugin.getConfig().getInt("name.min_length")))
                    .replace("%max%", String.valueOf(plugin.getConfig().getInt("name.max_length")));

            player.sendMessage(ChatUtils.translateColors(invalidClanMessage));
            return true;
        }

        if (isClanNameTaken(newClanName)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("clan_name_taken")));
            return true;
        }

        int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID);
        ClanUtils.updateClanPrefix(plugin, clanId, newClanName);
        ClanUtils.updateClanName(plugin, clanId, newClanName);
        player.sendMessage(ChatUtils.translateColors(plugin.getMessage("clan_renamed").replace("%clan%", newClanName)));
        notificationManager.notifyClanRenamed(clanId, newClanName);

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