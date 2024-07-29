package net.diground.exylia.commands.vip;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClanPrefixCommand {
    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ClanPrefixCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("usage_prefix")));
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();
        String newPrefix = args[1];
        String clanName = ClanUtils.getPlayerClan(plugin, playerUUID);


        if (clanName == null) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        if (!player.hasPermission("exyliaclan.vip.prefix")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission_vip")));
            return true;
        }

        if (!ClanUtils.hasPermission(plugin, playerUUID, "USE_PREFIX")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        if (!ClanUtils.isValidPrefix(plugin, newPrefix)) {
            String invalidClanMessage = plugin.getMessage("invalid_clan_prefix")
                    .replace("%min%", String.valueOf(plugin.getConfig().getInt("prefix.min_length")))
                    .replace("%max%", String.valueOf(plugin.getConfig().getInt("prefix.max_length")));

            player.sendMessage(ChatUtils.translateColors(invalidClanMessage));
            return true;
        }

        if (ClanUtils.isClanPrefixTaken(plugin, newPrefix)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("clan_prefix_taken")));
            return true;
        }

        int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID);

        try {
            ClanUtils.updateClanPrefix(plugin, clanId, newPrefix);
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("clan_prefix_updated")
                    .replace("%prefix%", newPrefix)));
        } catch (Exception e) {
            plugin.getLogger().severe("Could not update clan prefix: " + e.getMessage());
            return true;
        }

        return true;
    }
}
