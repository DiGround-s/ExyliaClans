package net.diground.exylia.commands.main;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.menus.model.InventoryPlayer;
import net.diground.exylia.utils.ChatUtils;
import net.diground.exylia.utils.ClanUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanMainCommand {

    private final ExyliaClans plugin;

    public ClanMainCommand(ExyliaClans plugin) {
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getClanManager().isInClan(player)) {
            plugin.getMenuManager().openMainNoClanMenu(new InventoryPlayer(player));
        } else {
            plugin.getMenuManager().openMainMenu(new InventoryPlayer(player), ClanUtils.getPlayerClanId(plugin, player.getUniqueId().toString()));
        }
        return true;
    }
}
