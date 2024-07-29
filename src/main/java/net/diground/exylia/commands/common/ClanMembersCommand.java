package net.diground.exylia.commands.common;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.menus.model.InventoryPlayer;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanMembersCommand {

    private final ExyliaClans plugin;

    public ClanMembersCommand(ExyliaClans plugin) {
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getClanManager().isInClan(player)) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("not_in_clan")));
            return true;
        }


        if (!ClanUtils.hasPermission(plugin, player.getUniqueId().toString(), "VIEW_MEMBERS")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        if (args.length == 1) {
            plugin.getMenuManager().openMembersMenu(new InventoryPlayer(player), ClanUtils.getPlayerClanId(plugin, player.getUniqueId().toString()), 0);
            return true;
        } else if (args.length <= 2) {
            String target = args[1];
            OfflinePlayer targetPlayer = plugin.getServer().getOfflinePlayer(target);

            // Verifica si el jugador ha jugado antes o si su nombre es diferente de null
            if (targetPlayer != null && targetPlayer.hasPlayedBefore()) {
                if (ClanUtils.isPlayerInClan(plugin, targetPlayer.getUniqueId().toString())) {
                    plugin.getMenuManager().openMembersMenu(new InventoryPlayer(player), ClanUtils.getPlayerClanId(plugin, targetPlayer.getUniqueId().toString()), 0);
                } else {
                    player.sendMessage(ChatUtils.translateColors(plugin.getMessage("player_not_in_clan")));
                }
                return true;
            } else if (ClanUtils.clanExists(plugin, target)) {
                int clandId = ClanUtils.getClanIdByName(plugin, target);
                plugin.getMenuManager().openMembersMenu(new InventoryPlayer(player), clandId, 0);
                return true;
            }

            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("player_or_clan_not_found")));
            return true;
        }
        return true;
    }
}
