package net.diground.exylia.commands.common;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.menus.model.InventoryPlayer;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanBannerCommand {

    private final ExyliaClans plugin;

    public ClanBannerCommand(ExyliaClans plugin) {
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


        if (!ClanUtils.hasPermission(plugin, player.getUniqueId().toString(), "USE_BANNER")) {
            player.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        if (args.length == 1) {
            plugin.getMenuManager().openBannerMenu(new InventoryPlayer(player), ClanUtils.getPlayerClanId(plugin, player.getUniqueId().toString()));
            return true;
        }
        return true;
    }

}
