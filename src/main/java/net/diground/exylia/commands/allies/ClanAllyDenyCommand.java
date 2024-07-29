package net.diground.exylia.commands.allies;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanAllyDenyCommand {

    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ClanAllyDenyCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("ally_deny_usage")));
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();
        String clanName = ClanUtils.getPlayerClan(plugin, playerUUID);
        String requestingClan = args[2];

        if (!ClanUtils.hasPermission(plugin, playerUUID, "DENY_ALLY")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        if (!ClanUtils.clanExists(plugin, requestingClan)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("clan_not_found").replace("%clan%", requestingClan)));
            return true;
        }

        if (clanName == null) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        int requestingClanId = ClanUtils.getClanIdByName(plugin, requestingClan);
        int clanId = ClanUtils.getClanIdByName(plugin, clanName);
        if (!ClanUtils.hasPendingAllyRequest(plugin, requestingClanId, clanId)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_pending_request").replace("%clan%", requestingClan)));
            return true;
        }

        ClanUtils.denyAllyRequest(plugin, requestingClanId, clanId);
        sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("ally_request_denied").replace("%clan%", requestingClan)));

        // Notificar a los miembros del clan que envió la solicitud
        notificationManager.notifyAllyRequestDenied(ClanUtils.getClanIdByName(plugin, requestingClan), clanName);

        // Notificar a los miembros del clan que denegó la solicitud
        notificationManager.notifyAllyRequestDenied(ClanUtils.getPlayerClanId(plugin, playerUUID), requestingClan);

        return true;
    }
}
