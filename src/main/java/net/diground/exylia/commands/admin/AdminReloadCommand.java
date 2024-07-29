package net.diground.exylia.commands.admin;

import net.diground.exylia.ExyliaClans;
import net.diground.exylia.managers.NotificationManager;
import net.diground.exylia.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminReloadCommand {
    private final ExyliaClans plugin;


    public AdminReloadCommand(ExyliaClans plugin) {
        this.plugin = plugin;
    }


    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("exyliaclans.admin.reload")) {
            sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("no_permission")));
            return true;
        }

        plugin.reloadMessagesConfig();
        plugin.reloadConfig();
        sender.sendMessage(ChatUtils.translateColors(plugin.getMessage("reloaded")));
        return true;
    }
}
