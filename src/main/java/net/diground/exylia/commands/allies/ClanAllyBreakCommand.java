package net.diground.exylia.commands.allies;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClanAllyBreakCommand {

    private final ExyliaClans plugin;
    private final Map<UUID, Long> confirmationMap;
    private final NotificationManager notificationManager;

    public ClanAllyBreakCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.confirmationMap = new HashMap<>();
        this.notificationManager = new NotificationManager(plugin);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("ally_break_usage")));
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        String clanName = ClanUtils.getPlayerClan(plugin, playerUUID.toString());
        String targetClan = args[2];

        if (clanName == null) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        if (!ClanUtils.hasPermission(plugin, playerUUID.toString(), "BREAK_ALLY")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        if (!ClanUtils.clanExists(plugin, targetClan)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("clan_not_found").replace("%clan%", targetClan)));
            return true;
        }
        int clanNameId = ClanUtils.getClanIdByName(plugin, clanName);
        int targetClanId = ClanUtils.getClanIdByName(plugin, targetClan);
        if (!ClanUtils.areClansAllied(plugin, clanNameId, targetClanId)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_allied").replace("%clan%", targetClan)));
            return true;
        }

        if (confirmationMap.containsKey(playerUUID) && confirmationMap.get(playerUUID) > System.currentTimeMillis()) {
            ClanUtils.breakAlliance(plugin, clanNameId, targetClanId);
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("ally_broken").replace("%clan%", targetClan)));
            confirmationMap.remove(playerUUID);

            int clanId = ClanUtils.getPlayerClanId(plugin, playerUUID.toString());

            // Notificar a los miembros de ambos clanes de la ruptura de la alianza
            notificationManager.notifyAllianceBroken(clanId, targetClan);
            notificationManager.notifyAllianceBroken(ClanUtils.getClanIdByName(plugin, targetClan), clanName);
        } else {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("ally_break_confirm")));
            confirmationMap.put(playerUUID, System.currentTimeMillis() + 10000);

            new BukkitRunnable() {
                @Override
                public void run() {
                    confirmationMap.remove(playerUUID);
                }
            }.runTaskLater(plugin, 200); // 200 ticks = 10 seconds
        }

        return true;
    }
}
