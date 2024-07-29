package net.diground.exylia.commands.allies;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanAllyRequestCommand {

    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ClanAllyRequestCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("ally_request_usage")));
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();
        String clanName = ClanUtils.getPlayerClan(plugin, playerUUID);
        String targetClan = args[2];

        if (clanName == null) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        if (!ClanUtils.hasPermission(plugin, playerUUID, "REQUEST_ALLY")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        if (!ClanUtils.clanExists(plugin, targetClan)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("clan_not_found").replace("%clan%", targetClan)));
            return true;
        }

        if (clanName.equalsIgnoreCase(targetClan)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("ally_same_clan")));
            return true;
        }

        int clanNameId = ClanUtils.getClanIdByName(plugin, clanName);
        int targetClanId = ClanUtils.getClanIdByName(plugin, targetClan);
        if (ClanUtils.areClansAllied(plugin, clanNameId, targetClanId)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("already_allied").replace("%clan%", targetClan)));
            return true;
        }

        if (ClanUtils.hasPendingAllyRequest(plugin, clanNameId, targetClanId)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("pending_request").replace("%clan%", targetClan)));
            return true;
        }


        int maxAllies = plugin.getLimitManager().getMaxAlliances();
        Bukkit.getLogger().info("Max allies: " + maxAllies + " Allies Count: " + ClanUtils.getAllianceCount(plugin, clanNameId));
        if (ClanUtils.getAllianceCount(plugin, clanNameId) >= maxAllies) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("max_allies_reached")));
            return true;
        }
        if (ClanUtils.getAllianceCount(plugin, targetClanId) >= maxAllies) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("ally_clan_max_allies_reached").replace("%clan%", targetClan)));
            return true;
        }

        ClanUtils.sendAllyRequest(plugin, clanNameId, targetClanId);
        player.sendMessage(ChatUtils.translateColors(plugin.getMessage("ally_request_sent").replace("%clan%", targetClan)));

        // Notificar a los miembros del clan del jugador que envía la solicitud
        notificationManager.notifyAllyRequestSent(ClanUtils.getPlayerClanId(plugin, playerUUID), targetClan);

        // Notificar a los miembros del clan objetivo de la solicitud
        notificationManager.notifyAllyRequestReceived(ClanUtils.getClanIdByName(plugin, targetClan), clanName);

        return true;
    }
}
