package net.diground.exylia.commands.allies;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanAllyAcceptCommand {

    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ClanAllyAcceptCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("ally_accept_usage")));
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();
        String clanName = ClanUtils.getPlayerClan(plugin, playerUUID);
        String requestingClan = args[2];

        if (clanName == null) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        if (!ClanUtils.hasPermission(plugin, playerUUID, "ACCEPT_ALLY")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        if (!ClanUtils.clanExists(plugin, requestingClan)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("clan_not_found").replace("%clan%", requestingClan)));
            return true;
        }
        int clanId = ClanUtils.getClanIdByName(plugin, clanName);
        int requestingClanId = ClanUtils.getClanIdByName(plugin, requestingClan);

        if (!ClanUtils.hasPendingAllyRequest(plugin, requestingClanId, clanId)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_pending_request").replace("%clan%", requestingClan)));
            return true;
        }


        int maxAllies = plugin.getLimitManager().getMaxAlliances();
        if (ClanUtils.getAllianceCount(plugin, clanId) >= maxAllies) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("max_allies_reached")));
            return true;
        }
        if (ClanUtils.getAllianceCount(plugin, requestingClanId) >= maxAllies) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("ally_clan_max_allies_reached").replace("%clan%", requestingClan)));
            return true;
        }


        ClanUtils.acceptAllyRequest(plugin, requestingClanId, clanId);
        sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("ally_request_accepted").replace("%clan%", requestingClan)));

        // Notificar a los miembros del clan que envió la solicitud
        notificationManager.notifyAllyRequestAccepted(ClanUtils.getClanIdByName(plugin, requestingClan), clanName);

        // Notificar a los miembros del clan que aceptó la solicitud
        notificationManager.notifyAllyRequestAccepted(ClanUtils.getPlayerClanId(plugin, playerUUID), requestingClan);

        return true;
    }
}
