package net.diground.exylia.commands.common;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ClanListCommand {

    private final ExyliaClans plugin;
    //private final MenuLoader menuLoader;
    private static final int CLANS_PER_PAGE = 45;
    private static final Map<Player, Integer> playerPages = new HashMap<>();

    public ClanListCommand(ExyliaClans plugin) {
        this.plugin = plugin;
        //this.menuLoader = new MenuLoader(plugin);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("only_players")));
            return true;
        }

        Player player = (Player) sender;
        String menuTitle;

        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatUtils.translateColors("Invalid page number."));
                return true;
            }
        }

        return true;
    }
}
