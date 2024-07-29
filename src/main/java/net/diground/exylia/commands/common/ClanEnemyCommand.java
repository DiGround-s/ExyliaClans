package net.diground.exylia.commands.common;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.menus.model.InventoryPlayer;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanEnemyCommand {
    private final ExyliaClans plugin;
    private final NotificationManager notificationManager;

    public ClanEnemyCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        this.notificationManager = new NotificationManager(plugin);

    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 2) {
            plugin.getMenuManager().openEnemiesMenu(new InventoryPlayer(player), ClanUtils.getPlayerClanId(plugin, player.getUniqueId().toString()));
            return true;
        }

        String playerUUID = player.getUniqueId().toString();
        String clanName = ClanUtils.getPlayerClan(plugin, playerUUID);
        String targetClan = args[1];

        if (clanName == null) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }

        if (!ClanUtils.hasPermission(plugin, playerUUID, "USE_ENEMY")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        if (!ClanUtils.clanExists(plugin, targetClan)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("clan_not_found").replace("%clan%", targetClan)));
            return true;
        }

        if (clanName.equalsIgnoreCase(targetClan)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("enemy_same_clan")));
            return true;
        }


        int clanId = ClanUtils.getClanIdByName(plugin, clanName);
        int targetClanId = ClanUtils.getClanIdByName(plugin, targetClan);
        if (ClanUtils.areClansAllied(plugin, clanId, targetClanId)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("this_clan_is_allied").replace("%clan%", targetClan)));
            return true;
        }

        int currentEnemies = ClanUtils.getEnemyCount(plugin, clanId);
        int maxEnemies = plugin.getLimitManager().getMaxEnemies();

        if (ClanUtils.areClansEnemies(plugin, clanId, targetClanId)) {
            ClanUtils.removeEnemy(plugin, clanId, targetClanId);
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("enemy_removed").replace("%clan%", targetClan)));
            notificationManager.notifyEnemyRemoved(ClanUtils.getPlayerClanId(plugin, playerUUID), targetClan);
        } else {
            if (currentEnemies >= maxEnemies) {
                player.sendMessage(ChatUtils.translateColors(plugin.getMessage("max_enemies_reached")));
                return true;
            }
            ClanUtils.addEnemy(plugin, clanId, targetClanId);
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("enemy_added").replace("%clan%", targetClan)));
            notificationManager.notifyEnemyAdded(ClanUtils.getPlayerClanId(plugin, playerUUID), targetClan);
        }
        return true;
    }
}
