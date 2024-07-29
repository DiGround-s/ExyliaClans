package net.diground.exylia.commands.extras;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.menus.model.InventoryPlayer;
import net.diground.exylia.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanTopCommand {
    private final ExyliaClans plugin;

    public ClanTopCommand(ExyliaClans plugin) {
        this.plugin = plugin;
    }



    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        Player player = (Player) sender;

        plugin.getMenuManager().openLeaderboardMenu(new InventoryPlayer(player), 0, "kills");

        return true;
    }

}
